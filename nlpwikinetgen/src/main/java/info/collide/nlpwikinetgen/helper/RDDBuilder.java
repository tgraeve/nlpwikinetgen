package info.collide.nlpwikinetgen.helper;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.graphx.EdgeRDD;
import org.apache.spark.graphx.Graph;
import org.apache.spark.graphx.VertexRDD;

import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class RDDBuilder {
	
	SparkConf conf = new SparkConf().setAppName("NLPWikiNetGen").setMaster("local");
	JavaSparkContext sc = new JavaSparkContext(conf);
	
	Graph<(int, String), String> wikiGraph = new Graph<(int,String),String>();
	
	public EdgeRDD edgesToRDD(List<Edge> edges) {
		
		JavaRDD<Edge> rdd = sc.parallelize(edges);
		EdgeRDD erdd = EdgeRDD.fromEdges(arg0, arg1, arg2);
		return erdd;
	}
	
	public VertexRDD<Node> nodesToRDD(List<Node> nodes) {
		JavaRDD<Node> rdd = sc.parallelize(nodes);
		VertexRDD vrdd = VertexRDD.fro
		
		return 
	}

}
