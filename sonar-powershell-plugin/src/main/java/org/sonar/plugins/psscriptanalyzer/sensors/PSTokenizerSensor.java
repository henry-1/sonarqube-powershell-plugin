package org.sonar.plugins.psscriptanalyzer.sensors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.TempFolder;
import org.sonar.plugins.psscriptanalyzer.Constants;
import org.sonar.plugins.psscriptanalyzer.fillers.CComplexityFiller;
import org.sonar.plugins.psscriptanalyzer.fillers.CpdFiller;
import org.sonar.plugins.psscriptanalyzer.fillers.HalsteadComplexityFiller;
import org.sonar.plugins.psscriptanalyzer.fillers.HighlightingFiller;
import org.sonar.plugins.psscriptanalyzer.fillers.IFiller;
import org.sonar.plugins.psscriptanalyzer.fillers.LineMeasuresFiller;
import org.sonar.plugins.psscriptanalyzer.readers.TokensReader;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;


public class PSTokenizerSensor extends BaseSensor implements org.sonar.api.batch.sensor.Sensor {
		
	private final TempFolder folder;
	private FileSystem fs;	
	private final TokensReader reader = new TokensReader();
	
	private final IFiller[] fillers = new IFiller[] { 
			new CComplexityFiller(config), 
			new LineMeasuresFiller(config), 
			new HalsteadComplexityFiller(config), 
			new CpdFiller(config),
			new HighlightingFiller(config)
			// new SignificantCodeFiller(config)
		};
	
	public PSTokenizerSensor(final TempFolder folder, final Configuration configuration) {
		super(configuration);
        this.folder = folder;
    }
	
	@Override
	public void describe(final SensorDescriptor descriptor) {
		descriptor.name(Constants.TOKENIZER_SENSOR_NAME)
    		.onlyOnLanguage(Constants.PROGRAMMING_LANGUAGE);		
	}
	
	@Override
	public void innerExecute(SensorContext context) {
		
		Boolean debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
		
		fs = context.fileSystem();
	    File baseDir = fs.baseDir();
	    
	    try {   	

	    	final File parserFile = extractScript();	        

	        final String scriptFile = parserFile.getAbsolutePath();
	        
	        
	        final FilePredicates p = fs.predicates();	        
	        ExecutorService service = Executors.newWorkStealingPool();
	    
	        final Iterable<InputFile> inputFiles = fs.inputFiles(p.and(p.hasLanguage(Constants.PROGRAMMING_LANGUAGE)));
	        
	        for (final InputFile inputFile : inputFiles) {
	        	
	        	if (inputFile.type() == InputFile.Type.TEST) {
	        		if(debugOutputEnabled)
	        			System.out.println("Skipping test file: " + inputFile.uri().getPath());
	                continue;
	            }
	        	
	        	final String findingPath = new File(inputFile.uri()).getAbsolutePath();

	        	// skip reporting temp files
	            if (findingPath.contains(".scannerwork")) {
	                continue;
	            }	            
	            
	            service.submit(() -> {
	                try {
	                    final String resultsFile = folder.newFile().toPath().toFile().getAbsolutePath();
	                    
	                    List<String> command = new ArrayList<>();
	                    command.add("powershell.exe");
	            	    command.add("-NoProfile");
	            	    command.add("-NonInteractive");
	            	    command.add("-ExecutionPolicy");
	            	    command.add("Bypass");
	            	    command.add("-File");
	            	    command.add(scriptFile);
	            	    command.add("-inputFile");
	            	    command.add(findingPath);
	            	    command.add("-output");
	            	    command.add(resultsFile);
	            	    
	            	    if(debugOutputEnabled)
	                    {
	            	    	command.add("-debugOutputEnabled");
	                	    command.add(debugOutputEnabled ? "1" : "0");
	                    	
	                    }	            	    
	            	    
                    	if(debugOutputEnabled)
    	        			System.out.println("Running %s command" + Arrays.toString(command.toArray()));
	                    
                    	final Process process = new ProcessBuilder(command)
                    		.directory(baseDir)
                            .redirectErrorStream(true)
                            .start();
	                    
	                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	                        String line;
	                        while ((line = reader.readLine()) != null) {
	                            System.out.println(line);
	                        }
	                    }

	                    final int pReturnValue = process.waitFor();

	                    if (pReturnValue != 0) {
	                    	System.err.println(String.format("Tokenizer did not run successfully on %s file. Error was: %s",
	                    			findingPath, read(process)));	                        
	                        return;
	                    }

	                    final File tokensFile = new File(resultsFile);
	                    if (!tokensFile.exists() || tokensFile.length() <= 0) {
	                    	System.err.println(String.format(
	                                "Tokenizer did not run successfully on %s file. Please check file contents.",
	                                findingPath));	                        
	                        return;
	                    }

	                    final Tokens tokens = reader.read(tokensFile);
	                    for (final IFiller filler : fillers) {
	                        filler.fill(context, inputFile, tokens);
	                    }	                    
	                    

	                    if (debugOutputEnabled) {
	                    	System.out.println(String.format("Running analysis for %s to %s finished.", findingPath,
	                                resultsFile));	                    	
	                    }

	                } catch (final Throwable e) {
	                	System.err.println(String.format("Unexpected exception while running tokenizer on %s. Error is: %s", inputFile, e.getMessage()));
	                }
	            });
	        }	     
	
		} catch (Exception e) {
	        System.err.println("Error running Powershell AST Token Senor: " + e.getMessage());
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
		
	    File tempScript = File.createTempFile("Invoke-LanguageParser", ".ps1");
	    
	    if(removeTempFiles)
	    	tempScript.deleteOnExit();
	
	    try (InputStream in = getClass().getResourceAsStream("/Invoke-LanguageParser.ps1");
	         OutputStream out = new FileOutputStream(tempScript)) {
	
	        if (in == null) {
	            throw new IllegalStateException("Invoke-Analyzer.ps1 not found in resources");
	        }
	        in.transferTo(out);
	    }
	
	    return tempScript;
	}
}
