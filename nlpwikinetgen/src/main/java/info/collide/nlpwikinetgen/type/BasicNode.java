package info.collide.nlpwikinetgen.type;

import java.io.Serializable;

public class BasicNode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6103387860681425918L;
	public String id;
	
	public BasicNode (String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
