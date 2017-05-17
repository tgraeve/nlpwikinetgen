package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import dkpro.similarity.algorithms.api.SimilarityException;
import dkpro.similarity.algorithms.api.TextSimilarityMeasure;
import dkpro.similarity.algorithms.lexical.ngrams.WordNGramJaccardMeasure;
import info.collide.nlpwikinetgen.type.DoubleNode;

public class SimilarityCalculator {
	
	DatabaseConfiguration dbConfig;
	Wikipedia wiki;
	RevisionApi revApi;
	IndexWriter indexWriter;
	Document doc;
	Field revId;
	Field article;
	
	public SimilarityCalculator(RevisionApi revApi) {
		this.revApi = revApi;
	}
	
	public void calcSimilarity(Iterable<Page> pages, int pageAmount) {
		int pagecounter = 0;
		
		List<DoubleNode> nodes = new ArrayList<DoubleNode>();
	
		System.out.println("Start calculating similarity...");
		
		for(Page page : pages) {
        	int pageId = page.getPageId(); 
        	int revisionId;
        	String prevText = "";
        	pagecounter++;
        	
        	Collection<Timestamp> revisionTimeStamps;
			try {
				revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
				if(!revisionTimeStamps.isEmpty()) {
            		System.out.println("Page '" + page.getTitle() + "' (" + page.getPageId() + ") has "+ revisionTimeStamps.size() + " revisions to index.");
    	        	for(Timestamp t : revisionTimeStamps) {
    	        		Revision rev = revApi.getRevision(pageId, t);
    	        		revisionId = rev.getRevisionID();
    	        		String text = rev.getRevisionText();
    	        		
    	        		TextSimilarityMeasure ms = new WordNGramJaccardMeasure(3);
 	        		
    	        		String[] tk1 = prevText.split(" ");
    	        		String[] tk2 = text.split(" ");
   	        		
    	        		try {
							double score = ms.getSimilarity(tk1, tk2);
							nodes.add(new DoubleNode(revisionId, score));
						} catch (SimilarityException e) {
							System.out.println("Failed calculating similarity measure.");
							e.printStackTrace();
						}
    	        		prevText = text;
    	        	}
            	}
	        	
	        	System.out.println("Calculated page " +pagecounter+ " of " +pageAmount+ " with ID: " +pageId + " successfully.");
			} catch (WikiApiException e) {
				System.out.println("Failed accessing JWPL API. Check database.");
				e.printStackTrace();
			}
		}
		
		//Serialize nodes
        FileOutputStream fos;
		try {
			fos = new FileOutputStream("similarityNodes.tmp");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(nodes);
	        oos.close();
		} catch (Exception e) {
			System.out.println("Failed serializing nodes. Please retry.");
			e.printStackTrace();
		} 
	}
}