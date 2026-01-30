package org.sonar.plugins.psscriptanalyzer;

import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;

public class PSScriptAnalyzerRulesDefinition implements RulesDefinition {
   

    @Override
    public void define(Context context) {
    	
    	String constantPerBugIssue = "10min";
    	String constantPerCodeSmellIssue = "5min";    	

        NewRepository repo = context        	
            .createRepository(Constants.REPOSITORY_KEY, Constants.PROGRAMMING_LANGUAGE)   
            .setName(Constants.REPOSITORY_NAME);

        // Rule Templates:
        
        NewRule errorRule = repo.createRule(Constants.SENSOR_RULE_TYPE_GENERAL_PSA_ERROR)
            .setName("PSA Execution Error")
            .setType(RuleType.BUG)
            .setSeverity(Severity.BLOCKER)
            .setHtmlDescription("This issue occurs when PSScriptAnalyzer fails to execute.");
        errorRule.setDebtRemediationFunction(errorRule.debtRemediationFunctions().constantPerIssue(constantPerBugIssue));
        
        NewRule infoRule = repo.createRule(Constants.SENSOR_RULE_TYPE_PSA_FINDING_INFO)
	        .setName(Constants.REPOSITORY_NAME)
	        .setType(RuleType.CODE_SMELL)
	        .setSeverity(Severity.INFO)
	        .setHtmlDescription("This is an informational Issue.");
        infoRule.setDebtRemediationFunction(errorRule.debtRemediationFunctions().constantPerIssue(constantPerCodeSmellIssue));

       NewRule minorRule = repo.createRule(Constants.SENSOR_RULE_TYPE_PSA_FINDING_WARNING)
	        .setName(Constants.REPOSITORY_NAME)
	        .setType(RuleType.CODE_SMELL)
	        .setSeverity(Severity.MAJOR)
	        .setHtmlDescription("This is a minor Issue.");
       minorRule.setDebtRemediationFunction(errorRule.debtRemediationFunctions().constantPerIssue(constantPerBugIssue));

       NewRule bugRule = repo.createRule(Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR)
	        .setName(Constants.REPOSITORY_NAME)
	        .setType(RuleType.BUG)
	        .setSeverity(Severity.CRITICAL)
	        .setHtmlDescription("This is a major Issue.");
       bugRule.setDebtRemediationFunction(errorRule.debtRemediationFunctions().constantPerIssue(constantPerBugIssue));
    
        repo.done();
    }
}