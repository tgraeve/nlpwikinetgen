package info.collide.nlpwikinetgen.helper;

import java.io.Serializable;

public class Vertex implements Serializable {
	int Id;
	int pageId;
	String name;
	boolean flag;
	long changeLoad;
	
	public Vertex (int Id, int pageId, String name, boolean flag, long changeLoad) {
		this.Id = Id;
		this.pageId = pageId;
		this.name = name;
		this.flag = flag;
		this.changeLoad = changeLoad;
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
	
	public boolean isFlagged() {
		return flag;
	}
	
	public long getChangeLoad() {
		return changeLoad;
	}
}
