package dkpro.interfaces;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

public interface I_Extractor {

	public void startPipeline() throws UIMAException, IOException;
	public void startPipeline(AnalysisEngineDescription... components) throws UIMAException, IOException;
	
}
