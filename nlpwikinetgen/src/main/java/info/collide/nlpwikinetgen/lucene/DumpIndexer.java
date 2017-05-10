package info.collide.nlpwikinetgen.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;

import java.sql.SQLException;

import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class DumpIndexer {

	public static void main(String[] args) throws IOException, WikiApiException, SQLException {
		
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		
		//get local config file
		BufferedReader br = null;
		FileReader fr = null;
		
		try {
			fr = new FileReader(System.getProperty("user.dir")+"/dbconf.txt");
			br = new BufferedReader(fr);
			
			String host = br.readLine();
			String db = br.readLine();
			String user = br.readLine();
			String pw = br.readLine();
			
	        dbConfig.setHost(host);
	        dbConfig.setDatabase(db);
	        dbConfig.setUser(user);
	        dbConfig.setPassword(pw);
	        dbConfig.setLanguage(Language.english);
			
		} catch (Exception e) {
			System.out.println("Config file seems to be broken");
			e.printStackTrace();
		}
		finally {
			br.close();
			fr.close();
		}
		
//		 // configure the database connection parameters
//        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
//        dbConfig.setHost("h2655337.stratoserver.net");
//        dbConfig.setDatabase("enwiki_20170111");
//        dbConfig.setUser("jwpldbadmin");
//        dbConfig.setPassword("APdJWPLDB");
//        dbConfig.setLanguage(Language.english);
        
        //set lucene config
        Directory directory = FSDirectory.open(new File(System.getProperty("user.dir")+"/lucene/enwiki_20170111").toPath());
        Analyzer analyzer = new WikiAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory , config);

        // Create a new German wikipedia.
        Wikipedia wiki = new Wikipedia(dbConfig);
        
        // Create Revision Machine tools
        RevisionApi revApi = new RevisionApi(dbConfig);

        for(Page page : wiki.getArticles()) {
        	int pageId = page.getPageId();
        	
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
        	if(!revisionTimeStamps.isEmpty()) {
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(pageId, t);
	        		int revisionId = rev.getRevisionID();
	        		
	        		String text = rev.getRevisionText();
	        		
	        		System.out.println(revisionId);
	        		
	        		index(indexWriter, revisionId, text);
	        	}
        	}
        }
        indexWriter.close();
        directory.close();
	}
	
	static void index(IndexWriter writer, int revisionId, String text) throws IOException {
    	Document doc = new Document();
    	
    	Field revId = new StoredField("revisionId", revisionId);
    	doc.add(revId);
    	
    	Field article = new TextField("text", text, Store.NO);
    	doc.add(article);
    	
    	//just backup for filter in analyzer. terms may not be too long.
    	try {
    		writer.addDocument(doc);
		} catch (IllegalArgumentException e) {
			System.out.println("Term too long.");
		}
    }
}