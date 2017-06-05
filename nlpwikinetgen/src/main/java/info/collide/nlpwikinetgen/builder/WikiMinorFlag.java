package info.collide.nlpwikinetgen.builder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.type.BasicNode;
import info.collide.nlpwikinetgen.type.BoolNode;

public class WikiMinorFlag implements GraphDataComponent {
	
	RevisionApi revApi;
	List<BoolNode> nodes;
	
	public WikiMinorFlag(RevisionApi revApi) {
		this.revApi = revApi;
		this.nodes = new ArrayList<BoolNode>();
	}

	@Override
	public void nextPage(int pageId, String title) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void nextRevision(int revisionId, String text, Timestamp t) throws Exception {
		System.out.println(revisionId);
		nodes.add(new BoolNode(revisionId, revApi.getRevision(revisionId).isMinor()));
		System.out.println(revisionId);
	}

	public Object close() {
		return nodes;
	}

}
