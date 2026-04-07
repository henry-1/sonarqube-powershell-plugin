package org.sonar.plugins.psscriptanalyzer.types;

import org.sonar.api.rules.RuleType;

public class PsaRule {
	private String key;
	private String description;
	private String whyIsThisAnIssue;
	private String remediationFunction;
	private String debtRemediationFunctionLinearOffset;
	private String debtRemediationFunctionCoefficient;
	private String severity;
	private String ruletype;
	
	public PsaRule(String key, String description, String whyIsThisAnIssue) {
		this.key = key;		
		this.description = description;
		this.whyIsThisAnIssue = whyIsThisAnIssue;
		
		this.debtRemediationFunctionLinearOffset = "";
		this.debtRemediationFunctionCoefficient = "5min";
		this.remediationFunction = "LINEAR";
		this.severity = "MAJOR";
		this.ruletype = "CODE_SMELL";
	}
	
	public String getKey() {
        return this.key;
    }
	
	public String getWhyIsThisAnIssue() {
        return this.whyIsThisAnIssue;
    }
	
	public String getDescription() {
        return this.description;
    }
	
	public void setRemediationFunction(String remediationFunction) {
        this.remediationFunction = remediationFunction;
    }
	public String getRemediationFunction() {
        return this.remediationFunction;
    }	
	
	public void setDebRemediationFunctionLinearOffset(String debtRemediationFunctionLinearOffset) {
        this.debtRemediationFunctionLinearOffset = debtRemediationFunctionLinearOffset;
    }
	public String getDebtRemediationFunctionLinearOffset() {
        return this.debtRemediationFunctionLinearOffset;
    }
	
	public void setDebtRemediationFunctionCoefficient(String debtRemediationFunctionCoefficient) {
        this.debtRemediationFunctionCoefficient = debtRemediationFunctionCoefficient;
    }
	public String getDebtRemediationFunctionCoefficient() {
        return this.debtRemediationFunctionCoefficient;
    }
	
	public void setSeverity(String severity) {
		this.severity = severity;
    }
	public String getSeverity() {
        return this.severity;
    }
	
	public void setRuleType(String ruletype) {
		this.ruletype = ruletype;
    }
	public String getRuleTypeAsString() {
        return this.ruletype;
    }
	public RuleType getRuleType()
    {
    	switch (this.ruletype.toLowerCase()) {	    
		    case "bug":
		        return RuleType.BUG;
	
		    case "vulnarability":
		    	return RuleType.VULNERABILITY;
		        
		    case "security_hotspot":
		    	return RuleType.SECURITY_HOTSPOT;
	
		    default:
		    	return RuleType.CODE_SMELL;
    	}
    }
}
