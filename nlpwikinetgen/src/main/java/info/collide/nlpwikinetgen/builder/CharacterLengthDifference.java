package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.type.IntNode;

public class CharacterLengthDifference implements GraphDataComponent {
	
	RevisionApi revApi;
	List<IntNode> nodes;
	int prevLength;
	String descr = "Character_Length_Difference";
	
	public CharacterLengthDifference(RevisionApi revApi) {
		this.revApi = revApi;
		this.nodes = new ArrayList<IntNode>();
	}

	@Override
	public void nextPage(String pageId, String title) throws Exception {
		this.prevLength = 0;
	}

	@Override
	public void nextRevision(String revisionId, String text, Timestamp t) throws Exception {
		int length = text.length();
		int difference = Math.abs(prevLength-length);
		nodes.add(new IntNode(revisionId, difference));
	}

	@Override
	public Object close() {
		return nodes;
	}
	
	@Override
	public void setDescr(String descr) {
		this.descr = descr;
		
	}

	@Override
	public String getDescr() {
		return descr;
	}

}
