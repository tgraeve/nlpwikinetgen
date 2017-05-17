package info.collide.nlpwikinetgen.builder;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

import info.collide.nlpwikinetgen.helper.*;
import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class RevisionNetwork {

	public static void main(String[] args) throws IOException, WikiApiException, SQLException {
		
		
		// configure the database connection parameters								//ENGLISH
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

      // Create a new english wikipedia.
      Wikipedia wiki = new Wikipedia(dbConfig);
      RevisionApi revApi = new RevisionApi(dbConfig) ;

      // Select page
      String title = "German_beer_culture";
      Category cat = wiki.getCategory(title);
		
		
		
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
        GMLWriter writer = new GMLWriter(System.getProperty("user.dir")+"/output/complete_dag.gml");
        
        List<Node> nodes = new ArrayList<Node>();
        List<Edge> edges = new ArrayList<Edge>();
//        Set<Integer> knownArticles = cat.getArticleIds();
        
        //iterating over all pages included in given category
        for(Page page : cat.getArticles()) {
        	int revisionId = -1;
        	int prevId = -1;
        	String name = page.getTitle().toString();
        	List<String> linkList = new LinkedList<String>();
        	int pageId = page.getPageId();
        	
        	//Get all revisions of an article
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
        	if(!revisionTimeStamps.isEmpty()) {
        		
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(pageId, t);
	        		revisionId = rev.getRevisionID();
	        		String text = rev.getRevisionText();
	        		
//	        		TextSimilarityMeasure ms = new WordNGramJaccardMeasure(3);
//	        		
//	        		String[] tk1 = prevText.split(" ");
//	        		String[] tk2 = text.split(" ");
//	        		
//	        		double score = ms.getSimilarity(tk1, tk2);
	        		
//	        		prevText = text;
	        		
	        		if(prevId!=-1) {
	        			// add basic node for revision, due to retrieval of follower first in second round
		        		nodes.add(new Node(prevId, pageId, revisionId));
		        		// add basic edges between revisions of same page
	        			edges.add(new Edge("revision", prevId,revisionId));
	        		}
	        		prevId = revisionId;
	        		
	        		// add edges for links between pages
	        		List<String> newLinks = parseAndCompareLinks(name,text,linkList);
	        		
	        		for(String link : newLinks) {
	        			if(!linkList.contains(link.toLowerCase())) {
		        			try {
		        				if(wiki.getPage(link) != null) {
				        			int targetPageId = wiki.getPage(link).getPageId();
//				        			if(knownArticles.contains(targetPageId)) { //due to problem that no revisions for page existent
				        				List<Timestamp> ts = revApi.getRevisionTimestampsBetweenTimestamps(targetPageId, revApi.getFirstDateOfAppearance(targetPageId), t);
					        			if(ts.size() > 0) {
					        				linkList.add(link.toLowerCase());
						        			edges.add(new Edge("link", revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID(), revisionId));
						        			System.out.println(wiki.getPage(revApi.getPageIdForRevisionId(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID())).getTitle()+" #TO# "+revisionId);
					        			}
//				        			}
			        			}
							} catch (Exception e) {
								// TODO: handle exception
							}
		        		}
	        		}
	        	}
	        	//adds last node in revision line of page
	        	if(revisionId==prevId) {
	        		nodes.add(new Node(revisionId, pageId));
	        	}
        	}
        }
        
        //Serialize nodes and edges
        FileOutputStream fos = new FileOutputStream("nodes.tmp");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(nodes);
        fos = new FileOutputStream("edges.tmp");
        oos = new ObjectOutputStream(fos);
        oos.writeObject(edges);
        oos.close();
        
//        writer.writeFile(nodes, edges); //TODO adapt new node configuration
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
