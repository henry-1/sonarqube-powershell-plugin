package org.sonar.plugins.psscriptanalyzer;

// http://sonarqube.example.com:8088/api/measures/component?component=sqp_d34478c50d2ba4f5432031709418d2dbd0ccc6d6&metricKeys=coverage,lines_to_cover,uncovered_lines

// http://sonarqube.example.com:8088/api/measures/component_tree?component=Local-Sonar-Plugin-Test&s=metric&metricSort=ncloc&asc=false&ps=500&p=1&component=PROJECT_KEY&metricKeys=software_quality_maintainability_remediation_effort%2Ccoverage%2Cncloc%2Csoftware_quality_reliability_rating%2Csoftware_quality_security_rating&strategy=leaves


import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class PowershellLanguage extends AbstractLanguage {

    // Language key used across the plugin
    public static final String KEY = Constants.PROGRAMMING_LANGUAGE;

    // Human-readable name
    public static final String NAME = Constants.PROGRAMMING_LANGUAGE_NAME;

    // File suffixes for PowerShell
    private static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

    //private final Configuration config;
    
    
    public PowershellLanguage(final Configuration configuration) {
        super(KEY, NAME);
        //this.config = configuration;
    }

    @Override
    public String[] getFileSuffixes() {
        return DEFAULT_FILE_SUFFIXES;
    }
}