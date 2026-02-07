package org.sonar.plugins.psscriptanalyzer.sensors;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.fs.FileSystem;

import org.sonar.api.config.Configuration;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.fillers.IssuesFiller;
import org.sonar.plugins.psscriptanalyzer.readers.FindingsReader;
import org.sonar.plugins.psscriptanalyzer.types.PSFinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;


public class PSScriptAnalyzerSensor implements Sensor {

	private final Configuration config;
	private FileSystem fs;	
	
	private final FindingsReader findingsReader = new FindingsReader();
	private final IssuesFiller issuesFiller = new IssuesFiller();

    public PSScriptAnalyzerSensor(Configuration config, FileSystem fileSystem) {
    	this.config = config;
        this.fs = fileSystem;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name(Constants.PSSCRIPTANALYZER_SENSOR_NAME)
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
		
	    fs = context.fileSystem();
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
	        List<PSFinding> findings = findingsReader.read(outFile);
	        this.issuesFiller.fill(context, baseDir, findings, debugOutputEnabled);        
	        
	
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
		
	    File tempScript = File.createTempFile("Invoke-Analyzer", ".ps1");
	    if(removeTempFiles)
	    	tempScript.deleteOnExit();
	
	    try (InputStream in = getClass().getResourceAsStream("/Invoke-Analyzer.ps1");
	         OutputStream out = new FileOutputStream(tempScript)) {
	
	        if (in == null) {
	            throw new IllegalStateException("Invoke-Analyzer.ps1 not found in resources");
	        }
	        in.transferTo(out);
	    }
	
	    return tempScript;
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
		
		String excludeRule = config
                .get(Constants.CONFIGURAITON_PROPERTY_EXCLUDERULE)
                .orElse("")
                .replaceAll("\\s+", ""); // remove all spaces, tabs, newlines
		
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
	    if (customRulesPath != null && !customRulesPath.isBlank()) 
	    {	        
	        command.add(customRulesPath.replace("\\", "/"));
	    }
	    
	    if(excludeRule != null && !excludeRule.isBlank())
	    {
	    	command.add("-excludeRule");
	    	command.add(excludeRule);
	    }
	    
	    command.add("-runPester");
	    command.add(runPesterTests ? "1" : "0");	    
	    command.add("-debugOutputEnabled");
	    command.add(debugOutputEnabled ? "1" : "0");	        
	    
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
	
}

