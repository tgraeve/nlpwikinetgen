package info.collide.nlpwikinetgen.builder;

import java.io.IOException;
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
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;
import info.collide.nlpwikinetgen.helper.*;

public class RevisionNetwork {
	
	static List<String> linkList;

	public static void main(String[] args) throws IOException, WikiApiException {
		// configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("134.91.20.26");
        dbConfig.setDatabase("wiki_20161101");
        dbConfig.setUser("tobias");
        dbConfig.setPassword("password");
        dbConfig.setLanguage(Language.german);

        // Create a new German wikipedia.
        Wikipedia wiki = new Wikipedia(dbConfig);
        RevisionIterator revIt = new RevisionIterator(dbConfig) ;
        RevisionApi revApi = new RevisionApi(dbConfig) ;

        // Select category
        String title = "Bier_als_Thema";
        Category cat;
        
        // initiate GMLWriter
        GMLWriter writer = new GMLWriter();
        
        try {
            cat = wiki.getCategory(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + title + " does not exist");
        }
        
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<StringPair> arcs = new ArrayList<StringPair>();

        for(Page page : cat.getArticles()) {
        	int prevId = -1;
        	String name = page.getTitle().toString();
        	linkList = new LinkedList<String>();
        	
        	//Get all revisions for the article
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
        	if(!revisionTimeStamps.isEmpty()) {
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(page.getPageId(), t);
	        		int revisionId = rev.getRevisionID();
	        		String text = rev.getRevisionText();
	        		boolean major = !rev.isMinor();
	        		int length = text.length();
	        		
	        		// add basic vertex for revision
	        		vertices.add(new Vertex(revisionId,name, major,length));
	        		
	        		// add basic arcs between revisions of same page
	        		if(prevId!=-1) {
	        			arcs.add(new StringPair(prevId,revisionId));
	        		}
	        		prevId = revisionId;
	        		System.out.println("\nVertex: "+revisionId+"++"+name+"++"+major+"++"+length);
	        		
	        		// add arcs for links between pages
	        		List<String> newLinks = parseAndCompareLinks(name,text,linkList);
	        		
	        		
	        		for(String link : newLinks) {
	        			System.out.println(link);
	        			if(wiki.existsPage(link)) {
		        			int targetPageId = wiki.getPage(link).getPageId();
		        			List<Timestamp> ts = revApi.getRevisionTimestampsBetweenTimestamps(targetPageId, revApi.getFirstDateOfAppearance(targetPageId), t);
		        			arcs.add(new StringPair(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID(), revisionId));
		        			System.out.println(wiki.getPage(revApi.getPageIdForRevisionId(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID())).getTitle()+" #TO# "+revisionId);
	        			}
	        		}
	        		
	        		
	        		linkList.addAll(newLinks);
	        	}
        	}
        }
        
//        writer.writeFile(vertices, arcs);

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
        linkList = new LinkedList<String>();
        String pattern = "(\\[\\[)([^\\]]+)(\\]\\])";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher regexMatcher = regexPattern.matcher(text);
        while (regexMatcher.find()) {
        	System.out.println(regexMatcher.group());
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
            if (!oldLinkList.contains(link)) {
                newLinkList.add(link);
            }
        }
        
        return newLinkList;
    }

}
