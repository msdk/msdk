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

package io.github.msdk.io.msp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;

public class MspExportAlgorithm {

    @SuppressWarnings("null")
    public static void exportSpectrum(@Nonnull File exportFile,
            @Nonnull MsSpectrum spectrum) throws IOException, MSDKException {
        exportSpectra(exportFile, Collections.singleton(spectrum));
    }

    public static void exportSpectra(@Nonnull File exportFile,
            @Nonnull Collection<MsSpectrum> spectra)
                    throws IOException, MSDKException {

        // Open the writer
        final BufferedWriter writer = new BufferedWriter(
                new FileWriter(exportFile));

        double mzValues[] = null;
        float intensityValues[] = null;
        int numOfDataPoints;

        // Write the data points
        for (MsSpectrum spectrum : spectra) {

            // Load data
            mzValues = spectrum.getMzValues(mzValues);
            intensityValues = spectrum.getIntensityValues(intensityValues);
            numOfDataPoints = spectrum.getNumberOfDataPoints();

            for (int i = 0; i < numOfDataPoints; i++) {
                // Write data point row
                writer.write(mzValues[i] + " " + intensityValues[i]);
                writer.newLine();
            }
        }
        
        
    }

}
