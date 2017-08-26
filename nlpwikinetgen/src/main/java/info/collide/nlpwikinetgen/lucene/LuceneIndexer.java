package info.collide.nlpwikinetgen.lucene;

import java.io.IOException;
import java.sql.Timestamp;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.builder.WikidataAnalyzer;

public class LuceneIndexer extends WikidataAnalyzer {
	
	DatabaseConfiguration dbConfig;
	Wikipedia wiki;
	IndexWriter indexWriter;
	Document doc;
	Field revId;
	Field article;
	
	Directory directory;
	
	String descr;
	
	public LuceneIndexer(IndexWriter indexWriter, RevisionApi revApi) throws WikiApiException {
		super(revApi);
		this.indexWriter = indexWriter;
		
		//instantiate one time due to performance
		doc = new Document();
		revId = new StringField("revisionId", "", Store.YES);
    	doc.add(revId);
    	article = new TextField("text", "", Store.NO);
    	doc.add(article);
	}
	
	@Override
	public void nextPage(String pageId, String title) {
	}
	
	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) {
		index(indexWriter, revisionId, text);
	}
	
	@Override
	public void close() {
		try {
			indexWriter.commit();
		} catch (IOException e) {
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
	
	private void index(IndexWriter writer, String revisionId, String text) {
    	
		revId.setStringValue(revisionId);
	    article.setStringValue(text);
	    
	    System.out.println(revisionId);
    	
    	//try/catch just backup for filter in analyzer. terms may not be too long.
    	try {
    		writer.updateDocument(new Term("revisionId",revisionId), doc);
		} catch (IllegalArgumentException e) {
			System.out.println("Maybe term is too long.");
		} catch (IOException e) {
			System.out.println("Error updating index.");
			e.printStackTrace();
		}
    }

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}
}