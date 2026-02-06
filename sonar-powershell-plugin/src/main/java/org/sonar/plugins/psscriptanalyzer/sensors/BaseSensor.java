package org.sonar.plugins.psscriptanalyzer.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.psscriptanalyzer.Constants;

public abstract class BaseSensor implements Sensor {
	
	protected final Configuration config;
	
	public BaseSensor(Configuration configuration) {
        this.config = configuration;
    }

	@Override
	public void describe(final SensorDescriptor descriptor) {
		descriptor.name(this.getClass().getSimpleName())
    		.onlyOnLanguage(Constants.PROGRAMMING_LANGUAGE);
		
	}

	@Override
    public void execute(final SensorContext context) {
        // Fetch skip flag from configuration
		//Boolean debugOutputEnabled = config
		//		.getBoolean(Constants.CONFIGURAITON_PROPERTY_DEBUGOUTPUTENABLED)
		//		.orElse(false);

        //if (skipPlugin) {
            
        //    return;
        //}

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
