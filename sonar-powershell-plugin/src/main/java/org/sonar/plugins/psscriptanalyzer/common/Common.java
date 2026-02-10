package org.sonar.plugins.psscriptanalyzer.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Common {
	
	/**
	 * Extracts the bundled PowerShell script to a temp file
	 */
	public final static File extractScript(String scriptName, Boolean removeTempFiles) throws IllegalStateException {
					
		try {
		    File tempScript = File.createTempFile(scriptName, ".ps1");
		    if(removeTempFiles)
		    	tempScript.deleteOnExit();
		    
		    try (InputStream in = Common.class.getResourceAsStream("/" + scriptName + ".ps1");
			         OutputStream out = new FileOutputStream(tempScript)) {
			
			        if (in == null) {
			            throw new IllegalStateException("Invoke-Analyzer.ps1 not found in resources");
			        }
			        in.transferTo(out);
			    }
		    return tempScript;
		}
		catch(Exception e) {
			throw new IllegalStateException("Unable to create " + scriptName);
		}	    
	}
	
	public final static List<Path> readAllXmlFiles(String directory) {
    	
    	List<Path> paths = new ArrayList<>();
    	
        if (directory == null || directory.isBlank()) {
        	System.err.println(String.format("Exception 0 in readAllXmlFiles: directory not found")); 
            return paths;
        }

        Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
        	System.err.println(String.format("Exception 1 in readAllXmlFiles: no files fond in directory"));  
            return paths;
        }

        try (Stream<Path> files = Files.list(dirPath)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xml"))
                    .collect(Collectors.toList());
        }
        catch (Exception e) {
        	System.err.println(String.format("Exception 2 in readAllXmlFiles: %s", e.getMessage())); 
        	return paths;
        }        
    }
}
