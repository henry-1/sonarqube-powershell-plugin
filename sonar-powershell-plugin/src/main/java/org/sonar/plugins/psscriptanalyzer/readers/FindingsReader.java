
package org.sonar.plugins.psscriptanalyzer.readers;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sonar.plugins.psscriptanalyzer.types.PSFinding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FindingsReader {
	public List<PSFinding> read(final File jsonFile)  throws IOException{
		
		ObjectMapper mapper = new ObjectMapper();
	    List<Map<String, Object>> psaResults = mapper.readValue(
	            jsonFile,
	            new TypeReference<List<Map<String, Object>>>() {}
	    );

	    List<PSFinding> results = new ArrayList<>();
	    for (Map<String, Object> item : psaResults) {
	        String testName = (String) item.get("RuleName");
	        String message = (String) item.get("Message");
	        String scriptName = (String) item.get("ScriptName"); // relative path inside project
	        int lineNumber = item.get("Line") != null ? (int) item.get("Line") : 1;
	        String severity = item.get("Severity") != null ? (String)item.get("Severity") : "Error";

	        results.add(new PSFinding(testName, message, scriptName, lineNumber, severity));
	    }

	    return results;
		
	}
	
}
