package org.sonar.plugins.psscriptanalyzer.fillers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.fix.NewInputFileEdit;
import org.sonar.api.batch.sensor.issue.fix.NewQuickFix;
import org.sonar.api.batch.sensor.issue.fix.NewTextEdit;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.Severity;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.types.PSFinding;

public class IssuesFiller {
	public void fill(final SensorContext context, final File sourceDir, final List<PSFinding> findings, Boolean debugOutputEnabled) {
		
		FileSystem fs = context.fileSystem();
		
		if(debugOutputEnabled)
        	System.out.println("Findings Count: " + findings.size());
        
        Map<String, InputFile> inputFilesByName = new HashMap<>();

        for (InputFile f : fs.inputFiles(fs.predicates().all())) {
            inputFilesByName.put(f.filename(), f);
        }	        
    
        for (PSFinding finding : findings) {
        	
        	String findingPath = finding.getScriptName().replace("\\", "/");

        	// Try exact relative path (preferred & correct)
        	InputFile inputFile =
        	    fs.inputFile(fs.predicates().hasRelativePath(findingPath));

        	// Fallback ONLY if PSA returns filename only
        	if (inputFile == null) {
        	    inputFile = inputFilesByName.get(findingPath);
        	}

        	// If still not found → skip
        	if (inputFile == null) {
        		if(debugOutputEnabled)
        			System.out.println("Skipping finding, file not indexed: " + findingPath);
        	    continue;
        	}
        	
        	if (inputFile.type() == InputFile.Type.TEST) {
        		if(debugOutputEnabled)
        			System.out.println("Skipping test file: " + finding.getScriptName());
                continue;
            }		            
            
        	String testName = finding.getTestName();
            int findingAtLine = Math.max(finding.getLine(), 1);
            String severity = finding.getSeverity();
            String message = finding.getMessage();	            

            if (findingAtLine > inputFile.lines()) {
            	if(debugOutputEnabled)
	                System.out.println(
	                    "Skipping invalid line " + findingAtLine +
	                    " for file " + inputFile.uri().getPath()
                );
                continue;
            }
            
            if(debugOutputEnabled)
            	System.out.println("File has " + inputFile.lines() + " lines. PSA line: " + findingAtLine);  
            
            String ruleKey = ruleKeyForSeverity(finding.getSeverity());
            
            if(debugOutputEnabled)
            	System.out.println(
        		    "[PSA ISSUE] rule=" + ruleKey +
        		    ", TestName=" + testName +
        		    ", file=" + inputFile.uri().getPath() +	        		    
        		    ", line=" + findingAtLine +
        		    ", severity=" + severity +
        		    ", message=" + message
        		);	            
            
            NewIssue issue = context.newIssue();
            
            issue.forRule(RuleKey.of(Constants.REPOSITORY_KEY, ruleKey))
                .at(issue.newLocation()
            		.message(message)            		
            		.on(inputFile).at(inputFile.selectLine(findingAtLine)))
                .save();          
        }
	}
	
	private String ruleKeyForSeverity(String psaSeverity) {
	    if (psaSeverity == null) {
	        return Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR;
	    }

	    switch (psaSeverity.toLowerCase()) {
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
	
	private Severity mapSeverity(String psaSeverity) {
		if (psaSeverity == null) {
	        return Severity.MAJOR;
	    }

	    switch (psaSeverity.toLowerCase()) {
	        case "information":
	            return Severity.INFO;

	        case "warning":
	            return Severity.MINOR;

	        case "error":
	            return Severity.MAJOR;

	        default:
	            return Severity.MAJOR;
	    }
	}	
}
