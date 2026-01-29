package org.sonar.plugins.powershell;

import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.config.Configuration;

public class PowershellLanguage extends AbstractLanguage {

    public static final String NAME = "Powershell";
    public static final String PROFILE_NAME = "Powershell default rules";
    public static final String KEY = "ps";

    private final Configuration configuration;
    private static final String[] DEFAULT_FILE_SUFFIXES = new String[] { "ps1", "psm1", "psd1" };

    public PowershellLanguage(final Configuration configuration) {
        super(KEY, NAME);
        this.configuration = configuration;
    }

    public String[] getFileSuffixes() {
        String[] suffixes = configuration.get("sonar.ps.file.suffixes").orElse("").split(",");
        if (suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].isEmpty())) {
            return DEFAULT_FILE_SUFFIXES;
        }
        return suffixes;
    }
}


