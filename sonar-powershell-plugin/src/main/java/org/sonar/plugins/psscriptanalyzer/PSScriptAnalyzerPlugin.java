package org.sonar.plugins.psscriptanalyzer;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.psscriptanalyzer.sensors.PSScriptAnalyzerSensor;
import org.sonar.plugins.psscriptanalyzer.sensors.PSTokenizerSensor;


public class PSScriptAnalyzerPlugin implements Plugin {

    @Override
    public void define(Context context) {
    	
    	String version = getClass().getPackage().getImplementationVersion();
        System.out.println("PSScriptAnalyzer Plugin Version: " + version);
    	
    	context.addExtensions(
            PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_CUSTOMRULESPATH)
                .name(Constants.CONFIGURAITON_PROPERTY_CUSTOMRULESPATH_NAME)
                .description(Constants.CONFIGURAITON_CUSTOMRULESPATH_DESCRIPTION)
                .type(PropertyType.TEXT)
                .defaultValue(Constants.CONFIGURAITON_CUSTOMRULESPATH_DEFAULT_VALUE)
                .category(Constants.REPOSITORY_NAME)
                .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
                .onQualifiers(Qualifiers.PROJECT)                
                .build()
            ,
            PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_CUSTOMRULESENABLED)
	            .name(Constants.CONFIGURAITON_PROPERTY_CUSTOMRULESENABLED_NAME)
	            .description(Constants.CONFIGURAITON_CUSTOMRULESENABLED_DESCRIPTION)
	            .type(PropertyType.BOOLEAN)
	            .defaultValue(Constants.CONFIGURAITON_CUSTOMRULESENABLED_DEFAULT_VALUE)
	            .category(Constants.REPOSITORY_NAME)
	            .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
	            .onQualifiers(Qualifiers.PROJECT)
	            .build()
	        ,
	        PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_DEFAULTRULESENABLED)
	            .name(Constants.CONFIGURAITON_PROPERTY_DEFAULTRULESENABLED_NAME)
	            .description(Constants.CONFIGURAITON_DEFAULTRULESENABLED_DESCRIPTION)
	            .type(PropertyType.BOOLEAN)
	            .defaultValue(Constants.CONFIGURAITON_DEFAULTRULESENABLED_DEFAULT_VALUE)
	            .category(Constants.REPOSITORY_NAME)
	            .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
	            .onQualifiers(Qualifiers.PROJECT)
	            .build()
            ,
	        PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_EXCLUDERULE)
	            .name(Constants.CONFIGURAITON_PROPERTY_EXCLUDERULE_NAME)
	            .description(Constants.CONFIGURAITON_EXCLUDERULE_DESCRIPTION)
	            .type(PropertyType.TEXT)
	            .defaultValue(Constants.CONFIGURAITON_DEFAULEXCLUDERULE_DEFAULT_VALUE)
	            .category(Constants.REPOSITORY_NAME)
	            .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
	            .onQualifiers(Qualifiers.PROJECT)
	            .build()
	        ,
            PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_PESTERENABLED)
                .name(Constants.CONFIGURAITON_PROPERTY_PESTERENABLED_NAME)
                .description(Constants.CONFIGURAITON_PESTERENABLED_DESCRIPTION)
                .type(PropertyType.BOOLEAN)
                .defaultValue(Constants.CONFIGURAITON_PESTERENABLED_DEFAULT_VALUE)
                .category(Constants.REPOSITORY_NAME)
                .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
                .onQualifiers(Qualifiers.PROJECT)
                .build()            
	    	,
	        PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
	            .name(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED_NAME)
	            .description(Constants.CONFIGURAITON_DEBUGOUTPUTENABLED_DESCRIPTION)
	            .type(PropertyType.BOOLEAN)
	            .defaultValue(Constants.CONFIGURAITON_DEBUGOUTPUTENABLED_DEFAULT_VALUE)
	            .category(Constants.REPOSITORY_NAME)
	            .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
	            .onQualifiers(Qualifiers.PROJECT)
	            .build()
            ,
	        PropertyDefinition.builder(Constants.CONFIGURAITON_PROPERTY_REMOVETEMPFILES)
	            .name(Constants.CONFIGURAITON_PROPERTY_REMOVETEMPFILES_NAME)
	            .description(Constants.CONFIGURAITON_REMOVETEMPFILES_DESCRIPTION)
	            .type(PropertyType.BOOLEAN)
	            .defaultValue(Constants.CONFIGURAITON_REMOVETEMPFILES_DEFAULT_VALUE)
	            .category(Constants.REPOSITORY_NAME)
	            .subCategory(Constants.CONFIGURATION_PROPERTY_SUBCATEGORY)
	            .onQualifiers(Qualifiers.PROJECT)
	            .build()
	        );
    	
    	
    	context.addExtensions(PowershellLanguage.class,
    			PSScriptAnalyzerRulesDefinition.class);
    	
    	context.addExtensions(PowershellQualityProfile.class,
    			PSScriptAnalyzerSensor.class);
    	
    	context.addExtension(PSTokenizerSensor.class);
    }
}