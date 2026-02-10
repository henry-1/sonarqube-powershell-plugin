package org.sonar.plugins.psscriptanalyzer.fillers;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;
import org.sonar.plugins.psscriptanalyzer.types.Token;

public class LineMeasuresFiller implements IFiller{
	
	private final Configuration config;
	private final Boolean debugOutputEnabled;

	private static final List<String> skipTypes = Arrays.asList("EndOfInput", "NewLine");

    private static final int COMMENT = 1;
    private static final int CODE = 2;
    
    public LineMeasuresFiller (Configuration configuration)
    {
    	this.config = configuration;
    	
    	this.debugOutputEnabled = config
			.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
			.orElse(false);
    }
    
	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		
		
		if(debugOutputEnabled)
        	System.out.println("[PSA-Plugin] Finding Line Measures for: " + f.uri().getPath());
		
		final List<Token> tokenList = tokens.getTokens();
		
		try {

            final long[] lines = new long[f.lines()];

            for (final Token token : tokenList) {

                if (skipTypes.contains(token.getKind()) || token.getText() == null) {
                    continue;
                }

                if ("Comment".equalsIgnoreCase(token.getKind())) {
                    for (int i = token.getStartLineNumber(); i <= token.getEndLineNumber(); i++) {
                    	int idx = i - 1;      // convert to 0-based array index
                        lines[idx] |= COMMENT;
                    }
                } else {
                    for (int i = token.getStartLineNumber(); i <= token.getEndLineNumber(); i++) {
                    	int idx = i - 1;      // convert to 0-based array index
                        lines[idx] |= CODE;
                    }
                }
            }

            int commentLineCount = 0;
            int nonCommentLineCount = 0;
            
            for (int i = 0; i < lines.length; i++) {
            	
                if ((lines[i] == COMMENT)) {
                    commentLineCount++;
                    continue;
                }
                if ((lines[i] & CODE) == CODE) {
                    nonCommentLineCount++;   	
                }
            }            
            
            synchronized (context) {
                context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.COMMENT_LINES).withValue(commentLineCount).save();
                context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.NCLOC).withValue(nonCommentLineCount).save();  
                
                if(debugOutputEnabled)
                	System.out.println(String.format("[PSA-Plugin] Found %s Code Lines and %s Comment Lines in %s.", 
                			nonCommentLineCount, commentLineCount, f.uri().getPath()));            
            }
        } catch (final Throwable e) {
        	System.err.println(String.format("[PSA-Plugin] Exception while calculating comment lines " + e.getMessage()));            
        }
    }
}

