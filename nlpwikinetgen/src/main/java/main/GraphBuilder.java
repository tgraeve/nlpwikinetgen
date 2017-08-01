package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
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

public class GraphBuilder {
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
				.master("local[2]")
				.getOrCreate();
		
		Logger.getLogger("org").setLevel(Level.WARN);
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		nodes = deserializeNodes(pathToFolder);
		edges = deserializeEdges(pathToFolder);
		
		dfNodes = spark.createDataFrame(nodes, Node.class);
		dfEdges = spark.createDataFrame(edges, Edge.class);
		dfNodes.cache();
		dfEdges.cache();
//		gf = new GraphFrame(dfNodes, dfEdges);
	}
	
	public void generateGraph(List<StringPair> filters, ArrayList<String> keyRev) throws IOException {
		Dataset<Row> allNodes;
		if(keyRev==null) {
			allNodes = dfNodes;
		}
		else {
			allNodes = dfNodes.filter(n -> !keyRev.contains(n.getString(0)));
		}
		
		List<String> allNodeList = allNodes.javaRDD().map(r -> r.getString(0)).collect();
		Dataset<Row> minorNodes = getMergedMinorNodes(filters);
		List<String> minorList = minorNodes.javaRDD().map(r -> r.getString(0)).collect();
		Dataset<Row> majorNodes = allNodes.filter(n -> !minorList.contains(n.getString(0)));
		List<Node> majorNodeList = majorNodes.javaRDD().map(r -> new Node(r.getString(0),r.getString(1))).collect();
		majorNodes.show();
		
		//edges of type "link" where neither source nor destination are deleted can be kept
		Dataset<Row> allLinks = dfEdges.filter("type = 'link'").filter(r -> allNodeList.contains(r.getString(1)));
		Dataset<Row> unaffectedLinks = allLinks.filter(e -> !minorList.contains(e.getString(0))).filter(e -> !minorList.contains(e.getString(1)));
		unaffectedLinks.show();
		List<Edge> unaffectedLinksList = unaffectedLinks.javaRDD().map(r -> new Edge(r.getString(1),r.getString(0),r.getString(2))).collect(); //mind order of columns is not order of native node class!
		System.out.println(allLinks.count() +" ###" + unaffectedLinksList.size());
		Dataset<Row> affectedLinks = allLinks.except(unaffectedLinks);
		List<Edge> affectedLinkEdges = affectedLinks.javaRDD().map(r -> new Edge(r.getString(1), r.getString(0),r.getString(2))).collect();
//		List<String> affectedLinkSrc = affectedLinks.select("src").javaRDD().map(r -> r.getString(1)).collect();
		
		/*
		 * rebuilds revision edges for all remaining nodes
		 * 
		 * no big benefit through optimization like no rebuilding if no revision of a page is affected, because
		 * this is almost not the case.
		 * same for processing every deleted node separately 
		 * 
		 * 
		 */
		
		List<Edge> corrRevEdges = new ArrayList<>();
		String pageId = "";
		String prevRevId = "";
		String prevPageId = "";
		
		for(Node n : majorNodeList) {
			pageId = n.getPageId();
			
			if (prevRevId != "") {
				if (prevPageId.equals(n.getPageId())) {
					corrRevEdges.add(new Edge(prevRevId,n.getId(),"revision"));
				}
			}
			prevRevId = n.getId();
			prevPageId = n.getPageId();
		}
		
		List<Edge> corrLinkEdges = new ArrayList<>();
		for(Edge e : affectedLinkEdges) {
			String src = e.getSrc();
			
			String srcPage = dfNodes.filter("id="+src).first().getString(1);
			String newSource = majorNodes.filter("pageId="+srcPage).first().getString(0);
			Edge corrEdge = new Edge(newSource,e.getDst(),e.getType());
			corrLinkEdges.add(corrEdge);
		}
		
		List<Edge> allCorrEdges = new ArrayList<>();
		allCorrEdges.addAll(unaffectedLinksList);
		allCorrEdges.addAll(corrRevEdges);
		allCorrEdges.addAll(corrLinkEdges);
		
		System.out.println("ALLE KNOTEN: "+allNodes.count()+" MINUS "+minorList.size()+" = "+majorNodeList.size()+" KNOTEN UND "+allCorrEdges.size()+" KANTEN!");
		GMLWriter writer = new GMLWriter(pathToFolder);
		writer.writeFile(majorNodeList, allCorrEdges);
	}
	
	private Dataset<Row> verifyMinorNodes(Dataset<Row> minorNodes) {
		Dataset<Row> linkDst = dfEdges.filter("type='link'").select("dst");
		Dataset<Row> verifiedNodes = minorNodes.except(linkDst);				//filter nodes with inlinks
		Dataset<Row> revDst = dfEdges.filter("type='revision'").select("dst");
		verifiedNodes = verifiedNodes.except(dfNodes.select("id").except(revDst)); //filter nodes which have no revision inlink (first nodes)
		return verifiedNodes;
	}
	
	public Dataset<Row> getMergedMinorNodes(List<StringPair> filters) {
		StructType schema = DataTypes.createStructType(new StructField[] {DataTypes.createStructField("id", DataTypes.StringType, false)});
		Dataset<Row> mergedNodes = spark.createDataFrame(new ArrayList<Row>(),schema);

		for(StringPair s : filters) {
			mergedNodes = mergedNodes.union(getMinorNodes(s.getS1(), s.getS2()).select("id"));
			mergedNodes.cache(); 
		}
		mergedNodes = mergedNodes.distinct();
		mergedNodes = verifyMinorNodes(mergedNodes);
		return mergedNodes;
	}
	
	public Dataset<Row> getMinorNodes(String data, Object threshold) {
		Dataset<Row> df = null;
		Dataset<Row> minor = null;
		List<BasicNode> nodes = deserialize(data);
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
		return minor;
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