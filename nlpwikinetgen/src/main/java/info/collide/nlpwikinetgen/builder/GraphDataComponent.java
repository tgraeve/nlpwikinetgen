package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;

public interface GraphDataComponent extends Cloneable {
	
	public void nextPage(String pageId, String title) throws Exception;
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception;
	public Object close();
	
	public Object clone();
	
	public void setOutputPath(String path);
	public String getOutputPath();
	
	public void setDescr(String descr);
	public String getDescr();
}
