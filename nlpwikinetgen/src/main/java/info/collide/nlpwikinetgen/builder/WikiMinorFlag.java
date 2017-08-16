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
public class WikiMinorFlag extends WikidataAnalyzer {

	private List<BoolNode> nodes;
	
	/**
	 * @param revApi Instance of {@link RevisionApi}.
	 */
	public WikiMinorFlag(RevisionApi revApi) {
		super(revApi);
		setDescr("Wiki_Minor_Flag");
		this.nodes = new ArrayList<BoolNode>();
	}

	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception {
		nodes.add(new BoolNode(revisionId, revApi.getRevision(Integer.parseInt(revisionId)).isMinor()));
	}

	@Override
	public void close() {
		//Serialize nodes and edges
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(getPath()+"/"+getDescr()+"_"+getTitle()+".filter");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(nodes);
	        oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Object clone() {
		WikiMinorFlag wmf = new WikiMinorFlag(revApi);
		return wmf;
	}
}