package info.collide.nlpwikinetgen.lucene;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.sweble.PlainTextConverter;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class DumpIndexer {

	public static void main(String[] args) throws IOException, WikiApiException, SQLException {
		
		 // configure the database connection parameters
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("134.91.20.26");
        dbConfig.setDatabase("wiki_20161101");
        dbConfig.setUser("tobias");
        dbConfig.setPassword("password");
        dbConfig.setLanguage(Language.german);
        
        //set lucene config
        Directory directory = FSDirectory.open(new File(System.getProperty("user.dir")+"/output/lucene/wiki_20161101").toPath());
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory , config);

        // Create a new German wikipedia.
        Wikipedia wiki = new Wikipedia(dbConfig);
        
        // Create Revision Machine tools
        RevisionIterator revIt = new RevisionIterator(dbConfig) ;
        RevisionApi revApi = new RevisionApi(dbConfig);

        
        for(Page page : wiki.getArticles()) {
        	String title = page.getTitle().toString();
        	Collection<Timestamp> revisionTimeStamps = revApi.getRevisionTimestamps(page.getPageId());
        	if(!revisionTimeStamps.isEmpty()) {
	        	for(Timestamp t : revisionTimeStamps) {
	        		Revision rev = revApi.getRevision(page.getPageId(), t);
	        		int revisionId = rev.getRevisionID();
	        		
	        		String text = rev.getRevisionText();
	        		
	        		System.out.println(revisionId);
	        		
	        		index(indexWriter, revisionId, getPlainText(title, text));
	        	}
        	}
        }

	}
	
	static void index(IndexWriter writer, int revisionId, String text) throws IOException {
    	Document doc = new Document();
    	
    	Field revId = new StoredField("RevisionID", revisionId);
    	doc.add(revId);
    	
    	Field article = new TextField("Text", text, Store.NO);
    	doc.add(article);
    	
    	writer.addDocument(doc);
    	
    }
	
	   /**
		 * <p>Returns the Wikipedia article as plain text using the SwebleParser with
		 * a SimpleWikiConfiguration and the PlainTextConverter. <br/>
		 * If you have different needs regarding the plain text, you can use
		 * getParsedPage(Visitor v) and provide your own Sweble-Visitor. Examples
		 * are in the <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package
		 * or on http://www.sweble.org </p>
		 *
		 * <p>Alternatively, use Page.getText() to return the Wikipedia article
		 * with all Wiki markup. You can then use the old JWPL MediaWiki parser for
		 * creating a plain text version. The JWPL parser is now located in a
		 * separate project <code>de.tudarmstad.ukp.wikipedia.parser</code>.
		 * Please refer to the JWPL Google Code project page for further reference.</p>
		 *
		 * @return The plain text of a Wikipedia article
		 * @throws WikiApiException
		 */
		public static String getPlainText(String title, String text)
			throws WikiApiException
		{
			//Configure the PlainTextConverter for plain text parsing
			return (String) parsePage(new PlainTextConverter(), title, text);
		}

		/**
		 * Parses the page with the Sweble parser using a SimpleWikiConfiguration
		 * and the provided visitor. For further information about the visitor
		 * concept, look at the examples in the
		 * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package, or on
		 * <code>http://www.sweble.org</code> or on the JWPL Google Code project
		 * page.
		 *
		 * @return the parsed page. The actual return type depends on the provided
		 *         visitor. You have to cast the return type according to the return
		 *         type of the go() method of your visitor.
		 * @throws WikiApiException
		 */
		public static Object parsePage(AstVisitor v, String title, String text) throws WikiApiException
		{
			// Use the provided visitor to parse the page
			return v.go(getCompiledPage(title, text).getPage());
		}

		/**
		 * Returns CompiledPage produced by the SWEBLE parser using the
		 * SimpleWikiConfiguration.
		 *
		 * @return the parsed page
		 * @throws WikiApiException
		 */
		public static CompiledPage getCompiledPage(String title, String text) throws WikiApiException
		{
			CompiledPage cp;
			try{
				SimpleWikiConfiguration config = new SimpleWikiConfiguration("classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");

				PageTitle pageTitle = PageTitle.make(config, title);
				PageId pageId = new PageId(pageTitle, -1);

				// Compile the retrieved page
				Compiler compiler = new Compiler(config);
				cp = compiler.postprocess(pageId, text, null);
			}catch(Exception e){
				throw new WikiApiException(e);
			}
			return cp;
		}

}
