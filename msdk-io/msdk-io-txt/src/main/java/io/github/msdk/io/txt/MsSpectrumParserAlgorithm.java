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

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.spectra.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.ArrayUtil;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;

public class MsSpectrumParserAlgorithm {

    private static final Pattern linePattern = Pattern
            .compile("(\\d+(\\.\\d+)?)[^\\d]+(\\d+(\\.\\d+)?)");

    public static @Nonnull MsSpectrum parseMsSpectrum(
            @Nonnull String spectrumText) {

        double mzValues[] = new double[16];
        float intensityValues[] = new float[16];
        int size = 0;

        Scanner scanner = new Scanner(spectrumText);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher m = linePattern.matcher(line);
            if (!m.find())
                continue;

            String mzString = m.group(1);
            String intensityString = m.group(3);

            double mz = Double.parseDouble(mzString);
            float intensity = Float.parseFloat(intensityString);
            mzValues = ArrayUtil.addToArray(mzValues, mz, size);
            intensityValues = ArrayUtil.addToArray(intensityValues, intensity,
                    size);
            size++;
        }
        scanner.close();

        // Sort the data points, in case they were not ordered
        DataPointSorter.sortDataPoints(mzValues, intensityValues, size,
                SortingProperty.MZ, SortingDirection.ASCENDING);

        MsSpectrumType specType = SpectrumTypeDetectionAlgorithm
                .detectSpectrumType(mzValues, intensityValues, size);
        MsSpectrum result = MSDKObjectBuilder.getMsSpectrum(mzValues,
                intensityValues, size, specType);

        return result;

    }
}
