package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.type.IntNode;

public class CharacterDifference implements GraphDataComponent {
	
	RevisionApi revApi;
	List<IntNode> nodes;
	int prevLength;
	
	public CharacterDifference(RevisionApi revApi) {
		this.revApi = revApi;
		this.nodes = new ArrayList<IntNode>();
	}

	@Override
	public void nextPage(int pageId, String title) throws Exception {
		this.prevLength = 0;
	}

	@Override
	public void nextRevision(int revisionId, String text, Timestamp t) throws Exception {
		int length = text.length();
		int difference = Math.abs(prevLength-length);
		nodes.add(new IntNode(revisionId, difference));
	}

	public Object close() {
		return nodes;
	}

}
