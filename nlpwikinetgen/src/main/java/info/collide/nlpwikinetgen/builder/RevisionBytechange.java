package info.collide.nlpwikinetgen.builder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;
import dkpro.ChunkTagChanger;
import dkpro.annotator.SpotlightAnnotator;
import dkpro.type.Concept;
import info.collide.nlpwikinetgen.helper.*;

public class RevisionBytechange {

	public static void main(String[] args) throws IOException, WikiApiException, UIMAException, SQLException {
		
		
		// configure the database connection parameters								//ENGLISH
      DatabaseConfiguration dbConfig = new DatabaseConfiguration();
      dbConfig.setHost("h2655337.stratoserver.net");
      dbConfig.setDatabase("enwiki_20170111");
      dbConfig.setUser("jwpldbadmin");
      dbConfig.setPassword("APdJWPLDB");
      dbConfig.setLanguage(Language.english);

      // Create a new english wikipedia.
      Wikipedia wiki = new Wikipedia(dbConfig);
      RevisionIterator revIt = new RevisionIterator(dbConfig) ;
      RevisionApi revApi = new RevisionApi(dbConfig) ;

      // Select page
      String title = "German_beer_culture";
      Category cat;
		
		
		
//		// configure the database connection parameters								//GERMAN
//        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
//        dbConfig.setHost("134.91.20.26");
//        dbConfig.setDatabase("wiki_20161101");
//        dbConfig.setUser("tobias");
//        dbConfig.setPassword("password");
//        dbConfig.setLanguage(Language.german);
//
//        // Create a new German wikipedia.
//        Wikipedia wiki = new Wikipedia(dbConfig);
//        RevisionIterator revIt = new RevisionIterator(dbConfig) ;
//        RevisionApi revApi = new RevisionApi(dbConfig) ;
//
//        // Select category
//        String title = "Bierkultur";
//        Category cat;
        
        //test if category exists
        try {
            cat = wiki.getCategory(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + title + " does not exist");
        }
        
      //initiate SQL Connection
        try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			// TODO: handle exception
		}
        Connection conn = null;
        try {
			conn = DriverManager.getConnection("jdbc:mysql://h2655337.stratoserver.net:3306/enwiki_20170111?" + "user=jwpldbuser" + "&" + "password=password");
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
        PreparedStatement insertBytechange = conn.prepareStatement("INSERT INTO revision_links_bytechange VALUES (?,?,?)");
        
        //iterating over all pages included in given category
        for(Page page : cat.getArticles()) {
        	int prevLength = 0;
        	int pageId = page.getPageId();
        	insertBytechange.setInt(1, pageId);
        	
        	//Get all revisions of the article
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
        	if(!revisionTimeStamps.isEmpty()) {
        		
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(pageId, t);
	        		int revisionId = rev.getRevisionID();
	        		String text = rev.getRevisionText();
	        		int length = text.length();
	        		
	        		int bytechange = length-prevLength;
	        		prevLength = length;
	        		
	        		insertBytechange.setInt(2, revisionId);
	        		insertBytechange.setInt(3, bytechange);
	        		insertBytechange.executeUpdate();
	
	        	}
        	}
        }
        
        if(insertBytechange != null) {
        	insertBytechange.close();
        }
        conn.close();
	}
}