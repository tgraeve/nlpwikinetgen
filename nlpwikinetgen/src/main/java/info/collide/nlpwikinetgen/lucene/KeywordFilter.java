package info.collide.nlpwikinetgen.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class KeywordFilter {

	IndexSearcher indexSearcher;
	IndexReader reader;
	QueryParser queryParser;
	Query query;
	
	String pathToFolder;
	String[] keywords;
	
	public KeywordFilter(String pathToFolder) throws IOException {
		this.pathToFolder = pathToFolder;
		Directory dir = FSDirectory.open(new File(pathToFolder+"/lucene").toPath());
		reader = DirectoryReader.open(dir);
		indexSearcher = new IndexSearcher(reader);
		
	}
   
	public ArrayList<String> getHits(String input) throws ParseException, IOException {
		ArrayList<String> revisions = new ArrayList<String>();
		TotalHitCountCollector coll = new TotalHitCountCollector();
		this.keywords = input.split(",");
	   
		for (String string : keywords) {
			Query q = new QueryParser("text", new WikiAnalyzer()).parse(string);
			indexSearcher.search(q, coll);
		
			ScoreDoc[] hits = indexSearcher.search(q, reader.numDocs()).scoreDocs;
			
			for(int i = 0; i<hits.length; i++) {
				int docId = hits[i].doc;
				Document doc = indexSearcher.doc(docId);
				revisions.add(doc.getField("revisionId").stringValue());
			}
		}
		return revisions; 
   }
   
}
