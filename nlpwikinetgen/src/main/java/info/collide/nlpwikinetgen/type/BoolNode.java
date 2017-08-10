package info.collide.nlpwikinetgen.type;

public class BoolNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5916343235809481865L;
	private boolean value;
	
	public BoolNode() {
	}
	
	public BoolNode (String id, boolean value) {
		super(id);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}