package info.collide.nlpwikinetgen.lucene;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.builder.GraphDataComponent;

public class DumpIndexer implements GraphDataComponent {
	
	DatabaseConfiguration dbConfig;
	Wikipedia wiki;
	RevisionApi revApi;
	IndexWriter indexWriter;
	Document doc;
	Field revId;
	Field article;
	
	Directory directory;
	
	private String outputFolder;
	private String pageId;
	private String revisionId;
	String descr;
	
	public DumpIndexer(RevisionApi revApi, String outputFolder) throws WikiApiException {
		this.revApi = revApi;
		this.outputFolder = outputFolder;
		
		//set lucene config
        Analyzer analyzer = new WikiAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        
        try {
			directory = FSDirectory.open(new File(outputFolder+"/lucene/").toPath());
			indexWriter = new IndexWriter(directory , config);
		} catch (IOException e) {
			System.out.println("Indexer failed while initializing the index writer.");
			e.printStackTrace();
		}
		
		//instantiate one time due to performance
		doc = new Document();
		revId = new StringField("revisionId", "", Store.YES);
    	doc.add(revId);
    	article = new TextField("text", "", Store.NO);
    	doc.add(article);
	}
	
	@Override
	public void nextPage(String pageId, String title) throws IOException {
		indexWriter.commit();
		this.pageId = pageId;
	}
	
	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) throws IOException {
		index(indexWriter, revisionId, text);
	}
	
	@Override
	public Object close() {
		try {
			indexWriter.close();
			directory.close();
		} catch (IOException e) {
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
	
	private void index(IndexWriter writer, String revisionId, String text) throws IOException {
    	
		revId.setStringValue(revisionId);
	    article.setStringValue(text);
    	
    	//try/catch just backup for filter in analyzer. terms may not be too long.
    	try {
    		writer.updateDocument(new Term("revisionId",revisionId), doc);
//    		writer.addDocument(doc);
		} catch (IllegalArgumentException e) {
			System.out.println("Maybe term is too long.");
		}
    }
}