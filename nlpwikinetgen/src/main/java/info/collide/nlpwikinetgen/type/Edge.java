package info.collide.nlpwikinetgen.type;

import java.io.Serializable;

public class Edge implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6419409809982356478L;

	private String src;
    private String dst;
	private String type;
	
	public Edge() {
		
	}

    public Edge(String src, String dst, String type) {
        this.src = src;
        this.dst = dst;
        this.type = type;
    }

	public void setType(String type) {
		this.type = type;
	}
	
    public String getType() {
    	return type;
    }
    
    public String getSrc() {
        return src;
    }

    public String getDst() {
        return dst;
    }
    
    @Override
	public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof Edge) {
             Edge sp = (Edge) o;
             if (getType()==sp.getType() && getSrc()==sp.getSrc() && getDst()==sp.getDst()) {
                 equals = true;
             }
        }
        return equals;
    }
    
    @Override
	public int hashCode() {
        return ("<" + getType() + ">,<" + getSrc() + ">,<" + getDst() + ">").hashCode();
    }

	public void setSrc(String src) {
		this.src = src;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}