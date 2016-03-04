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

package io.github.msdk.featdet.srmdetection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * This class creates a feature table based on the SRM chromatograms from a raw
 * data file.
 */
public class SrmDetectionMethod implements MSDKMethod<FeatureTable> {

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull Double minHeight;
    private final @Nonnull Double intensityTolerance;
    private final @Nonnull DataPointStore dataStore;

    private FeatureTable result;
    private boolean canceled = false;
    private int processedChromatograms = 0, totalChromatograms = 0;

    /**
     * <p>
     * Constructor for SrmDetectionMethod.
     * </p>
     *
     * @param rawDataFile
     *            a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param dataPointStore
     *            a {@link io.github.msdk.datamodel.datastore.DataPointStore}
     *            object.
     */
    public SrmDetectionMethod(@Nonnull RawDataFile rawDataFile,
            @Nonnull DataPointStore dataStore, @Nonnull Double minHeight,
            @Nonnull Double intensityTolerance, @Nonnull String nameSuffix) {
        this.rawDataFile = rawDataFile;
        this.minHeight = minHeight;
        this.intensityTolerance = intensityTolerance;
        this.dataStore = dataStore;

        // Make a new feature table
        result = MSDKObjectBuilder
                .getFeatureTable(rawDataFile.getName() + nameSuffix, dataStore);
    }

    /** {@inheritDoc} */
    @Override
    public FeatureTable execute() throws MSDKException {
        List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
        totalChromatograms = chromatograms.size();

        // Iterate over all chromatograms
        for (Chromatogram chromatogram : chromatograms) {
            // Canceled
            if (canceled)
                return null;

            // Ignore non SRM chromatograms
            if (chromatogram
                    .getChromatogramType() != ChromatogramType.MRM_SRM) {
                totalChromatograms += -1;
                continue;
            }

            List<IsolationInfo> isolations = chromatogram.getIsolations();
            for (IsolationInfo isolation : isolations) {
                /*
                 * TODO: 1. Find Q1 and Q3 values. Possibly use the precursor or
                 * product to the isolation? The Q1 value is the m/z value
                 */
            }

            // Chromatogram data
            ChromatographyInfo[] rtValues = chromatogram.getRetentionTimes();
            float[] intensityValues = chromatogram.getIntensityValues();
            Integer size = intensityValues.length;

            // Peak apex HashMap
            Map<Float, ChromatographyInfo> peakMap = new HashMap<Float, ChromatographyInfo>();

            // Iterate over all data points in the chromatogram to find the
            // peaks
            float intensity = 0f;
            Boolean fallingIntensity = false;
            for (int i = 0; i < size; i++) {
                float currentIntensity = intensityValues[i];

                // Ignore intensities below threshold
                if (currentIntensity < minHeight)
                    continue;

                // Intensity is falling
                if (currentIntensity < intensity) {
                    if (!fallingIntensity) {
                        // Add the peak apex to the peakMap
                        peakMap.put(intensity, rtValues[i]);
                        fallingIntensity = true;
                    }
                    intensity = currentIntensity;
                } else {
                    fallingIntensity = false;
                    intensity = currentIntensity;
                }

            }

            // Iterate over the peakMap
            Iterator it = peakMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                intensity = (float) pair.getKey();
                ChromatographyInfo chromatographyInfo = (ChromatographyInfo) pair.getValue();
                Float rtValue = chromatographyInfo.getRetentionTime();
                
                /*
                 * TODO: Generate the XIC for the features
                 * If possible then re-use the method from the TargetedDetection module for this
                 */
                System.out.println(intensity + " : " + rtValue);
                it.remove();
            }

            /*
             * TODO: Group features by identical Q1 and RT values
             */

            /*
             * TODO: Add Q1 and Q3 columns to ColumnNames?
             */

            processedChromatograms++;
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalChromatograms == 0 ? null
                : (float) processedChromatograms / totalChromatograms;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public FeatureTable getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
