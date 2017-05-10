import java.io.BufferedReader;
import java.io.FileReader;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class InitialBuilder {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public DatabaseConfiguration getDatabaseConfig() {
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
