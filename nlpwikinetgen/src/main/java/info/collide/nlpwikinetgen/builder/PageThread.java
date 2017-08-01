package info.collide.nlpwikinetgen.builder;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import info.collide.nlpwikinetgen.lucene.DumpIndexer;

public class PageThread implements Runnable {
	private Page page;
	private RevisionApi revApi;
	private NetworkBuilder netBuilder;
	private DumpIndexer indexer;
	private List<GraphDataComponent> filter;
	
	final Lock lock = new ReentrantLock();
	
	public PageThread(Page page, RevisionApi revApi, NetworkBuilder netBuilder, DumpIndexer indexer, List<GraphDataComponent> filter) {
		this.page = page;
		this.revApi = revApi;
		this.netBuilder = netBuilder;
		this.indexer = indexer;
		this.filter = filter;
	}

	@Override
	public void run() {
		int pageId = page.getPageId();
		String sPageId = Integer.toString(pageId);
		String title = "";
		try {
			title = page.getTitle().toString();
		} catch (WikiTitleParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (netBuilder != null) {netBuilder.nextPage(sPageId, title);}
		if (indexer != null) {indexer.nextPage(sPageId, title);}
		
		if (filter != null) {
			for(GraphDataComponent component : filter) {
				try {
					component.nextPage(sPageId, title);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		Collection<Timestamp> revisionTimeStamps = null;
		try {
			revisionTimeStamps = revApi.getRevisionTimestamps(pageId);
		} catch (WikiApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!revisionTimeStamps.isEmpty()) {
			for (Timestamp t : revisionTimeStamps) {
				Revision rev = null;
				try {
					rev = revApi.getRevision(pageId, t);
				} catch (WikiApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		String revisionId = Integer.toString(rev.getRevisionID());
        		String text = rev.getRevisionText();
        		
        		if (netBuilder != null) {netBuilder.nextRevision(revisionId, text, t);}
        		if (indexer != null) {indexer.nextRevision(revisionId, text, t);}
        		
        		if (filter != null) {
        			for(GraphDataComponent component : filter) {
        				try {
    						component.nextRevision(revisionId, text, t);
    					} catch (Exception e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
        			}
        		}
			}
		}
		if (netBuilder != null) {netBuilder.close();}
		if (indexer != null) {
			lock.lock();
			indexer.close();
			lock.unlock();
		}
		if (filter != null) {
			for(GraphDataComponent component : filter) {
				serializeData(component.close(), component.getDescr());
			}
		}
	}
	
	public void serializeData(Object o, String content) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(netBuilder.getPath()+"/" + content + ".filter");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(o);
	        oos.close();
		} catch (Exception e) {
			System.out.println("Failed serializing nodes. Please retry.");
			e.printStackTrace();
		} 
	}
}
