package org.sonar.plugins.psscriptanalyzer;


import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class PowershellLanguage extends AbstractLanguage {

    // Language key used across the plugin
    public static final String KEY = Constants.PROGRAMMING_LANGUAGE;

    // Human-readable name
    public static final String NAME = Constants.PROGRAMMING_LANGUAGE_NAME;

    // File suffixes for PowerShell
    private static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

    private final Configuration configuration;
    
    
    public PowershellLanguage(final Configuration configuration) {
        super(KEY, NAME);
        this.configuration = configuration;
    }

    @Override
    public String[] getFileSuffixes() {
        return DEFAULT_FILE_SUFFIXES;
    }
}