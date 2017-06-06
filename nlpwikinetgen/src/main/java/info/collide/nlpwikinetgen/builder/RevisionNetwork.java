package info.collide.nlpwikinetgen.builder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

import info.collide.nlpwikinetgen.helper.*;
import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class RevisionNetwork implements GraphDataComponent {
	private Wikipedia wiki;
	private RevisionApi revApi;
	private String path;
	
	private String pageId;
	private String title;
	private String prevId = "";
	
	List<Node> nodes;
	List<Edge> edges;
	List<String> linkList;
	
	public RevisionNetwork(RevisionApi revApi) {
		this.revApi = revApi;
	}
	
	public RevisionNetwork(Wikipedia wiki, RevisionApi revApi, String path) {
		this.wiki = wiki;
		this.revApi = revApi;
		this.path = path;
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}
	
	public void nextPage(String pageId, String title) {
		this.pageId = pageId;
		this.title = title;
		
		linkList = new LinkedList<String>();
	}
	
	public void nextRevision(String revisionId, String text, Timestamp t) {
		// add basic node for revision, due to retrieval of follower first in second round
		nodes.add(new Node(pageId, revisionId));
		if (prevId != "") {
			// add basic edges between revisions of same page
			edges.add(new Edge(prevId, revisionId, "revision"));
		}
		prevId = revisionId;
		
		
		// add edges for links between pages
		List<String> newLinks = parseAndCompareLinks(title,text,linkList);
		
		for(String link : newLinks) {
			if(!linkList.contains(link.toLowerCase())) {
    			try {
    				if(wiki.getPage(link) != null) {
	        			int targetPageId = wiki.getPage(link).getPageId();
//	        			if(knownArticles.contains(targetPageId)) { //due to problem that no revisions for page existent
	        				List<Timestamp> ts = revApi.getRevisionTimestampsBetweenTimestamps(targetPageId, revApi.getFirstDateOfAppearance(targetPageId), t);
		        			if(ts.size() > 0) {
		        				linkList.add(link.toLowerCase());
			        			edges.add(new Edge(Integer.toString(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID()), revisionId, "link"));
		        			}
//	        			}
        			}
				} catch (Exception e) {
					// TODO: handle exception
				}
    		}
		}
	}
	
	public Object close() {
        //Serialize nodes and edges
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(path+"/nodes.tmp");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(nodes);
	        fos = new FileOutputStream(path+"/edges.tmp");
	        oos = new ObjectOutputStream(fos);
	        oos.writeObject(edges);
	        oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
//	@Deprecated
//	public void buildNetwork(Wikipedia wiki, Iterable<Page> pages, long pageAmount) throws IOException, WikiApiException {
//        // initiate GMLWriter
//        GMLWriter writer = new GMLWriter(System.getProperty("user.dir")+"/data/dag.gml");
//        
//        List<Node> nodes = new ArrayList<Node>();
//        List<Edge> edges = new ArrayList<Edge>();
////        Set<Integer> knownArticles = cat.getArticleIds();
//        
//        System.out.println("Start building network...");
//        int pagecounter = 0;
//        
//        //iterating over all pages included in given category
//        for(Page page : pages) {
//        	int revisionId = -1;
//        	int prevId = -1;
//        	String name = page.getTitle().toString();
//        	linkList = new LinkedList<String>();
//        	int pageId = page.getPageId();
//        	pagecounter++;
//        	
//        	//Get all revisions of an article
//        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
//        	if(!revisionTimeStamps.isEmpty()) {
//        		System.out.println("Page '" + page.getTitle() + "' (" + page.getPageId() + ") has "+ revisionTimeStamps.size() + " revisions to index.");
//	        	for(Timestamp t : revisionTimeStamps) {
//	        		Revision rev = revApi.getRevision(pageId, t);
//	        		revisionId = rev.getRevisionID();
//	        		String text = rev.getRevisionText();
//	        		
//	        		if(prevId!=-1) {
//	        			// add basic node for revision, due to retrieval of follower first in second round
//		        		nodes.add(new Node(prevId, pageId, revisionId));
//		        		// add basic edges between revisions of same page
//	        			edges.add(new Edge(prevId,revisionId, "revision"));
//	        		}
//	        		prevId = revisionId;
//	        		
//	        		// add edges for links between pages
//	        		List<String> newLinks = parseAndCompareLinks(name,text,linkList);
//	        		
//	        		for(String link : newLinks) {
//	        			if(!linkList.contains(link.toLowerCase())) {
//		        			try {
//		        				if(wiki.getPage(link) != null) {
//				        			int targetPageId = wiki.getPage(link).getPageId();
////				        			if(knownArticles.contains(targetPageId)) { //due to problem that no revisions for page existent
//				        				List<Timestamp> ts = revApi.getRevisionTimestampsBetweenTimestamps(targetPageId, revApi.getFirstDateOfAppearance(targetPageId), t);
//					        			if(ts.size() > 0) {
//					        				linkList.add(link.toLowerCase());
//						        			edges.add(new Edge(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID(), revisionId, "link"));
//					        			}
////				        			}
//			        			}
//							} catch (Exception e) {
//								// TODO: handle exception
//							}
//		        		}
//	        		}
//	        	}
//	        	//adds last node in revision line of page
//	        	if(revisionId==prevId) {
//	        		nodes.add(new Node(revisionId, pageId));
//	        	}
//        	}
//        	System.out.println("Indexed page " +pagecounter+ " of " +pageAmount+ " with ID: " +pageId + " successfully.");
//        }
//        
//        //Serialize nodes and edges
//        FileOutputStream fos = new FileOutputStream("data/nodes.tmp");
//        ObjectOutputStream oos = new ObjectOutputStream(fos);
//        oos.writeObject(nodes);
//        fos = new FileOutputStream("data/edges.tmp");
//        oos = new ObjectOutputStream(fos);
//        oos.writeObject(edges);
//        oos.close();
//        
////        writer.writeFile(nodes, edges); //TODO adapt new node configuration
//	}
	
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

	@Override
	public void setDescr(String descr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDescr() {
		// TODO Auto-generated method stub
		return null;
	}

}
