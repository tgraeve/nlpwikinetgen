package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;

public class NetworkBuilder extends GraphDataAnalyzer {
	private Wikipedia wiki;
	
	private String pageId;
	private String prevId = null;
	
	List<Node> nodes;
	List<Edge> edges;
	List<String> linkList;
	
	public NetworkBuilder(Wikipedia wiki, RevisionApi revApi, String path) {
		super(revApi);
		this.wiki = wiki;
		setPath(path);
		
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}
	
	@Override
	public void nextPage(String pageId, String title) {
		this.pageId = pageId;
		setTitle(title);
		
		prevId = null;
		linkList = new LinkedList<String>();
	}
	
	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) {
		// add basic node for revision, due to retrieval of follower first in second round
		nodes.add(new Node(revisionId,pageId));
		if (prevId != null) {
			// add basic edges between revisions of same page
			edges.add(new Edge(prevId, revisionId, "revision"));
			System.out.println("PageId: "+pageId+" + Revision: "+prevId+" to "+revisionId);
		}
		prevId = revisionId;
		
		
		// add edges for links between pages
		List<String> newLinks = parseAndCompareLinks(getTitle(),text,linkList);
		
		for(String link : newLinks) {
			if(!linkList.contains(link.toLowerCase())) {
    			try {
    				if(wiki.getPage(link) != null) {
	        			int targetPageId = wiki.getPage(link).getPageId();
//	        			if(knownArticles.contains(targetPageId)) { //due to problem that no revisions for page existent
	        				List<Timestamp> ts = revApi.getRevisionTimestampsBetweenTimestamps(targetPageId, revApi.getFirstDateOfAppearance(targetPageId), t);
		        			if(ts.size() > 0) {
		        				linkList.add(link.toLowerCase());
		        				if (!pageId.equals(Integer.toString(targetPageId))) { //check if link is loop
				        			edges.add(new Edge(Integer.toString(revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID()), revisionId, "link"));
				        			System.out.println("PageId: "+pageId+" + Link: "+revApi.getRevision(targetPageId, ts.get(ts.size()-1)).getRevisionID()+" to "+revisionId);
								}
		        				else {
		        					System.out.println("########## LOOP ###########");
		        				}
		        			}
//	        			}
        			}
				} catch (Exception e) {
					// TODO: handle exception
				}
    		}
		}
	}
	
	@Override
	public void close() {
		//Serialize nodes and edges
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(getPath()+"/nodes_"+getTitle()+".tmp");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(nodes);
	        fos = new FileOutputStream(getPath()+"/edges_"+getTitle()+".tmp");
	        oos = new ObjectOutputStream(fos);
	        oos.writeObject(edges);
	        oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}
}
