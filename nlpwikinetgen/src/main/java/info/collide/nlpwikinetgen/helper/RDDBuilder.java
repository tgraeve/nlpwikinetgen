package info.collide.nlpwikinetgen.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;

import info.collide.nlpwikinetgen.type.BasicNode;
import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class RDDBuilder {
	
	SparkConf conf = new SparkConf().setAppName("NLPWikiNetGen").setMaster("local");
	JavaSparkContext sc = new JavaSparkContext(conf);
	
	public void nodesToRDD(String source) {
		File file = new File("data/"+ source.split("\\.")[0] + "RDD");
		ArrayList<BasicNode> nodes = deserialize(source);

		JavaRDD<BasicNode> nodesRDD = sc.parallelize(nodes);
		
		if (file.exists() && file.isDirectory()) {
			deleteDir(file);
		}
		nodesRDD.saveAsTextFile("data/"+ source.split("\\.")[0] + "RDD");
	}
	
	private ArrayList<BasicNode> deserialize(String source) {
		FileInputStream fis;
		ArrayList<BasicNode> nodes = null;
		try {
			fis = new FileInputStream("data/" + source);
			ObjectInputStream ois = new ObjectInputStream(fis);
	        nodes = (ArrayList<BasicNode>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return nodes;
	}
	
	private void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	
	
	
//	Graph<Node, String> wikiGraph = new Graph<Node,String>();
//	
//	public EdgeRDD edgesToRDD(List<Edge> edges) {
//		
//		JavaRDD<Edge> rdd = sc.parallelize(edges);
//		EdgeRDD erdd = EdgeRDD.fromEdges(arg0, arg1, arg2);
//		return erdd;
//	}
//	
//	public VertexRDD<Node> nodesToRDD(List<Node> nodes) {
//		JavaRDD<Node> rdd = sc.parallelize(nodes);
//		VertexRDD vrdd = VertexRDD.fro
//		
//		return 
//	}

}
