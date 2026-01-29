package org.sonar.plugins.powershell.sensors;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.fillers.IssuesFiller;
import org.sonar.plugins.powershell.issues.PsIssue;
import org.sonar.plugins.powershell.readers.IssuesReader;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.TempFolder;
import org.sonar.api.batch.fs.FileSystem;

public class ScriptAnalyzerSensor extends BaseSensor {

    private final TempFolder folder;
    private static final Logger LOGGER = Loggers.get(ScriptAnalyzerSensor.class);

    private final IssuesFiller issuesFiller = new IssuesFiller();
    private final IssuesReader reader = new IssuesReader();

    // Placeholder for the Powershell executable property
    private static final String PS_EXECUTABLE_PROPERTY = "sonar.ps.executable"; // Replace with Constants.PS_EXECUTABLE

    public ScriptAnalyzerSensor(final TempFolder folder, final Configuration configuration) {
        super(configuration); // Pass configuration to BaseSensor
        this.folder = folder;
    }

    @Override
    protected void innerExecute(final SensorContext context) {

        // Get Powershell executable from configuration
        final String powershellExecutable = super.configuration
                .get(PS_EXECUTABLE_PROPERTY)
                .orElse("powershell"); // Default fallback

        try {
            // Create temporary script file
            final File parserFile = folder.newFile("ps", "scriptAnalyzer.ps1");
            try {
                FileUtils.copyURLToFile(getClass().getResource("/scriptAnalyzer.ps1"), parserFile);
            } catch (final Throwable e1) {
                LOGGER.warn("Exception while copying tokenizer script", e1);
                return;
            }

            final String scriptFile = parserFile.getAbsolutePath();

            final FileSystem fileSystem = context.fileSystem();
            final File baseDir = fileSystem.baseDir();

            final String sourceDir = SystemUtils.IS_OS_WINDOWS
                    ? String.format("'%s'", baseDir.getAbsolutePath())
                    : baseDir.getAbsolutePath();

            final String outFile = folder.newFile().toPath().toFile().getAbsolutePath();

            final String[] args = new String[] { powershellExecutable, scriptFile, "-inputDir", sourceDir, "-output", outFile };

            LOGGER.info(String.format("Starting Script-Analyzer using powershell: %s", Arrays.toString(args)));
            final Process process = new ProcessBuilder(args).inheritIO().start();

            final int pReturnValue = process.waitFor();

            if (pReturnValue != 0) {
                LOGGER.info(String.format(
                        "Error executing Powershell Script-Analyzer. Maybe Script-Analyzer is not installed? Error was: %s",
                        read(process)));
                return;
            }

            final File outputFile = new File(outFile);
            if (!outputFile.exists() || outputFile.length() <= 0) {
                LOGGER.warn("Analysis was not run ok, and output file was empty at: " + outFile);
                return;
            }

            final List<PsIssue> issues = reader.read(outputFile);
            this.issuesFiller.fill(context, baseDir, issues);

            LOGGER.info(String.format("Script-Analyzer finished, found %s issues at %s", issues.size(), sourceDir));

        } catch (Throwable e) {
            LOGGER.warn("Unexpected exception while running analysis", e);
        }
    }
}
