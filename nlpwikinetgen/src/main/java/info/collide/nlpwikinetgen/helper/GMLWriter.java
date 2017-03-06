package info.collide.nlpwikinetgen.helper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GMLWriter {
	
	BufferedWriter writer;

	public GMLWriter() throws IOException {
    	writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/output/dag.gml"));
	}
	
	public GMLWriter(String outputDir) throws IOException {
    	writer = new BufferedWriter(new FileWriter(outputDir));
	}
	
	public void writeFile(List<Vertex> vertices, List<Edge> arcs) throws IOException {
		// start gml file
        writer.write("graph [");
        writer.newLine();
        writer.write("directed 1");
        writer.newLine();
        
        // write nodes
        for(Vertex v : vertices) {
        	writer.write("\tnode [");
            writer.newLine();
            writer.write("\t\tid "+v.getId());
            writer.newLine();
            writer.write("\t\tid "+v.getPageId());
            writer.newLine();
            writer.write("\t\tlabel \""+v.getName()+"\"");
            writer.newLine();
            writer.write("\t\tisMajorEdit \""+v.isFlagged()+"\"");
            writer.newLine();
            writer.write("\t\tbyteLoad \""+v.getChangeLoad()+"\"");
            writer.newLine();
            writer.write("\t]");
            writer.newLine();
        }
        for(Edge e : arcs) {
        	writer.write("\tedge [");
        	writer.newLine();
        	writer.write("\t\tsource "+e.getType());
        	writer.newLine();
        	writer.write("\t\tsource "+e.getSource());
        	writer.newLine();
        	writer.write("\t\ttarget "+e.getDestination());
        	writer.newLine();
        	writer.write("\t]");
        	writer.newLine();
        }
        
        writer.flush();
        writer.close();
	}

}
