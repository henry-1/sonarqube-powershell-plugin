package org.sonar.plugins.psscriptanalyzer;

import java.nio.file.Path;
import java.util.List;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.psscriptanalyzer.common.Common;

public class PowershellQualityProfile implements BuiltInQualityProfilesDefinition {
	
	private final Configuration config;
	
	public PowershellQualityProfile(final Configuration configuration) {
        this.config = configuration;
    }
	
	
    @Override
    public void define(Context context) {
    	
    	Boolean debugOutputEnabled = config
				.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
				.orElse(false);
    	
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(
        		Constants.QUALITY_PROFILE_NAME, 
        		Constants.PROGRAMMING_LANGUAGE);
        profile.setDefault(true);

        // Activate your generic PSA rule
        profile.activateRule(Constants.REPOSITORY_KEY, Constants.SENSOR_RULE_TYPE_GENERAL_PSA_ERROR);
           
        // Activate static fall-back Rules
        profile.activateRule(Constants.REPOSITORY_KEY, Constants.SENSOR_RULE_TYPE_PSA_FINDING_INFO);
        profile.activateRule(Constants.REPOSITORY_KEY, Constants.SENSOR_RULE_TYPE_PSA_FINDING_WARNING);
        profile.activateRule(Constants.REPOSITORY_KEY,Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR);
        
        
     // Activate dynamic Rules
        
 	   String psaRulesPath = config
                .get(Constants.CONFIGURAITON_PROPERTY_PSARULES_FOLDERPATH)
                .orElse("");
 	   
 	   List<Path> rulesDefinitions = Common.readAllXmlFiles(psaRulesPath);
 	   for(Path path: rulesDefinitions) {
 		  String fileName = path.getFileName().toString();
 		    int dotIndex = fileName.lastIndexOf('.');
 		    String nameWithoutExtension = (dotIndex == -1) 
 		        ? fileName 
 		        : fileName.substring(0, dotIndex);
 		    
	    	profile.activateRule(Constants.REPOSITORY_KEY,nameWithoutExtension);
 		    
	    	if(debugOutputEnabled)
 		    	System.out.println("[PSA-Plugin] Rule activated: " + nameWithoutExtension);
 	   }	   
 	   
        
        profile.done();
    }
}