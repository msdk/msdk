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

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.spectra.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;

public class MsSpectrumParserAlgorithm {

    private static final Pattern linePattern = Pattern
            .compile("(\\d+(\\.\\d+)?)[^\\d]+(\\d+(\\.\\d+)?)");

    public static @Nonnull MsSpectrum parseMsSpectrum(
            @Nonnull String spectrumText) {

        DoubleBuffer mzValues = DoubleBuffer.allocate(16);
        FloatBuffer intensityValues = FloatBuffer.allocate(16);

        Scanner scanner = new Scanner(spectrumText);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = linePattern.matcher(line);
            if (!m.find())
                continue;

            String mzString = m.group(1);
            String intensityString = m.group(3);

            try {
                double mz = Double.parseDouble(mzString);
                float intensity = Float.parseFloat(intensityString);
                mzValues.put(mz);
                intensityValues.put(intensity);
            } catch (Exception e) {
                // Ignore misformatted lines
                continue;
            }
        }
        scanner.close();

        MsSpectrumType specType = SpectrumTypeDetectionAlgorithm
                .detectSpectrumType(mzValues.array(), intensityValues.array(),
                        mzValues.position());
        MsSpectrum result = MSDKObjectBuilder.getMsSpectrum(mzValues.array(),
                intensityValues.array(), mzValues.position(), specType);

        return result;

    }
}
