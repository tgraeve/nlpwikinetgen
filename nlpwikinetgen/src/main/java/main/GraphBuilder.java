package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import static org.apache.spark.sql.functions.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.apache.log4j.*;
import org.graphframes.GraphFrame;
import info.collide.nlpwikinetgen.helper.GMLWriter;
import info.collide.nlpwikinetgen.type.BasicNode;
import info.collide.nlpwikinetgen.type.BoolNode;
import info.collide.nlpwikinetgen.type.DoubleNode;
import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.IntNode;
import info.collide.nlpwikinetgen.type.Node;
import info.collide.nlpwikinetgen.type.StringPair;

public class GraphBuilder implements Serializable {
	static SparkSession spark;
	List<Node> nodes;
	List<Edge> edges;
	Dataset<Row> dfNodes;
	Dataset<Row> dfEdges;
	static GraphFrame gf;
	public String pathToFolder;
	
	public GraphBuilder(String pathToFolder) {
		this.pathToFolder = pathToFolder;
		
		spark = SparkSession.builder()
				.appName("NLPWikiNetGen")
				.master("local[*]")
				.config("spark.sql.broadcastTimeout", 1800)
				.config("spark.executor.memory", "8g")
				.config("spark.driver.memory", "8g")
				.config("spark_local_ip","127.0.0.1")
				.getOrCreate();
		
		//set runtime options
		spark.sparkContext().setCheckpointDir(pathToFolder+"/cp-dir");
		
		Logger.getLogger("org").setLevel(Level.WARN);
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		nodes = deserializeNodes(pathToFolder);
		edges = deserializeEdges(pathToFolder);
		
	}
	
	public void generateGraph(List<StringPair> filters, ArrayList<String> keyRev) throws IOException {
		Dataset<Row> allNodes;
		GMLWriter writer = new GMLWriter(pathToFolder);
		
		dfNodes = spark.createDataFrame(nodes, Node.class);
		dfEdges = spark.createDataFrame(edges, Edge.class);
		dfNodes.cache();
		dfEdges.cache();
		
		if(keyRev==null) {
			allNodes = dfNodes;
		}
		else {
			allNodes = dfNodes.filter(n -> !keyRev.contains(n.getString(0)));
		}
		allNodes.cache();
		
		if(filters.size()>0 || keyRev!=null) {
			
			Dataset<Row> minorNodeIds = getMergedMinorNodes(filters);
//			Dataset<Row> minorNodes = allNodes.join(minorNodeIds, allNodes.col("id").equalTo(minorNodeIds.col("id"))).drop(minorNodeIds.col("id"));
			Dataset<Row> majorNodes = allNodes.join(minorNodeIds, allNodes.col("id").equalTo(minorNodeIds.col("id")), "leftanti");
			majorNodes.persist(StorageLevel.MEMORY_AND_DISK());
			List<Node> majorNodeList = majorNodes.javaRDD().map(r -> new Node(r.getString(0),r.getString(1))).collect();
			
			//edges of type "link" where neither source nor destination are deleted can be kept
			Dataset<Row> allLinks = dfEdges.filter("type = 'link'").join(dfNodes, dfEdges.col("src").equalTo(dfNodes.col("id"))).drop("id", "pageid").dropDuplicates(); //sort out links from foreign sites (where no nodes in data are existent)
			allLinks.cache();
			
			System.out.println("Determine unaffected and affected links...");
			Dataset<Row> unaffectedLinks = allLinks.join(majorNodes, allLinks.col("src").equalTo(majorNodes.col("id"))).drop("id","pageid");
			unaffectedLinks = unaffectedLinks.join(majorNodes, unaffectedLinks.col("dst").equalTo(majorNodes.col("id"))).drop("id", "pageid");		
			Dataset<Row> affectedLinks = allLinks.except(unaffectedLinks);
			
			System.out.println("Start correcting link endges...");
			Dataset<Row> corrLinkEdges = affectedLinks.join(allNodes, affectedLinks.col("src").equalTo(allNodes.col("id"))).drop("id");
			corrLinkEdges.persist(StorageLevel.DISK_ONLY());
			corrLinkEdges.count(); //to defeat timeouterrors due to long joins.
			corrLinkEdges = corrLinkEdges.join(majorNodes, corrLinkEdges.col("pageId").equalTo(majorNodes.col("pageId"))).drop("pageId").drop(majorNodes.col("pageId"));
			corrLinkEdges = corrLinkEdges.filter(x -> Integer.parseInt(x.getString(1)) > Integer.parseInt(x.getString(3))).withColumn("id", corrLinkEdges.col("id").cast("int"));
			corrLinkEdges = corrLinkEdges.groupBy("dst","src","type").max("id");
			corrLinkEdges = corrLinkEdges.drop("src").withColumn("src", corrLinkEdges.col("max(id)").cast("string")).select("dst","src","type");

			
			/*
			 * rebuilds revision edges for all remaining nodes
			 * 
			 * no big benefit through optimization like no rebuilding if no revision of a page is affected, because
			 * this is almost not the case.
			 * same for processing every deleted node separately 
			 * 
			 * 
			 */
			
			System.out.println("Start retrieving corrected revision endges...");
			Dataset<Row> corrRevEdges = majorNodes.withColumnRenamed("id", "src")
													.join(majorNodes, "pageId")
													.filter(x -> Integer.parseInt(x.getString(1)) < Integer.parseInt(x.getString(2)))
													.withColumn("dst", col("id").cast("int"))
													.drop("id")
													.groupBy("src", "pageId").min("dst")
													.withColumnRenamed("min(dst)", "dst")
													.withColumnRenamed("pageId", "type")
													.withColumn("dst", col("dst").cast("string"))
													.select("dst","src","type");
			
			corrRevEdges = spark.createDataFrame(corrRevEdges.javaRDD().map(x -> {return RowFactory.create(x.getString(0), x.getString(1), "revision");}),corrRevEdges.schema());
			
			System.out.println("Start to merge new graphdata...");
			List<Edge> allCorrEdges = new ArrayList<>();
			allCorrEdges.addAll(corrRevEdges.javaRDD().map(r -> new Edge(r.getString(1), r.getString(0),r.getString(2))).collect());
			allCorrEdges.addAll(unaffectedLinks.javaRDD().map(r -> new Edge(r.getString(1), r.getString(0),r.getString(2))).collect());
			allCorrEdges.addAll(corrLinkEdges.javaRDD().map(r -> new Edge(r.getString(1), r.getString(0),r.getString(2))).collect());
			
			System.out.println("ALLE KNOTEN: "+allNodes.count()+" MINUS "+minorNodeIds.count()+" = "+majorNodes.count()+" KNOTEN UND "+allCorrEdges.size()+" KANTEN!");
			writer.writeFile(majorNodeList, allCorrEdges);
		}
		else {
			writer.writeFile(nodes, edges);
		}
		spark.stop();
	}
	
	private Dataset<Row> verifyMinorNodes(Dataset<Row> minorNodes) {
		Dataset<Row> linkDst = dfEdges.filter("type='link'").select("dst");
		Dataset<Row> revDst = dfEdges.filter("type='revision'").select("dst");
		Dataset<Row> verifiedNodes = minorNodes.except(linkDst); //delete link destinations and start nodes out of minor nodes
		System.out.println("verified.");
		verifiedNodes.intersect(revDst); //filter nodes which have no revision inlink (first nodes)
		
		return verifiedNodes;
	}
	
	public Dataset<Row> getMergedMinorNodes(List<StringPair> filters) {
		StructType schema = DataTypes.createStructType(new StructField[] {DataTypes.createStructField("id", DataTypes.StringType, false)});
//		Dataset<Row> mergedNodes = spark.createDataFrame(new ArrayList<Row>(),schema);
		List<Row> mergedNodes = new ArrayList<Row>();

		for(StringPair s : filters) {
			mergedNodes.addAll(getMinorNodes(s.getS1(), s.getS2()));
		}
		Dataset<Row> minorNodes = spark.createDataFrame(mergedNodes, schema);
		minorNodes = minorNodes.distinct();
		minorNodes = verifyMinorNodes(minorNodes);
		return minorNodes;
	}
	
	public List<Row> getMinorNodes(String data, Object threshold) { //TODO tipp: avoiding redundant swap Dataset <-> ArrayList would tweak performance!
		Dataset<Row> df = null;
		Dataset<Row> minor = null;
		List nodes = deserialize(data);
		if (nodes.get(0).getClass()==DoubleNode.class) {
			df = spark.createDataFrame(nodes, DoubleNode.class);
			minor = df.filter("value>"+threshold);
		} else if (nodes.get(0).getClass()==BoolNode.class) {
			df = spark.createDataFrame(nodes, BoolNode.class);
			minor = df.filter("value=true");
		} else if (nodes.get(0).getClass()==IntNode.class) {
			df = spark.createDataFrame(nodes, IntNode.class);
			minor = df.filter("value<"+threshold);
		}
		minor = minor.drop("value");
		return minor.collectAsList();
	}
	
	private ArrayList<BasicNode> deserialize(String filter) {
		FileInputStream fis;
		ArrayList<BasicNode> nodes = null;
		try {
			fis = new FileInputStream(pathToFolder+"/"+filter+".filter");
			System.out.println(pathToFolder+"/"+filter+".filter");
			ObjectInputStream ois = new ObjectInputStream(fis);
	        nodes = (ArrayList<BasicNode>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return nodes;
	}
	
	private ArrayList<Node> deserializeNodes(String pathToFolder) {
		FileInputStream fis;
		ArrayList<Node> nodes = null;
		try {
			fis = new FileInputStream(pathToFolder+"/nodes.tmp");
			ObjectInputStream ois = new ObjectInputStream(fis);
	        nodes = (ArrayList<Node>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return nodes;
	}
	
	private ArrayList<Edge> deserializeEdges(String pathToFolder) {
		FileInputStream fis;
		ArrayList<Edge> edges = null;
		try {
			fis = new FileInputStream(pathToFolder+"/edges.tmp");
			ObjectInputStream ois = new ObjectInputStream(fis);
	        edges = (ArrayList<Edge>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return edges;
	}
}