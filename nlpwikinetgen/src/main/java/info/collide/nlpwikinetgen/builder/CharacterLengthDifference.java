package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.type.IntNode;

/**
 * Module that calculates the length difference between two revisions.
 * 
 * It calculates the difference between the length of the sequence of characters of
 * revision n and n-1 and saves it to an {@link IntNode}.
 * 
 * @author Tobias Graeve
 * 
 *
 */
public class CharacterLengthDifference implements GraphDataComponent {
	
	RevisionApi revApi;
	List<IntNode> nodes;
	String path;
	String title;
	int prevLength;
	String descr = "Character_Length_Difference";
	
	/**
	 * 
	 * @param revApi Instance of {@link RevisionApi} 
	 */
	public CharacterLengthDifference(RevisionApi revApi) {
		this.revApi = revApi;
		this.nodes = new ArrayList<IntNode>();
	}
	
	/**
	 * 
	 * @param revApi Instance of {@link RevisionApi}.
	 * @param descr Unique identifier of module.
	 * @param path Path to output folder.
	 */
	public CharacterLengthDifference(RevisionApi revApi, String descr, String path) {
		this.revApi = revApi;
		this.descr = descr;
		this.path = path;
		this.nodes = new ArrayList<IntNode>();
	}

	@Override
	public void nextPage(String pageId, String title) throws Exception {
		this.title = title;
		this.prevLength = 0;
	}

	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception {
		int length = text.length();
		int difference = Math.abs(prevLength-length);
		nodes.add(new IntNode(revisionId, difference));
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
		CharacterLengthDifference cld = new CharacterLengthDifference(revApi, descr, path);
		return cld;
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