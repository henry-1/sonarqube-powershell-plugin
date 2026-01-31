package org.sonar.plugins.psscriptanalyzer;

public class RuleFinding {
    private final String testName;
    private final String message;
    private final String scriptName;
    private final int line;
    private final String severity; 

    public RuleFinding(String testName, String message, String scriptName, int line, String severity) {
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
}