package org.sonar.plugins.psscriptanalyzer.fillers;

import java.util.Arrays;

/*
 * DUPLICATED_LINES  and DUPLICATED_LINES_DENSITY shouldn't be calculated by Sensor * 
 */

import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.config.Configuration;

import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.Token;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;

public class CpdFiller implements IFiller {
	
	private final Configuration config;
	private final Boolean debugOutputEnabled;
	
	private static final List<String> skipTypes = Arrays.asList("EndOfInput", "NewLine");

    private static final int COMMENT = 1;
    private static final int CODE = 2;
	
	public CpdFiller (Configuration configuration) {
		this.config = configuration;
		    	
    	this.debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);    	
	}

	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
				
		if(debugOutputEnabled)
			System.out.println("[PSA-Plugin] Getting Cut+Past Detection for file " + f.uri().getPath());
		
		String[] content = new String[0];
		try {
			content = f.contents().split("\\R", -1);
		} catch (final Throwable e){			
			System.err.println(String.format("[PSA-Plugin] Exception while splitting file content into lines for %s with error %s", f.uri().getPath(), e.getMessage()));
			return;
		}
		
		try {
		
			final long[] lines = new long[f.lines()];
			final List<Token> tokenList = tokens.getTokens();
	
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
	        
	        int tokenCount = 0;	
	        final NewCpdTokens cpdTokens = context.newCpdTokens().onFile(f);
	        
	        for (int i = 0; i < lines.length; i++) {
	        	if ((lines[i] & CODE) == CODE) {
	        		try {
	        		
		        		String originalLine = content[i]; // array is 0-based
		                int lineNumber = i + 1;           // Sonar is 1-based
		                String trimmedLine = originalLine.trim();
		                
		                if(trimmedLine.length() > 2) {    
		            		
			            	int startColumn = 0;
			            	while (startColumn < originalLine.length() && Character.isWhitespace(originalLine.charAt(startColumn))) {
			            	    startColumn++;
			            	}
			            	int endColumn = startColumn + trimmedLine.length();
		                       	
			                tokenCount++;
			                	
			                if(debugOutputEnabled)
			                	System.out.println(String.format("[PSA-Plugin] File: %s -> LineNumber: %s -> TokenText: %s -> TokenStart: %s EndColumn: %s", 
		                			f.uri().getPath(), lineNumber, trimmedLine, startColumn, endColumn));
			                 
			             	TextRange textRange = f.newRange(lineNumber, startColumn, lineNumber, endColumn);
			             	cpdTokens.addToken(textRange, trimmedLine);
		                }
		        	}catch(final Throwable e) {                    	
		            	System.err.println(String.format("[PSA-Plugin] Exception while creating TextRange %s", e.getMessage()));
		            }
	        	}        	
	        }
	        
	        synchronized (context) {
	               
	        	cpdTokens.save();                
	            
	            if(debugOutputEnabled)
	            	System.out.println(String.format("[PSA-Plugin] Found %s tokens for duplcation detection in file %s.",  
	            			tokenCount, f.uri().getPath()));        
	        }
		} catch (final Throwable e) {
        	System.err.println(String.format("[PSA-Plugin] Exception while Copy&Past Detection: %s", e.getMessage()));            
        }
	}
}
