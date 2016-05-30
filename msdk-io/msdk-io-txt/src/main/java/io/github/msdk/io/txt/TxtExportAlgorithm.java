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

/**
 * <p>TxtExportAlgorithm class.</p>
 */
public class TxtExportAlgorithm {

    /**
     * <p>exportSpectrum.</p>
     *
     * @param exportFile a {@link java.io.File} object.
     * @param spectrum a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     * @throws java.io.IOException if any.
     */
    @SuppressWarnings("null")
    public static void exportSpectrum(@Nonnull File exportFile,
            @Nonnull MsSpectrum spectrum) throws IOException {
        exportSpectra(exportFile, Collections.singleton(spectrum));
    }

    /**
     * <p>exportSpectra.</p>
     *
     * @param exportFile a {@link java.io.File} object.
     * @param spectra a {@link java.util.Collection} object.
     * @throws java.io.IOException if any.
     */
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

    /**
     * <p>spectrumToWriter.</p>
     *
     * @param spectrum a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     * @param writer a {@link java.io.Writer} object.
     * @throws java.io.IOException if any.
     */
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

    /**
     * <p>spectrumToString.</p>
     *
     * @param spectrum a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     * @return a {@link java.lang.String} object.
     */
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
