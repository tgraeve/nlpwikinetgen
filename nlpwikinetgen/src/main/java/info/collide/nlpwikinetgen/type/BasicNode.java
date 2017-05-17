package info.collide.nlpwikinetgen.type;

import java.io.Serializable;

public class BasicNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6103387860681425918L;
	public int id;
	
	public BasicNode (int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
