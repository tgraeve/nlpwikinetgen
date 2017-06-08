package main;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.plans.logical.Distinct;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.graphframes.GraphFrame;

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
	public static String pathToFolder;
	
	public GraphBuilder(String pathToFolder) {
		this.pathToFolder = pathToFolder;
		
		spark = SparkSession.builder()
				.appName("NLPWikiNetGen")
				.master("local[2]")
				.getOrCreate();
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		nodes = deserializeNodes(pathToFolder);
		edges = deserializeEdges(pathToFolder);
		
		dfNodes = spark.createDataFrame(nodes, Node.class);
		dfEdges = spark.createDataFrame(edges, Edge.class);
		dfNodes.cache();
		dfEdges.cache();
		gf = new GraphFrame(dfNodes, dfEdges);
	}
	
	public Dataset<Row> reduceNodes(ArrayList<Class> nodes, Object threshold) {
		Dataset<Row> df;
		Dataset<Row> minor = null;
		List<String> blessed = new ArrayList<String>();
		if (nodes.get(0).isInstance(DoubleNode.class)) {
			df = spark.createDataFrame(nodes, DoubleNode.class);
			minor = df.filter("value>"+threshold);
		}
		
		return minor;
	}
	
	private boolean testNodeLink(String id) {
		boolean erasable = true;
		Dataset<Row> dstId = gf.edges().filter("dst="+id).cache();
		if (dstId.filter("type='link'").count()>0) {
			erasable = false;
		}
		if (dstId.filter("type='revision'").count()<1) {
			erasable = false;
		}
		System.out.println("danach");
		return erasable;
	}
	
	public void filterNodes(List<StringPair> filters) {
		Dataset<Row> minorNodes = getMergedMinorNodes(filters);
		Dataset<Row> remainingNodes = dfNodes.select("id").except(minorNodes);
		
		System.out.println("NODES: "+dfNodes.count()+" MINUS "+minorNodes.count()+" IS "+remainingNodes.count());
	}
	
	public Dataset<Row> getMergedMinorNodes(List<StringPair> filters) {
		StructType schema = DataTypes.createStructType(new StructField[] {DataTypes.createStructField("id", DataTypes.StringType, false)});
		Dataset<Row> mergedNodes = spark.createDataFrame(new ArrayList<Row>(),schema);

		for(StringPair s : filters) {
			mergedNodes = mergedNodes.union(getMinorNodes(s.getS1(), s.getS2()).select("id"));
//			mergedNodes.collect();
			mergedNodes.cache();
		}
		mergedNodes = mergedNodes.distinct();
		return mergedNodes;
	}
	
	public static Dataset<Row> getMinorNodes(String data, Object threshold) {
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
	
	private static ArrayList<BasicNode> deserialize(String filter) {
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
	
	private static ArrayList<Node> deserializeNodes(String pathToFolder) {
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
	
	private static ArrayList<Edge> deserializeEdges(String pathToFolder) {
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
