package org.sonar.plugins.psscriptanalyzer.types;

import org.sonar.plugins.psscriptanalyzer.Constants;

public class PSFinding {
    private final String testName;
    private final String message;
    private final String scriptName;
    private final int line;
    private final String severity; 

    public PSFinding(String testName, String message, String scriptName, int line, String severity) {
        this.testName = testName;
        this.message = message;
        this.scriptName = scriptName;
        this.line = line;
        this.severity = severity;
    }

    public String getTestName() { return this.testName; }
    public String getMessage() { return (this.testName + " -> " + message); }
    public String getScriptName() { return scriptName; }
    public int getLine() { return line; }
    public String getSeverity() { return severity; }
    
    public String getRuleKey() {
	    if (this.severity == null) {
	        return Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR;
	    }

	    switch (this.severity.toLowerCase()) {
	        case "information":
	            return Constants.SENSOR_RULE_TYPE_PSA_FINDING_INFO;

	        case "warning":
	            return Constants.SENSOR_RULE_TYPE_PSA_FINDING_WARNING;

	        case "error":
	            return Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR;

	        default:
	            return Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR;
	    }
	}
}