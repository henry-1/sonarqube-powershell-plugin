package org.sonar.plugins.psscriptanalyzer;

public class Constants {
	public static final String CONFIGURAITON_PROPERTY_CUSTOMRULESPATH = "psscriptanalyzer.customrules.path";
	public static final String CONFIGURAITON_PROPERTY_CUSTOMRULESPATH_NAME = "Custom Rules Path";
	public static final String CONFIGURAITON_CUSTOMRULESPATH_DEFAULT_VALUE = "c:\\DEV\\PSScriptAnalyzerRules\\*.psm1";
	public static final String CONFIGURAITON_CUSTOMRULESPATH_DESCRIPTION = "Path to the directory containing custom PSScriptAnalyzer rules.";
	
	public static final String CONFIGURAITON_PROPERTY_CUSTOMRULESENABLED = "psscriptanalyzer.customrules.enabled";
	public static final String CONFIGURAITON_PROPERTY_CUSTOMRULESENABLED_NAME = "Enable Custom Rules";
	public static final String CONFIGURAITON_CUSTOMRULESENABLED_DEFAULT_VALUE = "false";
	public static final String CONFIGURAITON_CUSTOMRULESENABLED_DESCRIPTION = "Enable Custom Rules for PSScriptAnalyzer.";
	
	public static final String CONFIGURAITON_PROPERTY_DEFAULTRULESENABLED = "psscriptanalyzer.defaultrules.enabled";
	public static final String CONFIGURAITON_PROPERTY_DEFAULTRULESENABLED_NAME = "Enable Default Rules";
	public static final String CONFIGURAITON_DEFAULTRULESENABLED_DEFAULT_VALUE = "true";
	public static final String CONFIGURAITON_DEFAULTRULESENABLED_DESCRIPTION = "Enable default Rules for PSScriptAnalyzer.";
	
	public static final String CONFIGURAITON_PROPERTY_EXCLUDERULE = "psscriptanalyzer.exclude.rule";
	public static final String CONFIGURAITON_PROPERTY_EXCLUDERULE_NAME = "Exclude Rule";
	public static final String CONFIGURAITON_DEFAULEXCLUDERULE_DEFAULT_VALUE = "";
	public static final String CONFIGURAITON_EXCLUDERULE_DESCRIPTION = "Comma-seperated list of Rule Names to be excluded from Scan.";
	
	public static final String CONFIGURAITON_PROPERTY_PESTERENABLED = "psscriptanalyzer.pester.enabled";
	public static final String CONFIGURAITON_PROPERTY_PESTERENABLED_NAME = "Run Pester";
	public static final String CONFIGURAITON_PESTERENABLED_DEFAULT_VALUE = "false";
	public static final String CONFIGURAITON_PESTERENABLED_DESCRIPTION = "Run Pester Tests to get Insights about Code Coverage. Pester Module Version 5.5.0 or newer is required.";
	
	public static final String CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED = "psscriptanalyzer.debugoutput.enabled";
	public static final String CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED_NAME = "Output Debug Info";
	public static final String CONFIGURAITON_DEBUGOUTPUTENABLED_DEFAULT_VALUE = "false";
	public static final String CONFIGURAITON_DEBUGOUTPUTENABLED_DESCRIPTION = "Enable Debug Out of Analyzer Run Cycle.";
	
	public static final String CONFIGURAITON_PROPERTY_REMOVETEMPFILES = "psscriptanalyzer.removetempfiles";
	public static final String CONFIGURAITON_PROPERTY_REMOVETEMPFILES_NAME = "Remove Intermediate Files";
	public static final String CONFIGURAITON_REMOVETEMPFILES_DEFAULT_VALUE = "true";
	public static final String CONFIGURAITON_REMOVETEMPFILES_DESCRIPTION = "Remove intermediate Files after Scan.";
	
	public static final String CONFIGURATION_PROPERTY_SUBCATEGORY = "Rules";
	
	public static final String REPOSITORY_NAME = "PSScriptAnalyzer";
	public static final String REPOSITORY_KEY = "psscriptanalyzer";
	
	public static final String SENSOR_NAME = "PowerShell Script Analyzer Sensor";
	
	public static final String SENSOR_RULE_TYPE_GENERAL_PSA_ERROR = "analyzer-error";	
	public static final String SENSOR_RULE_TYPE_PSA_FINDING = "psscriptanalyzer-issue";	
	public static final String SENSOR_RULE_TYPE_PSA_FINDING_INFO = "psa-info";
	public static final String SENSOR_RULE_TYPE_PSA_FINDING_WARNING = "psa-warning";
	public static final String SENSOR_RULE_TYPE_PSA_FINDING_ERROR = "psa-error";
	
	public static final String PROGRAMMING_LANGUAGE_NAME = "Powershell";
	public static final String PROGRAMMING_LANGUAGE = "powershell";
	
	public static final String QUALITY_PROFILE_NAME = "Powershell Quality Profile";
}
