package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;
import java.util.List;

import info.collide.nlpwikinetgen.type.BasicNode;

public interface GraphDataComponent {
	
	public void nextPage(String pageId, String title) throws Exception;
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception;
	public Object close();
	
	public void setDescr(String descr);
	public String getDescr();
}
