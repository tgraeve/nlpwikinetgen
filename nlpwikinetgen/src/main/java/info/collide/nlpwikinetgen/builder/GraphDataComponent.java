package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import main.DataBuilder;

/**
 * Interface for modules which can plugged in {@link DataBuilder} pipeline.
 * 
 * @author Tobias Graeve
 *
 */
public interface GraphDataComponent {
	
	/**
	 * A GraphDataComponent is a module which generates data for resulting annotated graph.
	 * 
	 * 
	 * @param pageId Identifier of {@link Page}.
	 * @param title Title of wiki article.
	 * @throws Exception
	 */
	public void nextPage(String pageId, String title) throws Exception;
	
	/**
	 * @param revisionId Identifier of {@link Revision}.
	 * @param text Text of {@link Revision}.
	 * @param t Timestamp of {@link Revision}.
	 * @throws Exception
	 */
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception;

	/**
	 * Triggers components to end and save their results when finished.
	 */
	public void close();
	
	/**
	 * Clones module including all parameters.
	 * @return Copy of actual instance including all parameters.
	 */
	public Object clone();
	
	/**
	 * Set path of output folder for results.
	 * @param path Path to output folder.
	 */
	public void setOutputPath(String path);
	/**
	 * @return Path to output folder.
	 */
	public String getOutputPath();
	
	/**
	 * Set unique identifier of component.
	 * @param descr Unique identifier of component.
	 */
	public void setDescr(String descr);
	/**
	 * @return Unique identifier of component.
	 */
	public String getDescr();
}