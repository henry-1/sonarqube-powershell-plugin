package org.sonar.plugins.psscriptanalyzer;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class PowershellQualityProfile implements BuiltInQualityProfilesDefinition {
    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(
        		Constants.QUALITY_PROFILE_NAME, 
        		Constants.PROGRAMMING_LANGUAGE);

     // Activate your generic PSA rule
        profile.activateRule(Constants.REPOSITORY_KEY, Constants.SENSOR_RULE_TYPE_GENERAL_PSA_ERROR);
                
        profile.activateRule(Constants.REPOSITORY_KEY, Constants.SENSOR_RULE_TYPE_PSA_FINDING_INFO);
        profile.activateRule(Constants.REPOSITORY_KEY, Constants.SENSOR_RULE_TYPE_PSA_FINDING_WARNING);
        profile.activateRule(Constants.REPOSITORY_KEY,Constants.SENSOR_RULE_TYPE_PSA_FINDING_ERROR);
        
        profile.done();
    }
}