import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import dkpro.similarity.algorithms.lexical.ngrams.WordNGramJaccardMeasure;
import info.collide.nlpwikinetgen.builder.RevisionNetwork;
import info.collide.nlpwikinetgen.builder.SimilarityCalculator;
import info.collide.nlpwikinetgen.helper.RDDBuilder;
import info.collide.nlpwikinetgen.lucene.DumpIndexer;
import info.collide.nlpwikinetgen.type.DoubleNode;
import info.collide.nlpwikinetgen.type.Node;

/**
 * Builds necessary parts of graph out of wiki data.
 * Please combine required modules in main method.
 * 
 * @author Tobias Graeve
 *
 */
public class InitialBuilder {
	
	static DatabaseConfiguration dbConfig;
	static Wikipedia wiki;
	static RevisionApi revApi;

	public static void main(String[] args) throws WikiApiException, IOException {
		dbConfig = getDatabaseConfig();
		wiki = getWiki(dbConfig);
		revApi = getRevisionAPI(dbConfig);
		
		Category cat = wiki.getCategory("German_beer_culture");
		Iterable<Page> pages = cat.getArticles();
		int pageAmount = cat.getNumberOfPages();
		
		RevisionNetwork revNet = new RevisionNetwork(revApi);
		revNet.buildNetwork(wiki, pages, pageAmount);
		
		DumpIndexer indexer = new DumpIndexer(revApi);
		indexer.indexWiki(pages, pageAmount);
		
		SimilarityCalculator simCalc = new SimilarityCalculator(revApi);
		List<DoubleNode> simNodes = simCalc.calcSimilarity(pages, pageAmount, new WordNGramJaccardMeasure(3));
		serializeData(simNodes, "simNodesJac");
		
		RDDBuilder rddBuild = new RDDBuilder();
		rddBuild.nodesToRDD("nodes.tmp");

		
	}
	
	private static void serializeData(Object o, String content) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("data/" + content + ".tmp");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(o);
	        oos.close();
		} catch (Exception e) {
			System.out.println("Failed serializing nodes. Please retry.");
			e.printStackTrace();
		} 
	}
	
	private static DatabaseConfiguration getDatabaseConfig() {
		dbConfig = new DatabaseConfiguration();
		
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
	        
	        br.close();
			fr.close();
			
		} catch (Exception e) {
			System.out.println("Config file seems to be broken");
			e.printStackTrace();
		}
		
		return dbConfig;
	}
	
	private static Wikipedia getWiki(DatabaseConfiguration dbConfig) {
		try {
			wiki = new Wikipedia(dbConfig);
		} catch (WikiInitializationException e) {
			System.out.println("Can't build wiki object, please check database configuration.");
			e.printStackTrace();
		}
		
		return wiki;
	}
	
	private static RevisionApi getRevisionAPI(DatabaseConfiguration dbConfig) {
		try {
			revApi = new RevisionApi(dbConfig);
		} catch (WikiApiException e) {
			System.out.println("Can't build revision api, please check database configuration.");
			e.printStackTrace();
		}
		return revApi;
	}

}
