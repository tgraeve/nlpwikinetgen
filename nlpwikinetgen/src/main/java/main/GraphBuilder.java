package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.graphframes.GraphFrame;

import info.collide.nlpwikinetgen.type.BasicNode;
import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class GraphBuilder {
	
	public static void main(String[] args) {
		
		SparkConf conf = new SparkConf().setAppName("NLPWikiNetGen").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		SparkSession spark = SparkSession.builder()
								.appName("NLPWikiNetGen")
								.master("local")
								.getOrCreate();
		
		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		nodes = deserializeNodes();
		edges = deserializeEdges();
		
		Dataset<Row> nodesDF = spark.createDataFrame(nodes, Node.class);
		Dataset<Row> edgesDF = spark.createDataFrame(edges, Edge.class);
		
		GraphFrame gf = new GraphFrame(nodesDF, edgesDF);
		gf.inDegrees().show();
		
		spark.stop();
		
	}
	
	private static ArrayList<Node> deserializeNodes() {
		FileInputStream fis;
		ArrayList<Node> nodes = null;
		try {
			fis = new FileInputStream("data/firstGUIAttempt/nodes.tmp");
			ObjectInputStream ois = new ObjectInputStream(fis);
	        nodes = (ArrayList<Node>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return nodes;
	}
	
	private static ArrayList<Edge> deserializeEdges() {
		FileInputStream fis;
		ArrayList<Edge> edges = null;
		try {
			fis = new FileInputStream("data/firstGUIAttempt/edges.tmp");
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
