/* 
 * (C) Copyright 2015 by MSDK Development Team
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

package io.github.msdk.featuredetection.targeteddetection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.ChromatogramUtil.calcMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.MsSpectrumUtil;
import io.github.msdk.util.RTTolerance;
import io.github.msdk.util.RawDataFileUtil;

/**
 * This class creates a list of Chromatograms for a RawDataFile based the
 * inputted list of IonAnnotations.
 */
public class TargetedDetectionMethod implements MSDKMethod<List<Chromatogram>> {

    private final @Nonnull List<IonAnnotation> ionAnnotations;
    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull MZTolerance mzTolerance;
    private final @Nonnull RTTolerance rtTolerance;
    private final @Nonnull Double intensityTolerance;
    private final @Nonnull Double noiseLevel;

    private List<Chromatogram> result;
    private boolean canceled = false;
    private int processedScans = 0, totalScans = 0;

    /**
     * <p>Constructor for TargetedDetectionMethod.</p>
     *
     * @param ionAnnotations a {@link java.util.List} object.
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param mzTolerance a {@link io.github.msdk.util.MZTolerance} object.
     * @param rtTolerance a {@link io.github.msdk.util.RTTolerance} object.
     * @param intensityTolerance a {@link java.lang.Double} object.
     * @param noiseLevel a {@link java.lang.Double} object.
     */
    public TargetedDetectionMethod(@Nonnull List<IonAnnotation> ionAnnotations,
            @Nonnull RawDataFile rawDataFile,
            @Nonnull DataPointStore dataPointStore,
            @Nonnull MZTolerance mzTolerance, @Nonnull RTTolerance rtTolerance,
            @Nonnull Double intensityTolerance, @Nonnull Double noiseLevel) {
        this.ionAnnotations = ionAnnotations;
        this.rawDataFile = rawDataFile;
        this.dataPointStore = dataPointStore;
        this.mzTolerance = mzTolerance;
        this.rtTolerance = rtTolerance;
        this.intensityTolerance = intensityTolerance;
        this.noiseLevel = noiseLevel;
    }

    /** {@inheritDoc} */
    @Override
    public List<Chromatogram> execute() throws MSDKException {

        result = new ArrayList<Chromatogram>();
        List<BuildingChromatogram> tempChromatogramList = new ArrayList<BuildingChromatogram>();
        int chromatogramNumber = RawDataFileUtil
                .getNextChromatogramNumber(rawDataFile);

        // Variables
        Chromatogram chromatogram;
        BuildingChromatogram buildingChromatogram;
        ChromatogramDataPointList newDataPoints;
        int ionNr;

        // Create at new building chromatogram for all ions
        for (int i = 0; i < ionAnnotations.size(); i++) {
            BuildingChromatogram newChromatogram = new BuildingChromatogram();
            tempChromatogramList.add(newChromatogram);
        }

        // Loop through all MS scans in the raw data file
        List<MsScan> msScans = rawDataFile.getScans();
        totalScans = msScans.size();
        for (MsScan msScan : msScans) {

            // Get the scans data points
            MsSpectrumDataPointList dataPointList = MSDKObjectBuilder
                    .getMsSpectrumDataPointList();
            msScan.getDataPoints(dataPointList);
            ChromatographyInfo chromatographyInfo = msScan
                    .getChromatographyInfo();

            // Loop through all the ions in the ion annotation list
            ionNr = 0;
            for (IonAnnotation ionAnnotation : ionAnnotations) {
                Double ionMz = ionAnnotation.getExpectedMz();
                if (ionMz != null) {
                    Range<Double> mzRange = mzTolerance
                            .getToleranceRange(ionMz);

                    // Get highest data point from the MS dataPointList which
                    // has a m/z within the mzRange
                    Double mz = 0d;
                    Float intensity = 0f;
                    Integer index = MsSpectrumUtil
                            .getBasePeakIndex(dataPointList, mzRange);
                    if (index != null) {
                        mz = dataPointList.getMzBuffer()[index];
                        intensity = dataPointList.getIntensityBuffer()[index];
                    }

                    // Add this mzPeak or zero values to the chromatogram
                    buildingChromatogram = tempChromatogramList.get(ionNr);
                    buildingChromatogram.addDataPoint(chromatographyInfo, mz,
                            intensity);

                }
                ionNr++;
            }

            processedScans++;

            if (canceled)
                return null;

        }

        // Loop through all the ions in the ion annotation list
        ionNr = 0;
        for (IonAnnotation ionAnnotation : ionAnnotations) {

            // Temporary chromatogram
            buildingChromatogram = tempChromatogramList.get(ionNr);

            // Find the most intense data point and crop the chromatogram based
            // on the input parameters
            Double ionRt = (double) ionAnnotation.getChromatographyInfo()
                    .getRetentionTime();
            Range<Double> rtRange = rtTolerance.getToleranceRange(ionRt);
            buildingChromatogram.cropChromatogram(rtRange, intensityTolerance,
                    noiseLevel);

            // Final chromatogram
            chromatogram = MSDKObjectBuilder.getChromatogram(dataPointStore,
                    chromatogramNumber, ChromatogramType.XIC,
                    SeparationType.UNKNOWN);

            // Add the data points to the final chromatogram
            newDataPoints = buildingChromatogram.getDataPoints();
            if (newDataPoints != null) {
                chromatogram.setDataPoints(newDataPoints);
            }

            // Set the m/z value for the chromatogram
            double[] mzValues = buildingChromatogram.getMzValues();
            float[] intensityValues = newDataPoints.getIntensityBuffer();
            double newMz = ChromatogramUtil.calculateMz(intensityValues, mzValues, calcMethod.allAverage);
            chromatogram.setMz(newMz);

            // Add the ion annotation to the chromatogram
            chromatogram.setIonAnnotation(ionAnnotation);

            // Add the chromatogram to the chromatogram list
            result.add(chromatogram);
            chromatogramNumber++;
            ionNr++;
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) processedScans / totalScans;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public List<Chromatogram> getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
