package info.collide.nlpwikinetgen.helper;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.graphx.EdgeRDD;
import org.apache.spark.graphx.VertexRDD;

public class RDDBuilder {
	
	JavaSparkContext sc = new JavaSparkContext();
	
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
