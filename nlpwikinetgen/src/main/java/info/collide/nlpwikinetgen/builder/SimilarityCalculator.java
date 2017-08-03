package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.api.TextSimilarityMeasure;
import info.collide.nlpwikinetgen.type.DoubleNode;

/**
 * Uses {@link TextSimilarityMeasure} to calculate similarity between two revisions.
 * 
 * Calculates similarity with given measure of revisions n and n-1 and saves it to
 * a {@link DoubleNode}.
 * 
 * @author Tobias Graeve
 *
 */
public class SimilarityCalculator extends GraphDataAnalyzer {
	
	private List<DoubleNode> nodes;
	private String prevText;
	private TextSimilarityMeasure tsm;
	
	/**
	 * @param revApi Instance of {@link RevisionApi}
	 * @param tsm Instance of {@link TextSimilarityMeasure}
	 */
	public SimilarityCalculator(RevisionApi revApi, TextSimilarityMeasure tsm) {
		super(revApi);
		this.tsm = tsm;
		nodes = new ArrayList<DoubleNode>();
	}
	
	/**
	 * @param revApi Instance of {@link RevisionApi}.
	 * @param tsm Instance of {@link TextSimilarityMeasure}.
	 * @param descr Unique identifier of module.
	 * @param path Path to output folder.
	 */
	public SimilarityCalculator(RevisionApi revApi, TextSimilarityMeasure tsm, String descr, String path) {
		super(revApi);
		this.tsm = tsm;
		setDescr(descr);
		setPath(path);
		nodes = new ArrayList<DoubleNode>();
	}
	
	@Override
	public void nextPage(String pageId, String title) throws Exception {
		setTitle(title);
		prevText = "";
	}

	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception {
		String[] tk1 = prevText.split(" ");
		String[] tk2 = text.split(" ");
		
		try {
			double score = tsm.getSimilarity(tk1, tk2);
			nodes.add(new DoubleNode(revisionId, score));
		} catch (SimilarityException e) {
			System.out.println("Failed calculating similarity measure.");
			e.printStackTrace();
		}
		prevText = text;
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

	public Object clone() {
		SimilarityCalculator sc = new SimilarityCalculator(revApi, tsm, getDescr(), getPath());
		return sc;
	}
}