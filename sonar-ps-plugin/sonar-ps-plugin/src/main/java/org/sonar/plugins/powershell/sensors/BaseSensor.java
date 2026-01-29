package org.sonar.plugins.powershell.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.powershell.Constants;
import org.sonar.plugins.powershell.PowershellLanguage;

public abstract class BaseSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(BaseSensor.class);

    protected final Configuration configuration;

    // Constructor: Spring will inject Configuration automatically
    public BaseSensor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(PowershellLanguage.KEY)
                  .name(this.getClass().getSimpleName());
    }

    @Override
    public void execute(final SensorContext context) {
        // Fetch skip flag from configuration
        final boolean skipPlugin = configuration.get(Constants.SKIP_PLUGIN)
                                                .map(Boolean::parseBoolean)
                                                .orElse(false);

        if (skipPlugin) {
            LOGGER.debug("Skipping sensor as skip plugin flag is set: " + Constants.SKIP_PLUGIN);
            return;
        }

        // Run the actual sensor logic
        innerExecute(context);
    }

    protected abstract void innerExecute(final SensorContext context);

    // Helper to read process output
    protected static String read(Process process) throws IOException {
        return "input: " + read(process.getInputStream()) + 
               " error: " + read(process.getErrorStream());
    }

    protected static String read(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
            return builder.toString();
        }
    }
}

