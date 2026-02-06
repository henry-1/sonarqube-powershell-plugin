package org.sonar.plugins.psscriptanalyzer.fillers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.Token;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;

public class HalsteadComplexityFiller implements IFiller {

	private final Configuration config;
	private final Boolean debugOutputEnabled;
	
	private static final List<String> skipTypes = Arrays.asList("EndOfInput", "NewLine");
	
	private static final List<String> operandTypes = Arrays.asList("StringExpandable", "Variable", "SplattedVariable",
            "StringLiteral", "HereStringExpandable", "HereStringLiteral");
	
	public HalsteadComplexityFiller(Configuration configuration)
	{
		this.config = configuration;
    	
    	this.debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
	}	
	
	@Override
	public void fill(SensorContext context, InputFile f, Tokens tokens) {

		final List<Token> tokenList = tokens.getTokens();
		final List<String> uniqueOperands = new LinkedList<>();
        final List<String> uniqueOperators = new LinkedList<>();
		
		try {
            
            int totalOperands = 0;

            for (final Token token : tokenList) {
                if (skipTypes.contains(token.getKind()) || token.getText() == null) {
                    continue;
                }

                final String text = token.getText().toLowerCase();
                if (operandTypes.contains(token.getKind())) {
                    totalOperands++;
                    if (!uniqueOperands.contains(text)) {
                        uniqueOperands.add(text);
                    }
                    continue;
                }
                if (!uniqueOperators.contains(text)) {
                    uniqueOperators.add(text);
                }
            }
            int difficulty = (int) ((int) Math.ceil(uniqueOperators.size() / 2.0)
                    * ((totalOperands * 1.0) / uniqueOperands.size()));
            synchronized (context) {
                context.<Integer>newMeasure().on(f).forMetric(CoreMetrics.COGNITIVE_COMPLEXITY).withValue(difficulty)
                        .save();
                
                if(debugOutputEnabled)
                	System.out.println(String.format("Calculated %s for Cognitive Complexity in %s.", 
                			difficulty, 
                			f.uri().getPath()));
            }

        } catch (final Throwable e) {
        	System.err.println(String.format("Exception while saving cognitive complexity metric -> %s", e.getMessage()));  
        }		
	}
}
