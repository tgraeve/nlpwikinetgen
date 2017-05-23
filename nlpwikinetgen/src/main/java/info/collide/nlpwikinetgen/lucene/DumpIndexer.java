package info.collide.nlpwikinetgen.lucene;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

public class DumpIndexer {
	
	DatabaseConfiguration dbConfig;
	Wikipedia wiki;
	RevisionApi revApi;
	IndexWriter indexWriter;
	Document doc;
	Field revId;
	Field article;
	
	public DumpIndexer(RevisionApi revApi) throws WikiApiException {
		this.revApi = revApi;
		
		//instantiate one time due to performance
		doc = new Document();
		revId = new StringField("revisionId", "", Store.YES);
    	doc.add(revId);
    	article = new TextField("text", "", Store.NO);
    	doc.add(article);
	}
	
	public void indexWiki(Iterable<Page> pages, int pageAmount) {
		
		int pagecounter = 0;
		
		//set lucene config
        Directory directory = null;
        Analyzer analyzer = new WikiAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        
		try {
			directory = FSDirectory.open(new File(System.getProperty("user.dir")+"/data/lucene/").toPath());
			indexWriter = new IndexWriter(directory , config);
		} catch (IOException e) {
			System.out.println("Indexer failed while initializing the index writer.");
			e.printStackTrace();
		}
		
		System.out.println("Start indexing...");
		
		try {
			for(Page page : pages) {
	        	int pageId = page.getPageId(); 
	        	pagecounter++;
	        	
	        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
            	if(!revisionTimeStamps.isEmpty()) {
            		System.out.println("Page '" + page.getTitle() + "' (" + page.getPageId() + ") has "+ revisionTimeStamps.size() + " revisions to index.");
    	        	for(Timestamp t : revisionTimeStamps) {
    	        		Revision rev = revApi.getRevision(pageId, t);
    	        		int revisionId = rev.getRevisionID();
    	        		
    	        		String text = rev.getRevisionText();
    	        		System.out.println(revisionId);
    	        		index(indexWriter, revisionId, text);
    	        	}
            	}
	        	
	        	System.out.println("Indexed page " +pagecounter+ " of " +pageAmount+ " with ID: " +pageId + " successfully.");
	        	indexWriter.commit();
	        }
		} catch (Exception e) {
			System.out.println("Indexer failed while accessing JWPL DB.");
		} finally {
	        try {
				indexWriter.close();
				directory.close();
			} catch (IOException e) {
				System.out.println("Failed closing index writer.");
				e.printStackTrace();
			}
		}	
	}
	
	@Deprecated
	public void indexWiki(String category) {
		Category cat = null;
		int pageamount = 0;
		try {
			cat = wiki.getCategory(category);
			pageamount = cat.getNumberOfPages();
		} catch (WikiApiException e1) {
			System.out.println("Category does not exist. Check spelling. Capital sensitive!");
			e1.printStackTrace();
		}
		
		int pagecounter = 0;
		String progress = null;
		
		//set lucene config
        Directory directory = null;
        Analyzer analyzer = new WikiAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        
		try {
			directory = FSDirectory.open(new File(System.getProperty("user.dir")+"/lucene/" + wiki.getDatabaseConfiguration().getDatabase() +"_"+category).toPath());
			indexWriter = new IndexWriter(directory , config);
		} catch (IOException e) {
			System.out.println("Indexer failed while initializing the index writer.");
			e.printStackTrace();
		}
		
		System.out.println("Start indexing...");
		
		try {
			if (cat!=null) {
				for (Page page : cat.getArticles()) {
					int pageId = page.getPageId();
					pagecounter++;

					Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
					if (!revisionTimeStamps.isEmpty()) {
						System.out.println("Page '" + page.getTitle() + "' (" + page.getPageId() + ") has "
								+ revisionTimeStamps.size() + " revisions to index.");
						for (Timestamp t : revisionTimeStamps) {
							Revision rev = revApi.getRevision(pageId, t);
							int revisionId = rev.getRevisionID();

							String text = rev.getRevisionText();
//							System.out.println(revisionId);
							index(indexWriter, revisionId, text);
						}
					}

					System.out.println("Indexed page " + pagecounter + " of " + pageamount + " with ID: " + pageId + " successfully.");
					indexWriter.commit();
				} 
			}
		} catch (Exception e) {
			System.out.println("Indexer failed while accessing JWPL DB.");
		} finally {
	        try {
				indexWriter.close();
				directory.close();
			} catch (IOException e) {
				System.out.println("Failed closing index writer.");
				e.printStackTrace();
			}
		}	
	}
	
	
	private void index(IndexWriter writer, int revisionId, String text) throws IOException {
    	
		revId.setStringValue(Integer.toString(revisionId));
	    article.setStringValue(text);
    	
    	//try/catch just backup for filter in analyzer. terms may not be too long.
    	try {
    		writer.updateDocument(new Term("revisionId",Integer.toString(revisionId)), doc);
//    		writer.addDocument(doc);
		} catch (IllegalArgumentException e) {
			System.out.println("Maybe term too long.");
		}
    }
}