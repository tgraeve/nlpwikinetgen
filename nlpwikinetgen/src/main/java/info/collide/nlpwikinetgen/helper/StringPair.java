package info.collide.nlpwikinetgen.helper;

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
    
    public int hashCode() {
        return ("<" + getSource() + ">,<" + getDestination() + ">").hashCode();
    }
}