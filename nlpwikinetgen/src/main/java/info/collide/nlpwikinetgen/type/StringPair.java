package info.collide.nlpwikinetgen.type;

public class StringPair {
	int source;

    int destination;

    public StringPair(int source, int destination) {
        this.source = source;
        this.destination = destination;
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
        if (o instanceof StringPair) {
             StringPair sp = (StringPair) o;
             if (getSource()==sp.getSource() && getDestination()==sp.getDestination()) {
                 equals = true;
             }
        }
        return equals;
    }
    
    @Override
	public int hashCode() {
        return ("<" + getSource() + ">,<" + getDestination() + ">").hashCode();
    }
}