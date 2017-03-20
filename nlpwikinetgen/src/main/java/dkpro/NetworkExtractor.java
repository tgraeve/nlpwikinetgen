/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package dkpro;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import dkpro.interfaces.I_Extractor;


/**
 * Dieser Extractor kann vollständig konfiguriert werden.
 * Er beinhaltet nur das Einlesen von Text.
 * 
 * 
 * 
 * Quelle: input/
 * Ziel: output/
 * 
 * @author Tobias Graeve
 *
 */

public class NetworkExtractor implements I_Extractor {
	
	protected AnalysisEngineDescription[] components;

	@Override
	public void startPipeline(AnalysisEngineDescription... components) throws UIMAException, IOException
	{
		this.components = components;	
		
		CollectionReaderDescription reader = createReaderDescription(
		         TextReader.class,
		         ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "input/",
		         ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {"[+]*.txt"},
		         ResourceCollectionReaderBase.PARAM_LANGUAGE, "en");
		
		
		
		AnalysisEngineDescription[] pipe = new AnalysisEngineDescription[components.length];
		
		int i = 0;
		while(i<components.length)
		{
			pipe[i] = components[i];
			i++;
		}
		
		runPipeline(reader, pipe);
	}

	@Override
	public void startPipeline() {
		// TODO Automatisch generierter Methodenstub
		
	}
}