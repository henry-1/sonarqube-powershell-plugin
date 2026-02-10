package org.sonar.plugins.psscriptanalyzer.fillers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
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
        			System.out.println("[PSA-Plugin] Skipping finding, file not indexed: " + findingPath);
        	    continue;
        	}
        	
        	if (inputFile.type() == InputFile.Type.TEST) {
        		if(debugOutputEnabled)
        			System.out.println("[PSA-Plugin] Skipping test file: " + finding.getScriptName());
                continue;
            }		            
            
        	String testName = finding.getTestName();
            int findingAtLine = Math.max(finding.getLine(), 1);
            String severity = finding.getSeverity();
            	            

            if (findingAtLine > inputFile.lines()) {
            	if(debugOutputEnabled)
	                System.out.println(
	                    "[PSA-Plugin] Skipping invalid line " + findingAtLine +
	                    " for file " + inputFile.uri().getPath()
                );
                continue;
            }
            
            if(debugOutputEnabled)
            	System.out.println("[PSA-Plugin] File has " + inputFile.lines() + " lines. PSA line: " + findingAtLine);  
                        
            String stringForRuleKey = testName;
            String message = testName;
            
            RuleKey ruleKey = RuleKey.of(Constants.REPOSITORY_KEY, stringForRuleKey);
            ActiveRule activeRule = context.activeRules().find(ruleKey);
            
            if(activeRule == null)
            {
            	stringForRuleKey = finding.getRuleKey();  
            	message = finding.getMessage();
            }
            
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
            
            issue.forRule(RuleKey.of(Constants.REPOSITORY_KEY, stringForRuleKey))
                .at(issue.newLocation()
            		.message(message)            		
            		.on(inputFile).at(inputFile.selectLine(findingAtLine)))
                .save();          
        }
	}
}
