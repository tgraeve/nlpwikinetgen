import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.curator.framework.api.GetDataWatchBackgroundStatable;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.lucene.DumpIndexer;

public class InitialBuilder {
	
	static DatabaseConfiguration dbConfig;

	public static void main(String[] args) throws WikiApiException {
		DumpIndexer indexer = new DumpIndexer(getDatabaseConfig());
		indexer.indexWiki("German_beer_culture");
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
	
//	private static void indexWiki(DatabaseConfiguration dbConfig) {
//		DumpIndexer indexer = new DumpIndexer(dbConfig);
//		try {
//			indexer.indexWiki();
//		} catch (IOException | WikiApiException e) {
//			System.out.println("Indexer failed while indexing.");
//		}	
//	}
	
//	private static Wikipedia getWiki(DatabaseConfiguration dbConfig) {
//		try {
//			wiki = new Wikipedia(dbConfig);
//		} catch (WikiInitializationException e) {
//			System.out.println("Can't build wiki object, please check database configuration.");
//			e.printStackTrace();
//		}
//		
//		return wiki;
//	}
//	
//	private static RevisionApi getRevisionAPI(DatabaseConfiguration dbConfig) {
//		try {
//			revApi = new RevisionApi(dbConfig);
//		} catch (WikiApiException e) {
//			System.out.println("Can't build revision api, please check database configuration.");
//			e.printStackTrace();
//		}
//		return revApi;
//	}

}
