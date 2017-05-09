package info.collide.nlpwikinetgen.helper;

import java.io.Serializable;

public class Node implements Serializable {
	int Id;
	int pageId;
	String name;
	
	public Node (int Id, int pageId, String name) {
		this.Id = Id;
		this.pageId = pageId;
		this.name = name;
	}
	
	public int getId() {
		return Id;
	}
	
	public int getPageId() {
		return pageId;
	}
	
	public String getName() {
		return name;
	}
}
