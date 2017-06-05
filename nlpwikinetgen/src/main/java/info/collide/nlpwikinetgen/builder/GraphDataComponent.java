package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;
import java.util.List;

import info.collide.nlpwikinetgen.type.BasicNode;

public interface GraphDataComponent {
	
	public void nextPage(int pageId, String title) throws Exception;
	public void nextRevision(int revisionId, String text, Timestamp t) throws Exception;
	public Object close();
}
