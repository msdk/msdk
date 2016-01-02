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

package io.github.msdk.io.mzxml;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import com.google.common.base.Strings;

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.SeparationType;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;

/**
 * This class provides conversions from the jmzreader data model to MSDK data
 * model
 */
class MzXMLConverter {

    private DatatypeFactory dataTypeFactory;

    MzXMLConverter() {
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }

    MsFunction extractMsFunction(Spectrum spectrum) {
        Integer msLevel = spectrum.getMsLevel();
        return MSDKObjectBuilder.getMsFunction(msLevel);
    }

    ChromatographyInfo extractChromatographyData(Spectrum spectrum) {

        ParamGroup params = spectrum.getAdditional();

        for (CvParam cvParam : params.getCvParams()) {
            final String accession = cvParam.getAccession();
            if (Strings.isNullOrEmpty(accession))
                continue;
            if (MzXMLCV.cvScanRetentionTime.equals(accession)) {
                String value = cvParam.getValue();
                if (Strings.isNullOrEmpty(value))
                    continue;

                Date currentDate = new Date();
                Duration dur = dataTypeFactory.newDuration(value);
                float rt = dur.getTimeInMillis(currentDate) / 1000f;

                return MSDKObjectBuilder
                        .getChromatographyInfo1D(SeparationType.UNKNOWN, rt);
            }
        }

        return null;
    }

    MsScanType extractScanType(Spectrum spectrum) {
        return MsScanType.UNKNOWN;
    }

    PolarityType extractPolarity(Spectrum spectrum) {
        ParamGroup params = spectrum.getAdditional();

        for (CvParam cvParam : params.getCvParams()) {
            final String accession = cvParam.getAccession();
            if (Strings.isNullOrEmpty(accession))
                continue;
            if (MzXMLCV.cvScanPolarity.equals(accession)) {
                String value = cvParam.getValue();
                if (Strings.isNullOrEmpty(value))
                    continue;
                if ("+".equals(value))
                    return PolarityType.POSITIVE;
                if ("-".equals(value))
                    return PolarityType.NEGATIVE;
            }
        }
        return PolarityType.UNKNOWN;
    }

    ActivationInfo extractSourceFragmentation(Spectrum spectrum) {
        return null;
    }

    List<IsolationInfo> extractIsolations(Spectrum spectrum) {
        return Collections.emptyList();
    }

    static @Nonnull double[] extractMzValues(Spectrum spectrum,
            double[] array) {

        Map<Double, Double> jmzreaderPeakList = spectrum.getPeakList();

        // Allocate space for the data points
        if ((array == null) || (array.length < jmzreaderPeakList.size()))
            array = new double[jmzreaderPeakList.size()];

        // Copy the actual data point values
        int newIndex = 0;
        for (Double mz : jmzreaderPeakList.keySet()) {
            array[newIndex] = mz.doubleValue();
            newIndex++;
        }

        return array;
    }

    static @Nonnull float[] extractIntensityValues(Spectrum spectrum,
            float[] array) {

        Map<Double, Double> jmzreaderPeakList = spectrum.getPeakList();

        // Allocate space for the data points
        if ((array == null) || (array.length < jmzreaderPeakList.size()))
            array = new float[jmzreaderPeakList.size()];

        // Copy the actual data point values
        int newIndex = 0;
        for (Double mz : jmzreaderPeakList.keySet()) {
            array[newIndex] = jmzreaderPeakList.get(mz).floatValue();
            newIndex++;
        }

        return array;
    }

}
