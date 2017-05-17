package info.collide.nlpwikinetgen.type;

public class Node extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1170671657280438989L;
	public int pageId;
	public int follower;
	
	public Node (int id, int pageId) {
		super(id);
		this.pageId = pageId;
	}
	
	public Node (int id, int pageId, int follower) {
		super(id);
		this.pageId = pageId;
		this.follower = follower;
	}
	
	public int getPageId() {
		return pageId;
	}

	public int getFollower() {
		return follower;
	}

	public void setFollower(int follower) {
		this.follower = follower;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}
}