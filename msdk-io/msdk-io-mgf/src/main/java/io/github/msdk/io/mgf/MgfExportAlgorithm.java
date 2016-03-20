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

package io.github.msdk.io.mgf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;

/**
 * <p>MgfExportAlgorithm class.</p>
 *
 */
public class MgfExportAlgorithm {

    /**
     * <p>exportSpectrum.</p>
     *
     * @param exportFile a {@link java.io.File} object.
     * @param spectrum a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     * @throws java.io.IOException if any.
     * @throws io.github.msdk.MSDKException if any.
     */
    @SuppressWarnings("null")
    public static void exportSpectrum(@Nonnull File exportFile,
            @Nonnull MsSpectrum spectrum) throws IOException, MSDKException {
        exportSpectra(exportFile, Collections.singleton(spectrum));
    }

    /**
     * <p>exportSpectra.</p>
     *
     * @param exportFile a {@link java.io.File} object.
     * @param spectra a {@link java.util.Collection} object.
     * @throws java.io.IOException if any.
     * @throws io.github.msdk.MSDKException if any.
     */
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

            writer.write("BEGIN IONS");
            writer.newLine();

            if (spectrum instanceof MsScan) {
                MsScan scan = (MsScan) spectrum;

                for (IsolationInfo ii : scan.getIsolations()) {
                    Double precursorMz = ii.getPrecursorMz();
                    if (precursorMz == null)
                        continue;
                    writer.write("PEPMASS=" + precursorMz);
                    writer.newLine();
                    if (ii.getPrecursorCharge() != null) {
                        writer.write("CHARGE=" + ii.getPrecursorCharge());
                        writer.newLine();
                    }
                    break;
                }

                ChromatographyInfo rt = scan.getChromatographyInfo();
                if (rt != null) {
                    writer.write("RTINSECONDS=" + rt.getRetentionTime());
                    writer.newLine();
                }
                writer.write("Title=Scan #" + scan.getScanNumber());
                writer.newLine();

            }

            // Write ions
            for (int i = 0; i < numOfDataPoints; i++) {
                writer.write(mzValues[i] + " " + intensityValues[i]);
                writer.newLine();
            }

            writer.write("END IONS");
            writer.newLine();
            writer.newLine();

        }

        writer.close();

    }
}
