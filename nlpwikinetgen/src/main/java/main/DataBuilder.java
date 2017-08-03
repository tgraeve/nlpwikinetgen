package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.builder.GraphDataAnalyzer;
import info.collide.nlpwikinetgen.builder.NetworkBuilder;
import info.collide.nlpwikinetgen.builder.PageThread;
import info.collide.nlpwikinetgen.lucene.DumpIndexer;
import info.collide.nlpwikinetgen.lucene.WikiAnalyzer;
import info.collide.nlpwikinetgen.type.BasicNode;
import info.collide.nlpwikinetgen.type.Edge;
import info.collide.nlpwikinetgen.type.Node;
import info.collide.nlpwikinetgen.type.StringPair;
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
	private String category;
	private boolean buildGraph;
	private boolean buildIndex;
	private List<GraphDataAnalyzer> filter;
	private List<GraphDataAnalyzer> filters;


	private List<String> folderMeta;
	
	private DatabaseConfiguration dbConfig;
	private Wikipedia wiki;
	private RevisionApi revApi;
	private Iterable<Page> pages;
	private Iterable<StringPair> sPages;
	
	NetworkBuilder revNet = null;
	DumpIndexer indexer = null;
	IndexWriter indexWriter = null;
	long pageAmount;
	long counter = 0;
	long started = 0;
	
	public DataBuilder(String pathToConf, String pathToFolder, boolean wholeWiki, String category, boolean buildGraph, boolean buildIndex) throws WikiApiException, IOException {
		this.pathToConf = pathToConf;
		this.pathToFolder = pathToFolder;
		this.wholeWiki = wholeWiki;
		this.category = category;
		this.buildGraph = buildGraph;
		this.buildIndex = buildIndex;
		
		dbConfig = getDatabaseConfig();
		wiki = getWiki(dbConfig);
		revApi = getRevisionAPI();
				
//		if (wholeWiki) {
//			pages = wiki.getArticles();
//			pageAmount = wiki.getMetaData().getNumberOfPages()-wiki.getMetaData().getNumberOfDisambiguationPages()-wiki.getMetaData().getNumberOfRedirectPages();
//			
//		} else {
//			Category cat = wiki.getCategory(category);
//			pages = getAllPages(cat);
////			pageAmount = cat.get
//		}
		
		if (buildIndex) {
			//set lucene config
	        Analyzer analyzer = new WikiAnalyzer();
	        IndexWriterConfig config = new IndexWriterConfig(analyzer);
	        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
	        
	        try {
				Directory directory = FSDirectory.open(new File(pathToFolder+"/lucene/").toPath());
				indexWriter = new IndexWriter(directory , config);
			} catch (IOException e) {
				System.out.println("Indexer failed while initializing the index writer.");
				e.printStackTrace();
			}
		}
	}
	
	@Deprecated
	private HashSet<Page> getAllPages(Category cat) throws WikiApiException {
		HashSet<Page> p = new HashSet<Page>();
		p.addAll(cat.getArticles());
		for(Category c : cat.getDescendants()) {
			System.out.println(c.getTitle());
			c.getArticles().forEach(page -> p.add(page));
			p.addAll(c.getArticles());
		}
		return p;
	}
	
	@Override
	protected Object call() throws Exception {
		updateMessage("Start generating...");
		
		ExecutorService ex = Executors.newFixedThreadPool(64);
		
		if (wholeWiki) {
			pages = wiki.getArticles();
			pageAmount = wiki.getMetaData().getNumberOfPages()-wiki.getMetaData().getNumberOfDisambiguationPages()-wiki.getMetaData().getNumberOfRedirectPages();
			for(Page page : pages) {
				executePage(page, ex);
			}
		} else {
			Category cat = wiki.getCategory(category);
			cat.getArticles().forEach(page -> executePage(page, ex));
			for(Category c : cat.getDescendants()) {
				c.getArticles().forEach(page -> executePage(page, ex));
			}
		}
		
//		for (Page page : pages) {
//			if (buildGraph) {
//				revNet = new NetworkBuilder(wiki, revApi, pathToFolder);
//			}
//			if (buildIndex) {
//				indexer = new DumpIndexer(indexWriter, revApi, pathToFolder);
//			}
//			
//			ex.execute(new PageThread(page, revApi, revNet, indexer, filter));
//			
//			started++;
//			updateMessage("Started/All ("+started+"/"+pageAmount+")");
////			updateProgress(started, pageAmount);
//		}

		ex.shutdown();
		
		ex.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		
		if (buildGraph) {
			System.out.println("Started to concat single graph files.");
			List<Node> finalNodes = new ArrayList<Node>();
			List<Edge> finalEdges = new ArrayList<Edge>();
			File dir = new File(pathToFolder);
			for(File file : dir.listFiles((d,name) -> name.toLowerCase().startsWith("nodes"))) {
				finalNodes.addAll(deserializeNodes(file.getAbsolutePath()));
				file.deleteOnExit();
			}
			for(File file : dir.listFiles((d,name) -> name.toLowerCase().startsWith("edges"))) {
				finalEdges.addAll(deserializeEdges(file.getAbsolutePath()));
				file.deleteOnExit();
			}
			serializeGraph(finalNodes, finalEdges);
		}
		if (buildIndex) {
			indexWriter.close();
			System.out.println("IndexWriter closed.");
		}
		if (filter != null) {
			for(GraphDataAnalyzer f : filter) {
				String des = f.getDescr();
				System.out.println("Started to concat single filter files of "+des);
				List<BasicNode> finalNodes = new ArrayList<BasicNode>();
				File dir = new File(pathToFolder);
				System.out.println(des.toLowerCase());
				for(File file : dir.listFiles((d,name) -> name.startsWith(des))) {
					finalNodes.addAll(deserializeFilter(file.getAbsolutePath()));
					file.deleteOnExit();
				}
				serializeFilter(finalNodes, des);
			}
		}
		
		updateMessage("Done.");
		return null;
	}
		
	private void executePage(Page page, ExecutorService ex) {
		filters = new ArrayList<GraphDataAnalyzer>();
		if (buildGraph) {
			revNet = new NetworkBuilder(wiki, revApi, pathToFolder);
		}
		if (buildIndex) {
			try {
				indexer = new DumpIndexer(indexWriter, revApi, pathToFolder);
			} catch (WikiApiException e) {
				System.out.println("Failed indexing page "+page.getPageId()+".");
				e.printStackTrace();
			}
		}
		if(filter != null) {
			for(GraphDataAnalyzer f : filter) {
				GraphDataAnalyzer cloned = (GraphDataAnalyzer) f.clone();
				cloned.setPath(pathToFolder);
				filters.add(cloned);
			}
		}
		
		ex.execute(new PageThread(page, revApi, revNet, indexer, filters));
		
		started++;
		updateMessage("Started/All ("+started+"/"+pageAmount+")");
//		updateProgress(started, pageAmount);
	}
	
	private ArrayList<Node> deserializeNodes(String pathToFolder) {
		FileInputStream fis;
		ArrayList<Node> nodes = null;
		try {
			fis = new FileInputStream(pathToFolder);
			ObjectInputStream ois = new ObjectInputStream(fis);
	        nodes = (ArrayList<Node>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return nodes;
	}
	
	private ArrayList<Edge> deserializeEdges(String pathToFolder) {
		FileInputStream fis;
		ArrayList<Edge> edges = null;
		try {
			fis = new FileInputStream(pathToFolder);
			ObjectInputStream ois = new ObjectInputStream(fis);
	        edges = (ArrayList<Edge>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return edges;
	}
	
	public void serializeGraph(List<Node> nodes, List<Edge> edges) {
		//Serialize nodes and edges
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(pathToFolder+"/nodes.tmp");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(nodes);
	        fos = new FileOutputStream(pathToFolder+"/edges.tmp");
	        oos = new ObjectOutputStream(fos);
	        oos.writeObject(edges);
	        oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void serializeFilter(Object o, String content) {
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
	
	private ArrayList<BasicNode> deserializeFilter(String pathToFile) {
		FileInputStream fis;
		ArrayList<BasicNode> nodes = null;
		try {
			fis = new FileInputStream(pathToFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
	        nodes = (ArrayList<BasicNode>) ois.readObject();
	        ois.close();
		} catch (Exception e) {
			System.out.println("Failed deserializing. Please retry.");
			e.printStackTrace();
		}
		return nodes;
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
	
	public List<GraphDataAnalyzer> getFilter() {
		return filter;
	}

	public void setFilter(List<GraphDataAnalyzer> filter) {
		this.filter = filter;
	}
}