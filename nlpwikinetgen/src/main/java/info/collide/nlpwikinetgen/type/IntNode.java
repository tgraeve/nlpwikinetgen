package info.collide.nlpwikinetgen.type;

public class IntNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7904773846056238988L;
	public int value;
	
	public IntNode (String id, int value) {
		super(id);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}