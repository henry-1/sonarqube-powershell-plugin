package org.sonar.plugins.psscriptanalyzer.fillers;

/*
* Purpose: 
* 	It prevents false positives in change detection by ignoring irrelevant code, such as line numbers in COBOL.
* Definition: 
* 	If not explicitly defined by a scanner plugin, the entire file is typically assumed to be "significant code".
* Impact on Metrics: 
* 	Only changes to "significant code" are used to calculate modified lines and new code issues. 
*/

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.code.NewSignificantCode;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.Token;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;

public class SignificantCodeFiller implements IFiller{

	private final Configuration config;
	private final Boolean debugOutputEnabled;
	
	public SignificantCodeFiller (Configuration configuration) {
		this.config = configuration;
		    	
    	this.debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);    	
	}
	
	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {
		final List<Token> tokenList = tokens.getTokens();
		
		if(debugOutputEnabled)
			System.out.println("Getting significant Code for file " + f.uri().getPath());
		
		final NewSignificantCode siginificantCode = context.newSignificantCode().onFile(f);
		
		try {	
			
			// try to find relevant part of your script and mark it as relevant
			// other parts are ignored by SonarQube
			// if this is not set, all code is significant
			
			/*
        	int line = token.getStartLineNumber();
            int startLineOffset = (int)token.getStartColumnNumber();
            int endLineOffset = (int)token.getEndColumnNumber() - 1 ;

            siginificantCode.addRange(f.newRange(line, startLineOffset, line, endLineOffset));
            continue;
	           
            synchronized (context) {
                siginificantCode.save();
            }
            */
		
        } catch (final Throwable e) {
        	System.err.println(String.format("Exception while saving tokens for %s -> %s", f.uri().getPath(), e.toString()));	
        }	
		
	}

}
