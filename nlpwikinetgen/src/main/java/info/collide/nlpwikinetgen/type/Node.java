package info.collide.nlpwikinetgen.type;

import java.io.Serializable;

public class Node implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1170671657280438989L;
	private String id; 
	private String pageId;
	
	public Node (String id, String pageId) {
		super();
		this.id = id;
		this.pageId = pageId;
	}

	public Node(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}
}