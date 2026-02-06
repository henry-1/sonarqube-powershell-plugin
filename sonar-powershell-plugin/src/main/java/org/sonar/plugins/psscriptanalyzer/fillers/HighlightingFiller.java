package org.sonar.plugins.psscriptanalyzer.fillers;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;
import org.sonar.plugins.psscriptanalyzer.types.Token;

public class HighlightingFiller implements IFiller {
	
	private final Configuration config;
	private final Boolean debugOutputEnabled;

	public HighlightingFiller (Configuration configuration) {
		this.config = configuration;
		    	
    	this.debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
	}

	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {

		final List<Token> tokenList = tokens.getTokens();
		
		try {
            final NewHighlighting highlighting = context.newHighlighting().onFile(f);
            for (final Token token : tokenList) {
            	
            	try {
                    final List<String> kinds = Arrays.asList(token.getTokenFlags().toLowerCase().split(","));
                    int startLine = token.getStartLineNumber();
                    int startLineOffset = token.getStartColumnNumber() - 1;
                    int endLine = token.getEndLineNumber();
                    int endLineOffset = token.getEndColumnNumber() - 1;
                    if (check("comment", token, kinds)) {
                    	if(token.getText().toLowerCase().contains(".synopsis"))
                    	{
                    		highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.STRUCTURED_COMMENT);
                    	} else {
                    		highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.COMMENT);   
                    	}
                    }
                    else if (check("keyword", token, kinds)) {
                        highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.KEYWORD);
                    }
                    else if (check("StringLiteral", token, kinds) || check("StringExpandable", token, kinds)) {
                        highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.STRING);
                    }
                    else if (check("Variable", token, kinds)) {
                        highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.KEYWORD_LIGHT);
                    }
                    else if (check("Generic", token, kinds)) {
                        highlighting.highlight(startLine, startLineOffset, endLine, endLineOffset, TypeOfText.CONSTANT);
                    }
                } catch (Throwable e) {
                	System.err.println(String.format("Exception while adding highlighting for: %s -> %s", token, e.getMessage()));  
                }
            }
            synchronized (context) {
                highlighting.save();
            }
        } catch (Throwable e) {
        	System.err.println(String.format("Exception while running highlighting " + e.getMessage()));  
        }		
	}

    private static boolean check(final String txt, final Token token, final List<String> kinds) {
        return txt.toLowerCase().equals(token.getKind().toLowerCase()) || kinds.contains(txt.toLowerCase());
    }
}
