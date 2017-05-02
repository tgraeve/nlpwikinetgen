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
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import de.fau.cs.osr.ptk.common.AstVisitor;
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
import de.tudarmstadt.ukp.wikipedia.api.sweble.PlainTextConverter;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;
import dkpro.ChunkTagChanger;
import dkpro.annotator.SpotlightAnnotator;
import dkpro.type.Concept;
import info.collide.nlpwikinetgen.helper.*;

public class RevisionConcepts {

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
      String catName = "German_beer_culture";
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
          cat = wiki.getCategory(catName);
      } catch (WikiPageNotFoundException e) {
          throw new WikiApiException("Category " + catName + " does not exist");
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
        PreparedStatement insertConcept = conn.prepareStatement("INSERT INTO revision_concepts VALUES (?,'?')");
        
        
        //initialize dkpro pipeline components
        JCas jcas = JCasFactory.createJCas();
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(createEngineDescription(createEngineDescription(BreakIteratorSegmenter.class),
																	createEngineDescription(OpenNlpPosTagger.class),
																	createEngineDescription(StanfordLemmatizer.class),
																	createEngineDescription(OpenNlpChunker.class),
																	createEngineDescription(ChunkTagChanger.class),
																	createEngineDescription(SpotlightAnnotator.class),
																	createEngineDescription(CasDumpWriter.class, CasDumpWriter.PARAM_OUTPUT_FILE, "output/CASout.txt")));

		
		for(Page page : cat.getArticles()) {
        	System.out.println(page.getTitle().toString());
		}
        
        //iterating over all pages included in given category
        for(Page page : cat.getArticles()) {
        	int pageId = page.getPageId();
        	String title = page.getTitle().toString();
        	
        	System.out.println("##############ACTUAL PAGE:"+title+"###################");
        	
        	//Get all revisions of the article
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
        	if(!revisionTimeStamps.isEmpty()) {
        		
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(pageId, t);
	        		int revisionId = rev.getRevisionID();
	        		String text = rev.getRevisionText();
	        		String plaintext = getPlainText(title, text);
	        		
	        		System.out.println(t.toLocalDateTime());
	        		System.out.println(revisionId);
	        		
	        		if(!plaintext.isEmpty()) {
	        			//process dkpro
		        		jcas.reset();
		        		jcas.setDocumentText(plaintext);
		        		jcas.setDocumentLanguage("en");
		        		engine.process(jcas);
		        		
		        		for(Concept concept : JCasUtil.select(jcas, Concept.class))
		        		{
		        			//System.out.println("KONZEPT: "+ concept);
		        			try {
		        				insertConcept.setInt(1, revisionId);
		        				insertConcept.setString(2, concept.getLabel());
		        				insertConcept.executeUpdate();
							} catch (SQLException e) {
								System.out.println("Skipping Concept - already exists");
							}
		        		}
	        		}
	        	}
        	}
        }
        if(insertConcept != null) {
        	insertConcept.close();
        }
        conn.close();
	}
	
	
	
	
	
	
	/**
	 * <p>Returns the Wikipedia article as plain text using the SwebleParser with
	 * a SimpleWikiConfiguration and the PlainTextConverter. <br/>
	 * If you have different needs regarding the plain text, you can use
	 * getParsedPage(Visitor v) and provide your own Sweble-Visitor. Examples
	 * are in the <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package
	 * or on http://www.sweble.org </p>
	 *
	 * <p>Alternatively, use Page.getText() to return the Wikipedia article
	 * with all Wiki markup. You can then use the old JWPL MediaWiki parser for
	 * creating a plain text version. The JWPL parser is now located in a
	 * separate project <code>de.tudarmstad.ukp.wikipedia.parser</code>.
	 * Please refer to the JWPL Google Code project page for further reference.</p>
	 *
	 * @return The plain text of a Wikipedia article
	 * @throws WikiApiException
	 */
	public static String getPlainText(String title, String text)
		throws WikiApiException
	{
		//Configure the PlainTextConverter for plain text parsing
		return (String) parsePage(new PlainTextConverter(), title, text);
	}

	/**
	 * Parses the page with the Sweble parser using a SimpleWikiConfiguration
	 * and the provided visitor. For further information about the visitor
	 * concept, look at the examples in the
	 * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package, or on
	 * <code>http://www.sweble.org</code> or on the JWPL Google Code project
	 * page.
	 *
	 * @return the parsed page. The actual return type depends on the provided
	 *         visitor. You have to cast the return type according to the return
	 *         type of the go() method of your visitor.
	 * @throws WikiApiException
	 */
	public static Object parsePage(AstVisitor v, String title, String text) throws WikiApiException
	{
		// Use the provided visitor to parse the page
		return v.go(getCompiledPage(title, text).getPage());
	}

	/**
	 * Returns CompiledPage produced by the SWEBLE parser using the
	 * SimpleWikiConfiguration.
	 *
	 * @return the parsed page
	 * @throws WikiApiException
	 */
	public static CompiledPage getCompiledPage(String title, String text) throws WikiApiException
	{
		CompiledPage cp;
		try{
			SimpleWikiConfiguration config = new SimpleWikiConfiguration("classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");

			PageTitle pageTitle = PageTitle.make(config, title);
			PageId pageId = new PageId(pageTitle, -1);

			// Compile the retrieved page
			Compiler compiler = new Compiler(config);
			cp = compiler.postprocess(pageId, text, null);
		}catch(Exception e){
			throw new WikiApiException(e);
		}
		return cp;
	}

}
