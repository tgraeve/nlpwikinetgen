package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.type.BoolNode;

/**
 * Module that retrieves the internal minor flag out of mediawiki data.
 * 
 * Uses revision data of {@link Revision}.
 * 
 * @author Tobias Graeve
 *
 */
public class WikiMinorFlag implements GraphDataComponent {
	String descr = "Wiki_Minor_Flag";
	RevisionApi revApi;
	List<BoolNode> nodes;
	String path;
	String title;
	
	/**
	 * @param revApi Instance of {@link RevisionApi}.
	 */
	public WikiMinorFlag(RevisionApi revApi) {
		this.revApi = revApi;
		this.nodes = new ArrayList<BoolNode>();
	}
	
	/**
	 * @param revApi Instance of {@link RevisionApi}.
	 * @param path Path to output folder.
	 */
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
	public void close() {
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
	}

	@Override
	public void setDescr(String descr) {
		this.descr = descr;
		
	}

	@Override
	public String getDescr() {
		return descr;
	}
	
	@Override
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