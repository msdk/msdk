/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.io.txt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.msspectra.MsSpectrum;

public class TxtExportAlgorithm {

    @SuppressWarnings("null")
    public static void exportSpectrum(@Nonnull File exportFile,
            @Nonnull MsSpectrum spectrum) throws IOException {
        exportSpectra(exportFile, Collections.singleton(spectrum));
    }

    public static void exportSpectra(@Nonnull File exportFile,
            @Nonnull Collection<MsSpectrum> spectra) throws IOException {

        // Open the writer
        final BufferedWriter writer = new BufferedWriter(
                new FileWriter(exportFile));

        // Write the data points
        for (MsSpectrum spectrum : spectra) {
            spectrumToWriter(spectrum, writer);
        }

        writer.close();

    }

    public static void spectrumToWriter(@Nonnull MsSpectrum spectrum,
            @Nonnull Writer writer) throws IOException {

        double mzValues[] = spectrum.getMzValues();
        float intensityValues[] = spectrum.getIntensityValues();
        int numOfDataPoints = spectrum.getNumberOfDataPoints();

        for (int i = 0; i < numOfDataPoints; i++) {
            // Write data point row
            writer.write(mzValues[i] + " " + intensityValues[i]);
            writer.write(System.lineSeparator());
        }

    }

    @SuppressWarnings("null")
    public static @Nonnull String spectrumToString(
            @Nonnull MsSpectrum spectrum) {

        StringWriter sw = new StringWriter();
        try {
            spectrumToWriter(spectrum, sw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }
}
