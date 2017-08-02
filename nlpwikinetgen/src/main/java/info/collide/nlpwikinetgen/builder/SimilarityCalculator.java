package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.api.TextSimilarityMeasure;
import info.collide.nlpwikinetgen.type.DoubleNode;

public class SimilarityCalculator implements GraphDataComponent {
	
	DatabaseConfiguration dbConfig;
	Wikipedia wiki;
	RevisionApi revApi;
	IndexWriter indexWriter;
	String path;
	Document doc;
	Field revId;
	Field article;
	private List<DoubleNode> nodes;
	private String prevText;
	private String pageId;
	private String title;
	private TextSimilarityMeasure tsm;
	String descr;
	
	public SimilarityCalculator(RevisionApi revApi, TextSimilarityMeasure tsm) {
		this.revApi = revApi;
		this.tsm = tsm;
		nodes = new ArrayList<DoubleNode>();
	}
	
	public SimilarityCalculator(RevisionApi revApi, TextSimilarityMeasure tsm, String descr, String path) {
		this.revApi = revApi;
		this.tsm = tsm;
		this.descr = descr;
		this.path = path;
		nodes = new ArrayList<DoubleNode>();
	}
	

	@Override
	public void nextPage(String pageId, String title) throws Exception {
		this.title = title;
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
	public List<DoubleNode> close() {
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
		SimilarityCalculator sc = new SimilarityCalculator(revApi, tsm, descr, path);
		return sc;
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