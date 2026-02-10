package org.sonar.plugins.psscriptanalyzer;

import java.nio.file.Path;
import java.util.List;

import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.psscriptanalyzer.common.Common;
import org.sonar.plugins.psscriptanalyzer.readers.PsaRuleReader;
import org.sonar.plugins.psscriptanalyzer.types.PsaRule;


public class PSScriptAnalyzerRulesDefinition implements RulesDefinition {
   
	private final Configuration config;
	
	public PSScriptAnalyzerRulesDefinition(final Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public void define(Context context) {
    	
    	Boolean debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
    	
    	final PsaRuleReader psaRuleReader = new PsaRuleReader();
    	
    	String constantPerBugIssue = "10min";
    	String constantPerCodeSmellIssue = "5min";    	

        NewRepository repo = context        	
            .createRepository(Constants.REPOSITORY_KEY, Constants.PROGRAMMING_LANGUAGE)   
            .setName(Constants.REPOSITORY_NAME);

        // Rule Templates:
        
        NewRule errorRule = repo.createRule(Constants.SENSOR_RULE_TYPE_GENERAL_PSA_ERROR)
            .setName("PSA Execution Error")
            .setType(RuleType.BUG)
            .setStatus(RuleStatus.READY)            
            .setSeverity(Severity.BLOCKER)
            .setHtmlDescription("This issue occurs when PSScriptAnalyzer fails to execute.");
        errorRule.setDebtRemediationFunction(errorRule.debtRemediationFunctions().constantPerIssue(constantPerBugIssue));
        
        NewRule infoRule = repo.createRule(Constants.SENSOR_RULE_TYPE_PSA_FINDING_INFO)
	        .setName(Constants.REPOSITORY_NAME)
	        .setType(RuleType.CODE_SMELL)
	        .setSeverity(Severity.INFO)
	        .setStatus(RuleStatus.READY)
	        .setHtmlDescription("This is an informational Issue.");
        infoRule.setDebtRemediationFunction(infoRule.debtRemediationFunctions().constantPerIssue(constantPerCodeSmellIssue));

       NewRule minorRule = repo.createRule(Constants.SENSOR_RULE_TYPE_PSA_FINDING_WARNING)
	        .setName(Constants.REPOSITORY_NAME)
	        .setType(RuleType.CODE_SMELL)
	        .setStatus(RuleStatus.READY)
	        .setSeverity(Severity.MAJOR)
	        .setHtmlDescription("This is a minor Issue.");
       minorRule.setDebtRemediationFunction(minorRule.debtRemediationFunctions().constantPerIssue(constantPerBugIssue));

       NewRule bugRule = repo.createRule(Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR)
	        .setName(Constants.REPOSITORY_NAME)
	        .setType(RuleType.CODE_SMELL)
	        .setStatus(RuleStatus.READY)
	        .setSeverity(Severity.CRITICAL)	        
	        .setHtmlDescription("This is a major Issue.");
       bugRule.setDebtRemediationFunction(bugRule.debtRemediationFunctions().constantPerIssue(constantPerBugIssue));      
       
       
       // Dynamic Rules: 
       
	   String psaRulesPath = config
               .get(Constants.CONFIGURAITON_PROPERTY_PSARULES_FOLDERPATH)
               .orElse("");
	   
	   List<Path> rulesDefinitions = Common.readAllXmlFiles(psaRulesPath);

	   for(Path rulesDefinition : rulesDefinitions) {
		   final PsaRule psaRule = psaRuleReader.read(rulesDefinition);
		   
		   NewRule dynamicRule = repo.createRule(psaRule.getKey())
				   .setName(Constants.REPOSITORY_NAME)
				   .setType(psaRule.getRuleType())
				   .setStatus(RuleStatus.READY)
				   .setSeverity(psaRule.getSeverity())
				   .setHtmlDescription(psaRule.getDescription());
				   //.setInternalKey(psaRule.getKey());
		   
		   var f = dynamicRule.debtRemediationFunctions();
		   String coefficient = psaRule.getDebtRemediationFunctionCoefficient();
		   String linearOffset = psaRule.getDebtRemediationFunctionLinearOffset();
		   
		   switch (psaRule.getRemediationFunction()) {		    
			    case "LINEAR":
			        dynamicRule.setDebtRemediationFunction(f.linear(coefficient));
			        break;
	
			    case "LINEAR_OFFSET":
			    	dynamicRule.setDebtRemediationFunction(f.linearWithOffset(coefficient, linearOffset));
			        break;
	
			    default:
			    	dynamicRule.setDebtRemediationFunction(f.constantPerIssue(coefficient));
		   }
		   
		   if(debugOutputEnabled)
		    	System.out.println("[PSA-Plugin] Rule created: " + psaRule.getKey());
	   }
   
        repo.done();
    }
        
    
    
}