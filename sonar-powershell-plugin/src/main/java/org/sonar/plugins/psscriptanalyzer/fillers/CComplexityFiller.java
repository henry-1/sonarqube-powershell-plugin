package org.sonar.plugins.psscriptanalyzer.fillers;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;

public class CComplexityFiller implements IFiller{
	
	private final Configuration config;
	private final Boolean debugOutputEnabled;
	
	public CComplexityFiller (Configuration configuration) {
		
		this.config = configuration;
		
		this.debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
	}
	
	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		
		final int tokenComplexity = tokens.getComplexity();

		try {
            synchronized (context) {
                context.<Integer>newMeasure().on(f)
                	.forMetric(CoreMetrics.COMPLEXITY)
                	.withValue(tokenComplexity)
                    .save();
                
                if(debugOutputEnabled)
                	System.out.println(String.format("[PSA-Plugin] Adding CComplexity of %s to %s file %s.", tokens.getComplexity(), f.language(), f.uri().getPath()));
                
            }
        } catch (final Throwable e) {
        	System.err.println(String.format("[PSA-Plugin] Exception while saving tokens" + e.getMessage()));
        }		
	}
}
