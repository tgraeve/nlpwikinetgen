package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.type.BoolNode;

public class WikiMinorFlag implements GraphDataComponent {
	String descr = "Wiki_Minor_Flag";
	RevisionApi revApi;
	List<BoolNode> nodes;
	String path;
	String title;
	
	public WikiMinorFlag(RevisionApi revApi) {
		this.revApi = revApi;
		this.nodes = new ArrayList<BoolNode>();
	}
	
	public WikiMinorFlag(RevisionApi revApi, String path) {
		this.revApi = revApi;
		this.path = path;
		this.nodes = new ArrayList<BoolNode>();
	}

	@Override
	public void nextPage(String pageId, String title) throws Exception {
		this.title = title;
	}

	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception {
		nodes.add(new BoolNode(revisionId, revApi.getRevision(Integer.parseInt(revisionId)).isMinor()));
	}

	@Override
	public Object close() {
		//Serialize nodes and edges
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(path+"/"+descr+"_"+title+".filter");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(nodes);
	        oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setDescr(String descr) {
		this.descr = descr;
		
	}

	@Override
	public String getDescr() {
		return descr;
	}
	
	public Object clone() {
		WikiMinorFlag wmf = new WikiMinorFlag(revApi, path);
		return wmf;
	}

	@Override
	public void setOutputPath(String path) {
		this.path = path;	
	}

	@Override
	public String getOutputPath() {
		return path;
	}
}
