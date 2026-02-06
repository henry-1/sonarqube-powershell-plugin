package org.sonar.plugins.psscriptanalyzer.fillers;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;

public interface IFiller {
	void fill(final SensorContext context, final InputFile f, final Tokens tokens);
}
