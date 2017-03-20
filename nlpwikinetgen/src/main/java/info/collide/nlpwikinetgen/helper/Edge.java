package info.collide.nlpwikinetgen.helper;

public class Edge {
	
	String type;
	int source;
    int destination;

    public Edge(String type, int source, int destination) {
    	this.type = type;
        this.source = source;
        this.destination = destination;
    }

    public String getType() {
    	return type;
    }
    
    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }
    
    @Override
	public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof Edge) {
             Edge sp = (Edge) o;
             if (getType()==sp.getType() && getSource()==sp.getSource() && getDestination()==sp.getDestination()) {
                 equals = true;
             }
        }
        return equals;
    }
    
    @Override
	public int hashCode() {
        return ("<" + getType() + ">,<" + getSource() + ">,<" + getDestination() + ">").hashCode();
    }
}