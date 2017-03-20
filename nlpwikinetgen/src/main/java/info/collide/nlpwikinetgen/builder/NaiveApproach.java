package info.collide.nlpwikinetgen.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.util.*;

import de.tudarmstadt.ukp.wikipedia.api.*;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;


public class NaiveApproach implements WikiConstants {

    public static void main(String[] args) throws WikiApiException, IOException {

        // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("134.91.20.26");
        dbConfig.setDatabase("wiki_20161101");
        dbConfig.setUser("tobias");
        dbConfig.setPassword("password");
        dbConfig.setLanguage(Language.german);
        
        //set lucene config
        Directory directory = FSDirectory.open(new File(System.getProperty("user.dir")+"/output").toPath());
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory , config);

        // Create a new German wikipedia.
        Wikipedia wiki = new Wikipedia(dbConfig);
        RevisionIterator revIt = new RevisionIterator(dbConfig) ;
        RevisionApi revApi = new RevisionApi(dbConfig) ;

        // Select category
        String title = "Bier_als_Thema";
        Category cat;
        
        try {
            cat = wiki.getCategory(title);
        } catch (WikiPageNotFoundException e) {
            throw new WikiApiException("Category " + title + " does not exist");
        }
        
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<StringPair> arcs = new ArrayList<StringPair>();
        int pageCount = 0;
        
        for(Page page : cat.getArticles()) {
        	int prevId = -1;
//        	String pageId = new DecimalFormat("000000000000").format(pageCount); //no need for own id, revisionID is unique
        	String name = page.getTitle().toString();
        	//Get all revisions for the article
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
        	if(!revisionTimeStamps.isEmpty()) {
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(page.getPageId(), t);
	        		int revisionId = rev.getRevisionID();
	        		boolean major = !rev.isMinor();
	        		vertices.add(new Vertex(revisionId,name, major,rev.byteSize()));
	        		System.out.println(rev.getRevisionText());
	        		if(prevId!=-1) {
	        			arcs.add(new StringPair(prevId,revisionId));
	        		}
	        		prevId = revisionId;
	        		System.out.println("\nVertex: "+revisionId+"++"+name+"++"+major+"++"+rev.byteSize());
	        		
	        		indexDocument(indexWriter, name, revisionId, rev.getRevisionText());
	        	}
        	}
        	pageCount++;
        }
        
        writeGML(vertices, arcs);
        

//        // the title of the page
//        System.out.println("Queried string       : " + title);
//        System.out.println("Title                : " + page.getTitle());
//
//        // whether the page is a disambiguation page
//        System.out.println("IsDisambiguationPage : " + page.isDisambiguation());
//
//        // whether the page is a redirect
//        // If a page is a redirect, we can use it like a normal page.
//        // The other infos in this example are transparently served by the page that the redirect points to.
//        System.out.println("redirect page query  : " + page.isRedirect());
//
//        // the number of links pointing to this page
//        System.out.println("# of ingoing links   : " + page.getNumberOfInlinks());
//
//        // the number of links in this page pointing to other pages
//        System.out.println("# of outgoing links  : " + page.getNumberOfOutlinks());
//
//        // the number of categories that are assigned to this page
//        System.out.println("# of categories      : " + page.getNumberOfCategories());

    }
    
    static void writeGML(List<Vertex> vertices, List<StringPair> arcs) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/output/dag.gml"));
    	
    	// start gml file
        writer.write("graph [");
        writer.newLine();
        writer.write("directed 1");
        writer.newLine();
        
        // write nodes
        for(Vertex v : vertices) {
        	writer.write("\tnode [");
            writer.newLine();
            writer.write("\t\tid "+v.getId());
            writer.newLine();
            writer.write("\t\tlabel \""+v.getName()+"\"");
            writer.newLine();
            writer.write("\t\tisMajorEdit \""+v.isFlagged()+"\"");
            writer.newLine();
            writer.write("\t\tbyteLoad \""+v.getChangeLoad()+"\"");
            writer.newLine();
            writer.write("\t]");
            writer.newLine();
        }
        for(StringPair a : arcs) {
        	writer.write("\tedge [");
        	writer.newLine();
        	writer.write("\t\tsource "+a.getSource());
        	writer.newLine();
        	writer.write("\t\ttarget "+a.getDestination());
        	writer.newLine();
        	writer.write("\t]");
        	writer.newLine();
        }
        
        writer.flush();
        writer.close();
    }
    
    static void indexDocument(IndexWriter writer, String title, int revision, String content) throws IOException {
    	Document doc = new Document();
    	
    	Field name = new StoredField("title", title);
    	doc.add(name);
    	
    	Field revId = new StoredField("RevisionID", revision);
    	doc.add(revId);
    	
    	Field article = new TextField("Content", content, Store.YES);
    	doc.add(article);
    	
    	writer.addDocument(doc);
    	
    }
    
    public void writePajek(List<Vertex> vertices, List<StringPair> arcs) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/output/dag.net"));
        writer.write("*Vertices " + vertices.size());
        writer.newLine();
        
        String line ="";
        for(Vertex v : vertices) {
        	if(v.isFlagged()) {
        		line = v.getId()+" "+"'"+v.getName()+"'"+" ic Red";
        	}
        	else {
        		line = v.getId()+" "+"'"+v.getName()+"'";
        	}
        	writer.write(line);
        	writer.newLine();	
        }
        
        writer.write("*Arcs");
        writer.newLine();
        
        for(StringPair a : arcs) {
        	writer.write(a.getSource()+" "+a.getDestination());
        	writer.newLine();
        }
        
        writer.flush();
        writer.close();
    }
    
    static class Vertex {
    	int Id;
    	String name;
    	boolean flag;
    	long changeLoad;
    	
    	public Vertex (int Id, String name, boolean flag, long changeLoad) {
    		this.Id = Id;
    		this.name = name;
    		this.flag = flag;
    		this.changeLoad = changeLoad;
    	}
    	
    	public int getId() {
    		return Id;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public boolean isFlagged() {
    		return flag;
    	}
    	
    	public long getChangeLoad() {
    		return changeLoad;
    	}
    }
    
    static class StringPair {

        int source;

        int destination;

        public StringPair(int source, int destination) {
            this.source = source;
            this.destination = destination;
        }

        public int getSource() {
            return source;
        }

        public int getDestination() {
            return destination;
        }
        
        @Override
		public boolean equals(Object o) {
            boolean equals = false;
            if (o instanceof StringPair) {
                 StringPair sp = (StringPair) o;
                 if (getSource()==sp.getSource() && getDestination()==sp.getDestination()) {
                     equals = true;
                 }
            }
            return equals;
        }
        
        @Override
		public int hashCode() {
            return ("<" + getSource() + ">,<" + getDestination() + ">").hashCode();
        }
    }
}
