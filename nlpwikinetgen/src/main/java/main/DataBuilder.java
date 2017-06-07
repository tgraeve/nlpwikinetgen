package main;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import dkpro.similarity.algorithms.lexical.ngrams.WordNGramJaccardMeasure;
import filter.RDDFilter;
import info.collide.nlpwikinetgen.builder.GraphDataComponent;
import info.collide.nlpwikinetgen.builder.RevisionNetwork;
import info.collide.nlpwikinetgen.builder.SimilarityCalculator;
import info.collide.nlpwikinetgen.helper.RDDBuilder;
import info.collide.nlpwikinetgen.lucene.DumpIndexer;
import info.collide.nlpwikinetgen.type.DoubleNode;
import info.collide.nlpwikinetgen.type.Node;
import javafx.concurrent.Task;

/**
 * Builds necessary parts of graph out of wiki data.
 * Please combine required modules in main method.
 * 
 * @author Tobias Graeve
 *
 */
public class DataBuilder extends Task{
	
	private String pathToConf;
	private String pathToFolder;
	private boolean wholeWiki;
	private String Category;
	private boolean buildGraph;
	private boolean buildIndex;
	private List<GraphDataComponent> filter;

	private List<String> folderMeta;
	
	private DatabaseConfiguration dbConfig;
	private Wikipedia wiki;
	private RevisionApi revApi;
	Iterable<Page> pages;
	
	RevisionNetwork revNet = null;
	DumpIndexer indexer = null;
	long pageAmount;
	long counter = 0;
	
	public DataBuilder(String pathToConf, String pathToFolder, boolean wholeWiki, String category, boolean buildGraph, boolean buildIndex) throws WikiApiException, IOException {
		this.pathToConf = pathToConf;
		this.pathToFolder = pathToFolder;
		this.wholeWiki = wholeWiki;
		this.Category = category;
		this.buildGraph = buildGraph;
		this.buildIndex = buildIndex;
		
		dbConfig = getDatabaseConfig();
		wiki = getWiki(dbConfig);
		revApi = getRevisionAPI();
				
		if (wholeWiki) {
			pages = wiki.getArticles();
			pageAmount = wiki.getMetaData().getNumberOfPages();
			
		} else {
			Category cat = wiki.getCategory(category);
			pages = cat.getArticles();
			pageAmount = cat.getNumberOfPages();
		}
		
		if (buildGraph) {
			revNet = new RevisionNetwork(wiki, revApi, pathToFolder);
		}
		
		if (buildIndex) {
			indexer = new DumpIndexer(revApi, pathToFolder);
		}
	}
	
	@Override
	protected Object call() throws Exception {
		updateMessage("Start generating...");
		for (Page page : pages) {
			int pageId = page.getPageId();
			String sPageId = Integer.toString(pageId);
			String title = page.getTitle().toString();
			
			if (revNet != null) {revNet.nextPage(sPageId, title);}
			if (indexer != null) {indexer.nextPage(sPageId, title);}
			
			for(GraphDataComponent component : filter) {
				component.nextPage(sPageId, title);
			}
			
			Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
			if (!revisionTimeStamps.isEmpty()) {
				for (Timestamp t : revisionTimeStamps) {
					Revision rev = revApi.getRevision(pageId, t);
	        		String revisionId = Integer.toString(rev.getRevisionID());
	        		String text = rev.getRevisionText();
	        		
	        		if (revNet != null) {revNet.nextRevision(revisionId, text, t);}
	        		if (indexer != null) {indexer.nextRevision(revisionId, text, t);}
	        		
	        		for(GraphDataComponent component : filter) {
	    				component.nextRevision(revisionId, text, t);
	    			}
				}
			}
			counter++;
			updateMessage("Page "+title+" done. ("+counter+"/"+pageAmount+")");
			updateProgress(counter, pageAmount);
		}
		
		if (revNet != null) {revNet.close();}
		if (indexer != null) {indexer.close();}
		
		for(GraphDataComponent component : filter) {
			serializeData(component.close(), component.getDescr());
		}
		
		updateMessage("Done.");
		return null;
	}

	public static void main(String[] args) throws WikiApiException, IOException {
//		dbConfig = getDatabaseConfig();
//		wiki = getWiki(dbConfig);
//		revApi = getRevisionAPI(dbConfig);
		
//		Category cat = wiki.getCategory("German_beer_culture");
//		Iterable<Page> pages = cat.getArticles();
//		int pageAmount = cat.getNumberOfPages();
//		
//		RevisionNetwork revNet = new RevisionNetwork(revApi);
//		revNet.buildNetwork(wiki, pages, pageAmount);
//		
//		DumpIndexer indexer = new DumpIndexer(revApi);
//		indexer.indexWiki(pages, pageAmount);
//		
//		SimilarityCalculator simCalc = new SimilarityCalculator(revApi);
//		List<DoubleNode> simNodes = simCalc.calcSimilarity(pages, pageAmount, new WordNGramJaccardMeasure(3));
//		serializeData(simNodes, "simNodesJac");
		
//		RDDBuilder rddBuild = new RDDBuilder();
//		rddBuild.nodesToRDD("nodes.tmp");
//		rddBuild.nodesToRDD("simNodesJac.tmp");
//		
//		RDDFilter rddMerger = new RDDFilter();
//		rddMerger.filterRDD();
	}
	
	
	public void serializeData(Object o, String content) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(pathToFolder+"/" + content + ".filter");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(o);
	        oos.close();
		} catch (Exception e) {
			System.out.println("Failed serializing nodes. Please retry.");
			e.printStackTrace();
		} 
	}
	
	public DatabaseConfiguration getDatabaseConfig() {
		dbConfig = new DatabaseConfiguration();
		
		BufferedReader br = null;
		FileReader fr = null;
		
		try {
			fr = new FileReader(pathToConf);
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
	        
	        br.close();
			fr.close();
			
		} catch (Exception e) {
			System.out.println("Config file seems to be broken");
			e.printStackTrace();
		}	
		return dbConfig;
	}
	
	public Wikipedia getWiki(DatabaseConfiguration dbConfig) {
		try {
			wiki = new Wikipedia(dbConfig);
		} catch (WikiInitializationException e) {
			System.out.println("Can't build wiki object, please check database configuration.");
			e.printStackTrace();
		}
		return wiki;
	}
	
	public RevisionApi getRevisionAPI() {
		try {
			revApi = new RevisionApi(dbConfig);
		} catch (WikiApiException e) {
			System.out.println("Can't build revision api, please check database configuration.");
			e.printStackTrace();
		}
		return revApi;
	}
	
	public List<GraphDataComponent> getFilter() {
		return filter;
	}

	public void setFilter(List<GraphDataComponent> filter) {
		this.filter = filter;
	}
}