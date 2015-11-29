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

package io.github.msdk.io.mzml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ActivationType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.SeparationType;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.ParamGroup;
import uk.ac.ebi.jmzml.model.mzml.Precursor;
import uk.ac.ebi.jmzml.model.mzml.PrecursorList;
import uk.ac.ebi.jmzml.model.mzml.Scan;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.SelectedIonList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;

/**
 * This class provides conversions between the jmzml data model and the MSDK
 * data model
 */
class MzMLConverter {

    private int lastScanNumber = 0;

    private Map<String, Integer> scanIdTable = new Hashtable<String, Integer>();

    @Nonnull
    Integer extractScanNumber(Spectrum spectrum) {

        String spectrumId = spectrum.getId();

        if (scanIdTable.containsKey(spectrumId))
            return scanIdTable.get(spectrumId);

        final Pattern pattern = Pattern.compile("scan=([0-9]+)");
        final Matcher matcher = pattern.matcher(spectrumId);
        boolean scanNumberFound = matcher.find();

        // Some vendors include scan=XX in the ID, some don't, such as
        // mzML converted from WIFF files. See the definition of nativeID in
        // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
        if (scanNumberFound) {
            Integer scanNumber = Integer.parseInt(matcher.group(1));
            lastScanNumber = scanNumber;
            scanIdTable.put(spectrumId, scanNumber);
            return scanNumber;
        }

        Integer scanNumber = lastScanNumber + 1;
        lastScanNumber++;
        scanIdTable.put(spectrumId, scanNumber);
        return scanNumber;
    }

    @Nonnull
    MsFunction extractMsFunction(Spectrum spectrum) {
        Integer msLevel = 1;
        // Browse the spectrum parameters
        List<CVParam> cvParams = spectrum.getCvParam();
        if (cvParams != null) {
            for (CVParam param : cvParams) {
                String accession = param.getAccession();
                String value = param.getValue();
                if ((accession == null) || (value == null))
                    continue;

                // MS level MS:1000511
                if (accession.equals("MS:1000511")) {
                    msLevel = Integer.parseInt(value);
                }
            }
        }
        return MSDKObjectBuilder.getMsFunction(msLevel);
    }

    @Nullable
    ChromatographyInfo extractChromatographyData(Spectrum spectrum) {

        ScanList scanListElement = spectrum.getScanList();
        if (scanListElement == null)
            return null;
        List<Scan> scanElements = scanListElement.getScan();
        if (scanElements == null)
            return null;

        for (Scan scan : scanElements) {
            List<CVParam> cvParams = scan.getCvParam();
            if (cvParams == null)
                continue;

            for (CVParam param : cvParams) {
                String accession = param.getAccession();
                String unitAccession = param.getUnitAccession();
                String value = param.getValue();
                if ((accession == null) || (value == null))
                    continue;

                // Retention time (actually "Scan start time") MS:1000016
                if (accession.equals(MzMLCV.cvScanStartTime)) {
                    try {
                        float retentionTime;
                        if ((unitAccession == null)
                                || (unitAccession.equals(MzMLCV.cvUnitsMin1))
                                || unitAccession.equals(MzMLCV.cvUnitsMin2)) {
                            // Minutes
                            retentionTime = Float.parseFloat(value) * 60f;
                        } else {
                            // Seconds
                            retentionTime = Float.parseFloat(value);
                        }
                        ChromatographyInfo chromInfo = MSDKObjectBuilder
                                .getChromatographyInfo1D(SeparationType.UNKNOWN,
                                        retentionTime);
                        return chromInfo;
                    } catch (Exception e) {
                        // Ignore incorrectly formatted numbers, just dump the
                        // exception
                        e.printStackTrace();
                    }

                }
            }
        }

        return null;
    }

    @Nonnull
    String extractScanDefinition(Spectrum spectrum) {
        List<CVParam> cvParams = spectrum.getCvParam();
        if (cvParams != null) {
            for (CVParam param : cvParams) {
                String accession = param.getAccession();

                if (accession == null)
                    continue;
                if (accession.equals(MzMLCV.cvScanFilterString))
                    return param.getValue();
            }
        }
        ScanList scanListElement = spectrum.getScanList();
        if (scanListElement != null) {
            List<Scan> scanElements = scanListElement.getScan();
            if (scanElements != null) {
                for (Scan scan : scanElements) {
                    cvParams = scan.getCvParam();
                    if (cvParams == null)
                        continue;
                    for (CVParam param : cvParams) {
                        String accession = param.getAccession();
                        if (accession == null)
                            continue;
                        if (accession.equals(MzMLCV.cvScanFilterString))
                            return param.getValue();
                    }

                }
            }
        }
        return spectrum.getId();
    }

    @Nonnull
    MsScanType extractScanType(Spectrum spectrum) {
        return MsScanType.UNKNOWN;
    }

    @Nonnull
    PolarityType extractPolarity(Spectrum spectrum) {
        List<CVParam> cvParams = spectrum.getCvParam();
        if (cvParams != null) {
            for (CVParam param : cvParams) {
                String accession = param.getAccession();

                if (accession == null)
                    continue;
                if (accession.equals(MzMLCV.cvPolarityPositive))
                    return PolarityType.POSITIVE;
                if (accession.equals(MzMLCV.cvPolarityNegative))
                    return PolarityType.NEGATIVE;
            }
        }
        ScanList scanListElement = spectrum.getScanList();
        if (scanListElement != null) {
            List<Scan> scanElements = scanListElement.getScan();
            if (scanElements != null) {
                for (Scan scan : scanElements) {
                    cvParams = scan.getCvParam();
                    if (cvParams == null)
                        continue;
                    for (CVParam param : cvParams) {
                        String accession = param.getAccession();
                        if (accession == null)
                            continue;
                        if (accession.equals(MzMLCV.cvPolarityPositive))
                            return PolarityType.POSITIVE;
                        if (accession.equals(MzMLCV.cvPolarityNegative))
                            return PolarityType.NEGATIVE;
                    }

                }
            }
        }
        return PolarityType.UNKNOWN;

    }

    @Nullable
    ActivationInfo extractSourceFragmentation(Spectrum spectrum) {
        return null;
    }

    @Nonnull
    List<IsolationInfo> extractIsolations(Spectrum spectrum) {
        PrecursorList precursorListElement = spectrum.getPrecursorList();
        if ((precursorListElement == null)
                || (precursorListElement.getCount().equals(0)))
            return Collections.emptyList();

        Double precursorMz = null;
        Integer precursorCharge = null;
        List<Precursor> precursorList = precursorListElement.getPrecursor();
        for (Precursor parent : precursorList) {

            SelectedIonList selectedIonListElement = parent
                    .getSelectedIonList();
            if ((selectedIonListElement == null)
                    || (selectedIonListElement.getCount().equals(0)))
                return Collections.emptyList();
            List<ParamGroup> selectedIonParams = selectedIonListElement
                    .getSelectedIon();
            if (selectedIonParams == null)
                continue;

            for (ParamGroup pg : selectedIonParams) {
                List<CVParam> pgCvParams = pg.getCvParam();
                for (CVParam param : pgCvParams) {
                    String accession = param.getAccession();
                    String value = param.getValue();
                    if ((accession == null) || (value == null))
                        continue;
                    // cvMz is sometimes used is used in mzML 1.0 files
                    if (accession.equals(MzMLCV.cvMz)
                            || accession.equals(MzMLCV.cvPrecursorMz)) {
                        precursorMz = Double.parseDouble(value);

                    }
                    if (accession.equals(MzMLCV.cvChargeState)) {
                        precursorCharge = Integer.parseInt(value);
                    }
                }

            }
        }
        if (precursorMz != null) {
            @SuppressWarnings("null")
            IsolationInfo isolation = MSDKObjectBuilder.getIsolationInfo(
                    Range.singleton(precursorMz), null, precursorMz,
                    precursorCharge, null);
            return ImmutableList.<IsolationInfo> builder().add(isolation)
                    .build();
        }
        return Collections.emptyList();
    }

    @Nonnull
    SeparationType extractSeparationType(Spectrum spectrum) {
        return SeparationType.UNKNOWN;
    }

    /**
     * <p>
     * extractSeparationType.
     * </p>
     *
     * @param chromatogram
     *            a {@link uk.ac.ebi.jmzml.model.mzml.Chromatogram} object.
     * @return a {@link io.github.msdk.datamodel.rawdata.SeparationType} object.
     */
    @Nonnull
    public SeparationType extractSeparationType(
            uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram) {
        return SeparationType.UNKNOWN;
    }

    /**
     * <p>
     * extractChromatogramType.
     * </p>
     *
     * @param chromatogram
     *            a {@link uk.ac.ebi.jmzml.model.mzml.Chromatogram} object.
     * @return a {@link io.github.msdk.datamodel.chromatograms.ChromatogramType}
     *         object.
     */
    @Nonnull
    public ChromatogramType extractChromatogramType(
            uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram) {
        List<CVParam> cvParams = chromatogram.getCvParam();
        cvParams = chromatogram.getCvParam();

        if (cvParams != null) {
            for (CVParam param : cvParams) {
                String accession = param.getAccession();

                if (accession == null)
                    continue;
                if (accession.equals(MzMLCV.cvChromatogramTIC))
                    return ChromatogramType.TIC;
                if (accession.equals(MzMLCV.cvChromatogramMRM_SRM))
                    return ChromatogramType.MRM_SRM;
                if (accession.equals(MzMLCV.cvChromatogramSIC))
                    return ChromatogramType.SIC;
                if (accession.equals(MzMLCV.cvChromatogramBPC))
                    return ChromatogramType.BPC;
            }
        }

        return ChromatogramType.UNKNOWN;
    }

    /**
     * <p>
     * extractIsolations.
     * </p>
     *
     * @param chromatogram
     *            a {@link uk.ac.ebi.jmzml.model.mzml.Chromatogram} object.
     * @return a {@link java.util.List} object.
     */
    @Nonnull
    public List<IsolationInfo> extractIsolations(
            uk.ac.ebi.jmzml.model.mzml.Chromatogram chromatogram) {
        if (extractChromatogramType(chromatogram) == ChromatogramType.MRM_SRM) {
            List<CVParam> cvParams;
            Double precursorIsolationMz = null, productIsolationMz = null,
                    precursorActivationEnergy = null;
            ActivationType precursorActivation = ActivationType.UNKNOWN;
            ActivationInfo activationInfo = null;

            // Precursor isolation window
            cvParams = chromatogram.getPrecursor().getIsolationWindow()
                    .getCvParam();
            if (cvParams != null) {
                for (CVParam param : cvParams) {
                    if (param.getAccession().equals(MzMLCV.cvIsolationWindow)) {
                        precursorIsolationMz = Double
                                .parseDouble(param.getValue());
                        break;
                    }
                }
            }

            // Precursor activation
            cvParams = chromatogram.getPrecursor().getActivation().getCvParam();
            if (cvParams != null) {
                for (CVParam param : cvParams) {
                    if (param.getAccession().equals(MzMLCV.cvActivationCID))
                        precursorActivation = ActivationType.CID;
                    if (param.getAccession().equals(MzMLCV.cvActivationEnergy))
                        precursorActivationEnergy = Double
                                .parseDouble(param.getValue());
                }
            }

            // Product isolation window
            cvParams = chromatogram.getProduct().getIsolationWindow()
                    .getCvParam();
            if (cvParams != null) {
                for (CVParam param : cvParams) {
                    if (param.getAccession().equals(MzMLCV.cvIsolationWindow)) {
                        productIsolationMz = Double
                                .parseDouble(param.getValue());
                        break;
                    }
                }
            }

            if (precursorActivationEnergy != null) {
                activationInfo = MSDKObjectBuilder.getActivationInfo(
                        precursorActivationEnergy, precursorActivation);
            }

            List<IsolationInfo> isolations = new ArrayList<>();
            IsolationInfo isolationInfo = null;

            if (precursorIsolationMz != null) {
                isolationInfo = MSDKObjectBuilder.getIsolationInfo(
                        Range.singleton(precursorIsolationMz), null,
                        precursorIsolationMz, null, activationInfo);
                isolations.add(isolationInfo);
            }

            if (productIsolationMz != null) {
                isolationInfo = MSDKObjectBuilder.getIsolationInfo(
                        Range.singleton(productIsolationMz), null,
                        productIsolationMz, null, null);
                isolations.add(isolationInfo);
            }

            return isolations;
        }

        return Collections.emptyList();

    }

    static void extractDataPoints(Spectrum spectrum,
            MsSpectrumDataPointList dataPointList) {

        dataPointList.clear();

        BinaryDataArrayList dataList = spectrum.getBinaryDataArrayList();

        if ((dataList == null) || (dataList.getCount().equals(0)))
            return;

        // Obtain the data arrays from spectrum
        final BinaryDataArray mzArray = dataList.getBinaryDataArray().get(0);
        final BinaryDataArray intensityArray = dataList.getBinaryDataArray()
                .get(1);
        final Number mzValues[] = mzArray.getBinaryDataAsNumberArray();
        final Number intensityValues[] = intensityArray
                .getBinaryDataAsNumberArray();

        // Allocate space for the data points
        dataPointList.allocate(mzValues.length);
        final double mzBuffer[] = dataPointList.getMzBuffer();
        final float intensityBuffer[] = dataPointList.getIntensityBuffer();

        // Copy the actual data point values
        for (int i = 0; i < mzValues.length; i++) {
            mzBuffer[i] = mzValues[i].doubleValue();
            intensityBuffer[i] = intensityValues[i].floatValue();
        }

        // Commit the changes
        dataPointList.setSize(mzValues.length);

    }

    static void extractDataPoints(Spectrum spectrum,
            MsSpectrumDataPointList dataPointList,
            @Nonnull Range<Double> mzRange,
            @Nonnull Range<Float> intensityRange) {

        dataPointList.clear();

        BinaryDataArrayList dataList = spectrum.getBinaryDataArrayList();

        if ((dataList == null) || (dataList.getCount().equals(0)))
            return;

        // Obtain the data arrays from spectrum
        final BinaryDataArray mzArray = dataList.getBinaryDataArray().get(0);
        final BinaryDataArray intensityArray = dataList.getBinaryDataArray()
                .get(1);
        final Number mzValues[] = mzArray.getBinaryDataAsNumberArray();
        final Number intensityValues[] = intensityArray
                .getBinaryDataAsNumberArray();

        // Find how many data points will pass the conditions
        int numOfGoodDataPoints = 0;
        for (int i = 0; i < mzValues.length; i++) {
            if (!mzRange.contains(mzValues[i].doubleValue()))
                continue;
            if (!intensityRange.contains(intensityValues[i].floatValue()))
                continue;
            numOfGoodDataPoints++;
        }

        if (numOfGoodDataPoints == 0)
            return;

        // Allocate space for the data points
        dataPointList.allocate(numOfGoodDataPoints);
        final double mzBuffer[] = dataPointList.getMzBuffer();
        final float intensityBuffer[] = dataPointList.getIntensityBuffer();

        // Copy the actual data point values
        int newIndex = 0;
        for (int i = 0; i < mzValues.length; i++) {
            mzBuffer[newIndex] = mzValues[i].doubleValue();
            intensityBuffer[newIndex] = intensityValues[i].floatValue();
            newIndex++;
        }

        // Commit the changes
        dataPointList.setSize(numOfGoodDataPoints);

    }

    static void extractDataPoints(
            uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram,
            ChromatogramDataPointList dataPointList) {

        dataPointList.clear();

        BinaryDataArrayList dataList = jmzChromatogram.getBinaryDataArrayList();

        if ((dataList == null) || (dataList.getCount().equals(0)))
            return;

        // Obtain the data arrays from spectrum
        final BinaryDataArray rtArray = dataList.getBinaryDataArray().get(0);
        final BinaryDataArray intensityArray = dataList.getBinaryDataArray()
                .get(1);
        final Number rtValues[] = rtArray.getBinaryDataAsNumberArray();
        final Number intensityValues[] = intensityArray
                .getBinaryDataAsNumberArray();

        // Allocate space for the data points
        dataPointList.allocate(rtValues.length);
        final ChromatographyInfo rtBuffer[] = dataPointList.getRtBuffer();
        final float intensityBuffer[] = dataPointList.getIntensityBuffer();

        // Copy the actual data point values
        for (int i = 0; i < rtValues.length; i++) {
            final float rt = rtValues[i].floatValue();
            final float intensity = intensityValues[i].floatValue();
            final ChromatographyInfo chromatographyInfo = MSDKObjectBuilder
                    .getChromatographyInfo1D(SeparationType.UNKNOWN, rt);
            rtBuffer[i] = chromatographyInfo;
            intensityBuffer[i] = intensity;
        }

        // Commit the changes
        dataPointList.setSize(rtValues.length);
    }

    static @Nullable Double extractMz(
            uk.ac.ebi.jmzml.model.mzml.Chromatogram jmzChromatogram) {
        return null;
    }

}
