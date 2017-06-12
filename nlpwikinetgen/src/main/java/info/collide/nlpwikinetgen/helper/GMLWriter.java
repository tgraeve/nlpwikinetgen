package info.collide.nlpwikinetgen.helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class GMLWriter {
	
	BufferedWriter writer;

	public GMLWriter() throws IOException {
    	writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/output/dag.gml"));
	}
	
	public GMLWriter(String outputDir) throws IOException {
    	writer = new BufferedWriter(new FileWriter(outputDir+"/filteredGraph.gml"));
	}
	
	public void writeFile(List<Node> nodes, List<Edge> edges) throws IOException {
		// start gml file
        writer.write("graph [");
        writer.newLine();
        writer.write("directed 1");
        writer.newLine();
        
        // write nodes
        for(Node v : nodes) {
        	writer.write("\tnode [");
            writer.newLine();
            writer.write("\t\tid "+v.getId());
            writer.newLine();
            writer.write("\t\tpageid "+v.getPageId());
            writer.newLine();
            writer.write("\t]");
            writer.newLine();
        }
        for(Edge e : edges) {
        	writer.write("\tedge [");
        	writer.newLine();
        	writer.write("\t\ttype \""+e.getType()+"\"");
        	writer.newLine();
        	writer.write("\t\tsource "+e.getSrc());
        	writer.newLine();
        	writer.write("\t\ttarget "+e.getDst());
        	writer.newLine();
        	writer.write("\t]");
        	writer.newLine();
        }
        
        writer.flush();
        writer.close();
	}

}
