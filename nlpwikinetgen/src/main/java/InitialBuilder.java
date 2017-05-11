import java.io.BufferedReader;
import java.io.FileReader;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import info.collide.nlpwikinetgen.lucene.DumpIndexer;

public class InitialBuilder {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	private void indexWiki(Wikipedia wiki) {
		DumpIndexer indexer = new DumpIndexer(wiki);
		indexer.indexWiki();
	}
	
	private Wikipedia getWiki(DatabaseConfiguration dbConfig) {
		Wikipedia wiki = null;
		try {
			wiki = new Wikipedia(dbConfig);
		} catch (WikiInitializationException e) {
			System.out.println("Execution aborted: Can't access Wikipedia, please check database configuration.");
			e.printStackTrace();
		}
		
		return wiki;
	}
	
	private DatabaseConfiguration getDatabaseConfig() {
		DatabaseConfiguration dbConfig = new DatabaseConfiguration();
		
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

}
