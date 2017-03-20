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

public class RevisionNetwork {

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
        
        // initiate GMLWriter
        GMLWriter writer = new GMLWriter(System.getProperty("user.dir")+"/output/complete_dag_Bier.gml");
        
        //test if category exists
        try {
            cat = wiki.getCategory(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + title + " does not exist");
        }
        
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Edge> arcs = new ArrayList<Edge>();
        Set<Integer> knownArticles = cat.getArticleIds();
        
        //iterating over all pages included in given category
        for(Page page : cat.getArticles()) {
        	int prevId = -1;
        	String name = page.getTitle().toString();
        	List<String> linkList = new LinkedList<String>();
        	int pageId = page.getPageId();
        	
        	//Get all revisions of the article
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
        	if(!revisionTimeStamps.isEmpty()) {
        		
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(pageId, t);
	        		int revisionId = rev.getRevisionID();
	        		String text = rev.getRevisionText();
	        		boolean major = !rev.isMinor();
	        		int length = text.length();
	        		
        			// add basic vertex for revision
	        		vertices.add(new Vertex(revisionId, pageId, name, major,length));
	        		
	        		System.out.println("\nVertex: "+revisionId+"++"+name+"++"+major+"++"+length);
	        		
	        		// add basic arcs between revisions of same page
	        		if(prevId!=-1) {
	        			arcs.add(new Edge("revision", prevId,revisionId));
	        		}
	        		prevId = revisionId;
	        		
	        		// add arcs for links between pages
	        		List<String> newLinks = parseAndCompareLinks(name,text,linkList);
	        		System.out.println(newLinks);
	        		
	        		for(String link : newLinks) {
	        			if(!linkList.contains(link.toLowerCase())) {
		        			System.out.println(link);
		        			try {
		        				if(wiki.getPage(link) != null) {
				        			int targetPageId = wiki.getPage(link).getPageId();
				        			System.out.println(targetPageId);
				        			if(knownArticles.contains(targetPageId)) {
				        				List<Timestamp> ts = revApi.getRevisionTimestampsBetweenTimestamps(targetPageId, revApi.getFirstDateOfAppearance(targetPageId), t);
					        			if(ts.size() > 0) {
						        			arcs.add(new Edge("link", revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID(), revisionId));
						        			System.out.println(wiki.getPage(revApi.getPageIdForRevisionId(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID())).getTitle()+" #TO# "+revisionId);
					        			}
				        			}
			        			}
							} catch (Exception e) {
								// TODO: handle exception
							}
		        		}
	        			linkList.add(link.toLowerCase());
	        		}
	        		System.out.println(linkList);
	        		//linkList.addAll(newLinks);
	        	}
        	}
        }
        
        writer.writeFile(vertices, arcs);
	}
	
	/**
     * Parse the given text, extract links from it and compare
     * those links to the known links 
     * 
     * @author goehnert
     * 
     * @param text
     * @param oldLinkList
     */
	private static List<String> parseAndCompareLinks(String pageTitle, String text, List<String> oldLinkList) {
        // extract links
		List<String> linkList = new LinkedList<String>();
        String pattern = "(\\[\\[)([^\\]]+)(\\]\\])";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher regexMatcher = regexPattern.matcher(text);
        while (regexMatcher.find()) {
            String link = regexMatcher.group().replaceAll("\\[\\[", "").replaceAll("\\]\\]", "");
            
            // cut off additional parameters
            int paramIndex = link.indexOf("|");
            if (paramIndex > -1) {
            	link = link.substring(0, paramIndex);                
            }
            // cut off jump labels within pages
            int jumpIndex = link.indexOf("#");
            if (jumpIndex > -1) {
            	link = link.substring(0, jumpIndex);                    
            }
            // outer if: exclude special links
            // inner if: complete page name where necessary
            if (!link.contains(":")) {
                if (link.startsWith("/")) {
                    link = pageTitle + link;
                }
                // translate spaces into wiki notation
                link = link.replaceAll(" ", "_");
                linkList.add(link);
            }
        }
        
        // compare links
        List<String> newLinkList = new LinkedList<String>();
        for (String link : linkList) {
            if (!oldLinkList.contains(link.toLowerCase())) {
                newLinkList.add(link);
            }
        }
        
        return newLinkList;
    }

}
