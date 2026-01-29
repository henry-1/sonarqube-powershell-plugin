package org.sonar.plugins.psscriptanalyzer;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.batch.rule.Severity;

import org.sonar.api.config.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PSScriptAnalyzerSensor implements Sensor {

	private final Configuration config;
	private final FileSystem fileSystem;	
	
    public PSScriptAnalyzerSensor(Configuration config, FileSystem fileSystem) {
    	this.config = config;
        this.fileSystem = fileSystem;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name(Constants.SENSOR_NAME)
                  .onlyOnLanguage(Constants.PROGRAMMING_LANGUAGE); // Must match RulesDefinition
    }

    @Override
	public void execute(SensorContext context) {    	
		Boolean debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
		Boolean removeTempFiles = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_REMOVETEMPFILES)
				.orElse(true);
		
	    FileSystem fs = context.fileSystem();
	    File baseDir = fs.baseDir();
	
	    try {   	
		    
	        // Extract bundled PowerShell script to temp file
	        File scriptFile = extractScript();	        
	
	        // Define output JSON in the system temp folder
	        String tmpRoot = System.getProperty("java.io.tmpdir");
	        File outFile = new File(tmpRoot, "psa-findings.json");
	        if(removeTempFiles)
	        	outFile.deleteOnExit();
	        
	        if(debugOutputEnabled)
	        {
		        System.out.println("PowerShell script extracted to: " + scriptFile.getAbsolutePath());
		        System.out.println("Analyzer output will go to: " + outFile.getAbsolutePath());
	        }
	
	        // Run PowerShell ScriptAnalyzer on entire baseDir
	        runScriptAnalyzer(baseDir, scriptFile, outFile);
	
	        // Read findings from JSON
	        List<RuleFinding> findings = readFindings(outFile);
	        
	        if(debugOutputEnabled)
	        	System.out.println("Findings Count: " + findings.size());
	        
	        Map<String, InputFile> inputFilesByName = new HashMap<>();

	        for (InputFile f : fs.inputFiles(fs.predicates().all())) {
	            inputFilesByName.put(f.filename(), f);
	        }
	        
	        for (RuleFinding finding : findings) {
	        	
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
	        	
	            String ruleKey = ruleKeyForSeverity(finding.getSeverity());
	            
	            if(debugOutputEnabled)
	            	System.out.println(
	        		    "[PSA ISSUE] rule=" + ruleKey +
	        		    ", file=" + inputFile.relativePath() +
	        		    ", abs=" + inputFile.absolutePath() +
	        		    ", line=" + finding.getLine() +
	        		    ", severity=" + finding.getSeverity() +
	        		    ", message=" + finding.getMessage()
	        		);
	            
	            int findingAtLine = Math.max(finding.getLine(), 1);

	            if (findingAtLine > inputFile.lines()) {
	            	if(debugOutputEnabled)
		                System.out.println(
		                    "Skipping invalid line " + findingAtLine +
		                    " for file " + inputFile.relativePath()
	                );
	                continue;
	            }
	            
	            if(debugOutputEnabled)
	            	System.out.println("File has " + inputFile.lines() + " lines. PSA line: " + findingAtLine);  
	            	            
	            NewIssue issue = context.newIssue()
	                .forRule(RuleKey.of(Constants.REPOSITORY_KEY, ruleKey));
	                       
	            NewIssueLocation location = issue.newLocation()
	                .on(inputFile)
	                .message(finding.getMessage())
	                .at(inputFile.selectLine(findingAtLine));
	            
	            issue.at(location)
	            	.overrideSeverity(mapSeverity(finding.getSeverity()))
	                .save();
	        }
	
	    } catch (Exception e) {
	        System.err.println("Error running ScriptAnalyzer: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	/**
	 * Extracts the bundled PowerShell script to a temp file
	 */
	private File extractScript() throws IOException {
		
		Boolean removeTempFiles = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_REMOVETEMPFILES)
				.orElse(true);
		
	    File tempScript = File.createTempFile("Run-Analyzer", ".ps1");
	    if(removeTempFiles)
	    	tempScript.deleteOnExit();
	
	    try (InputStream in = getClass().getResourceAsStream("/Run-Analyzer.ps1");
	         OutputStream out = new FileOutputStream(tempScript)) {
	
	        if (in == null) {
	            throw new IllegalStateException("Run-Analyzer.ps1 not found in resources");
	        }
	        in.transferTo(out);
	    }
	
	    return tempScript;
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
	
	/**
	 * Runs PowerShell ScriptAnalyzer on all scripts in the folder
	 */
	private void runScriptAnalyzer(File baseDir, File scriptFile, File outFile) throws IOException, InterruptedException {
	    
		Boolean removeTempFiles = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_REMOVETEMPFILES)
				.orElse(true);
		
		String customRulesPath = config
                .get(Constants.CONFIGURAITON_PROPERTY_CUSTOMRULESPATH)
                .orElse("");
		
		Boolean enableCustomRules = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_CUSTOMRULESENABLED)
				.orElse(false);
		
		Boolean enableDefaultRules = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEFAULTRULESENABLED)
				.orElse(true);
		
		Boolean runPesterTests = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_PESTERENABLED)
				.orElse(false);
		
		Boolean debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
		
		//String customRulesPath = "C:\\DEV\\PSScriptAnalyzerRules\\*.psm1";
		
		List<String> command = new ArrayList<>();
	    command.add("powershell.exe");
	    command.add("-NoProfile");
	    command.add("-NonInteractive");
	    command.add("-ExecutionPolicy");
	    command.add("Bypass");
	    command.add("-File");
	    command.add(scriptFile.getAbsolutePath());
	    command.add("-inputDir");
	    command.add(baseDir.getAbsolutePath());
	    command.add("-outputDir");
	    command.add(outFile.getAbsolutePath());
	    command.add("-includeDefaultRules");	    
	    command.add(enableDefaultRules ? "1" : "0");	
	    command.add("-includeCustomRules");
	    command.add(enableCustomRules ? "1" : "0");
	    
	    command.add("-CustomRulesPath");
	    if (!customRulesPath.isBlank()) 
	    {	        
	        command.add(customRulesPath.replace("\\", "/"));
	    }
	    
	    command.add("-runPester");
	    command.add(runPesterTests ? "1" : "0");	    
	    command.add("-debugOutputEnabled");
	    command.add(debugOutputEnabled ? "1" : "0");
	    command.add("-removeTempFiles");
	    command.add(removeTempFiles ? "1" : "0");
	    
	    
	    ProcessBuilder pb = new ProcessBuilder(command)
		    		.inheritIO()
		    		.redirectErrorStream(true);

	    Process process = pb.start();

	    // Print process output for debugging
	    try (BufferedReader reader = new BufferedReader(
	    		new InputStreamReader(process.getInputStream(), 
	    		StandardCharsets.UTF_8))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            System.out.println(line);
	        }
	    }

	    int exitCode = process.waitFor();
	    if (exitCode != 0) {
	        throw new RuntimeException("PowerShell ScriptAnalyzer failed with exit code " + exitCode);
	    }
	}

	/**
	 * Reads the JSON output from ScriptAnalyzer
	 */
	private List<RuleFinding> readFindings(File jsonFile) throws IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    List<Map<String, Object>> psaResults = mapper.readValue(
	            jsonFile,
	            new TypeReference<List<Map<String, Object>>>() {}
	    );

	    List<RuleFinding> results = new ArrayList<>();
	    for (Map<String, Object> item : psaResults) {
	        String ruleName = (String) item.get("RuleName");
	        String message = (String) item.get("Message");
	        String scriptName = (String) item.get("ScriptName"); // relative path inside project
	        int lineNumber = item.get("Line") != null ? (int) item.get("Line") : 1;
	        String severity = item.get("Severity") != null ? (String)item.get("Severity") : "Error";

	        results.add(new RuleFinding(ruleName, message, scriptName, lineNumber, severity));
	    }

	    return results;
	}
	

	/**
	 * Data holder for a finding
	 */
	private static class RuleFinding {
	    private final String testName;
	    private final String message;
	    private final String scriptName;
	    private final int line;
	    private final String severity; 

	    public RuleFinding(String ruleKey, String message, String scriptName, int line, String severity) {
	        this.testName = ruleKey;
	        this.message = message;
	        this.scriptName = scriptName;
	        this.line = line;
	        this.severity = severity;
	    }

	    public String getMessage() { return ("Test: " + testName + " -> " + message); }
	    public String getScriptName() { return scriptName; }
	    public int getLine() { return line; }
	    public String getSeverity() { return severity; }
	}
}

