package info.collide.nlpwikinetgen.helper;

public class Vertex {
	int Id;
	String name;
	boolean flag;
	long changeLoad;
	
	public Vertex (int Id, String name, boolean flag, long changeLoad) {
		this.Id = Id;
		this.name = name;
		this.flag = flag;
		this.changeLoad = changeLoad;
	}
	
	public int getId() {
		return Id;
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
