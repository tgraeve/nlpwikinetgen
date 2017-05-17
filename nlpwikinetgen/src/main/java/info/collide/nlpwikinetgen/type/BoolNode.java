package info.collide.nlpwikinetgen.type;

public class BoolNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5916343235809481865L;
	public boolean value;
	
	public BoolNode (int id, boolean value) {
		super(id);
		this.value = value;
	}

	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
}