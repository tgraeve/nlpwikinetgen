/**
 * 
 */
package info.collide.nlpwikinetgen.lucene;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.wikipedia.WikipediaTokenizer;
import org.apache.lucene.index.IndexWriter;

/**
 * @author Tobias Graeve
 *
 */
public class WikiAnalyzer extends Analyzer {

	 @Override
	   protected TokenStreamComponents createComponents(String fieldName) {
	     Tokenizer source = new WikipediaTokenizer();
	     TokenStream filter = new StandardFilter(source);
	     filter = new LowerCaseFilter(filter);
	     filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	     filter = new SnowballFilter(filter, "English");
	     filter = new LengthFilter(filter, 0, IndexWriter.MAX_TERM_LENGTH/4);
	     return new TokenStreamComponents(source, filter);
	   }
}