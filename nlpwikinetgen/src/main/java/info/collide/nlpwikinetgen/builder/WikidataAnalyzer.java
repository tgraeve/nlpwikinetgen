package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import main.DataBuilder;

/**
 * Abstract class of module which can plugged in {@link DataBuilder} pipeline to generate
 * data for resulting annotated graph.
 * 
 * @author Tobias Graeve
 *
 */
abstract public class WikidataAnalyzer {
	
	protected RevisionApi revApi;
	private String path;
	private String descr;
	private String title;
	
	/**
	 * Constructor which gets necessary {@link RevisionApi}.
	 * 
	 * @param revApi Instance of {@link RevisionApi}.
	 */
	public WikidataAnalyzer(RevisionApi revApi) {
		this.revApi = revApi;
	}
	
	/**
	 * 
	 * @param pageId Identifier of {@link Page}.
	 * @param title Title of wiki article.
	 * @throws Exception
	 */
	public void nextPage(String pageId, String title) throws Exception {
		this.title = title;
	}
	
	/**
	 * @param revisionId Identifier of {@link Revision}.
	 * @param text Text of {@link Revision}.
	 * @param t Timestamp of {@link Revision}.
	 * @throws Exception
	 */
	public abstract void nextRevision(String revisionId, String text, Timestamp t) throws Exception;

	/**
	 * Triggers components to end and save their results when finished.
	 */
	public abstract void close();
	
	/**
	 * Clones module including all parameters.
	 * @return Copy of actual instance including all parameters.
	 */
	public abstract Object clone();
	
	/**
	 * Set path of output folder for results.
	 * @param path Path to output folder.
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return Path to output folder.
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Set unique identifier of component.
	 * @param descr Unique identifier of component.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	/**
	 * @return Unique identifier of component.
	 */
	public String getDescr() {
		return descr;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}