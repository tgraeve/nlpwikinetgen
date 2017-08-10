package info.collide.nlpwikinetgen.type;

public class DoubleNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1170671657280438989L;
	private double value;
	
	public DoubleNode() {
		
	}
	
	public DoubleNode (String id, double value) {
		super(id);
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}