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

package io.github.msdk.io.rawdataimport.netcdf;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionMethod;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * <p>NetCDFFileImportMethod class.</p>
 *
 */
public class NetCDFFileImportMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private NetcdfFile inputFile;

    private int parsedScans, totalScans = 0;

    private int scanStartPositions[];
    private float scanRetentionTimes[];

    private final @Nonnull File sourceFile;
    private final @Nonnull FileType fileType = FileType.NETCDF;
    private final @Nonnull DataPointStore dataStore;

    private RawDataFile newRawFile;
    private boolean canceled = false;

    private Variable massValueVariable, intensityValueVariable;

    // Some software produces netcdf files with a scale factor such as 0.05
    // TODO: need junit test for this
    private double massValueScaleFactor = 1;
    private double intensityValueScaleFactor = 1;

    private final @Nonnull MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
            .getMsSpectrumDataPointList();

    /**
     * <p>Constructor for NetCDFFileImportMethod.</p>
     *
     * @param sourceFile a {@link java.io.File} object.
     * @param dataStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     */
    public NetCDFFileImportMethod(@Nonnull File sourceFile,
            @Nonnull DataPointStore dataStore) {
        this.sourceFile = sourceFile;
        this.dataStore = dataStore;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    public RawDataFile execute() throws MSDKException {

        logger.info("Started parsing file " + sourceFile);

        // Check if the file is readable
        if (!sourceFile.canRead()) {
            throw new MSDKException("Cannot read file " + sourceFile);
        }

        String fileName = sourceFile.getName();
        newRawFile = MSDKObjectBuilder.getRawDataFile(fileName, sourceFile,
                fileType, dataStore);

        try {

            // Open NetCDF-file
            inputFile = NetcdfFile.open(sourceFile.getPath());

            // Read NetCDF variables
            readVariables();

            // Parse scans
            for (int scanIndex = 0; scanIndex < totalScans; scanIndex++) {

                // Check if cancel is requested
                if (canceled) {
                    return null;
                }

                MsScan buildingScan = readNextScan(scanIndex);
                newRawFile.addScan(buildingScan);
                parsedScans++;

            }

            // Close file
            inputFile.close();

        } catch (Exception e) {
            throw new MSDKException(e);
        }

        logger.info("Finished parsing " + sourceFile + ", parsed " + parsedScans
                + " scans");

        return newRawFile;

    }

    private void readVariables() throws MSDKException, IOException {

        /*
         * DEBUG: dump all variables for (Variable v : inputFile.getVariables())
         * { System.out.println("variable " + v.getShortName()); }
         */

        // Find mass_values and intensity_values variables
        massValueVariable = inputFile.findVariable("mass_values");
        if (massValueVariable == null) {
            logger.error("Could not find variable mass_values");
            throw (new MSDKException("Could not find variable mass_values"));
        }
        assert(massValueVariable.getRank() == 1);

        Attribute massScaleFacAttr = massValueVariable
                .findAttribute("scale_factor");
        if (massScaleFacAttr != null) {
            massValueScaleFactor = massScaleFacAttr.getNumericValue()
                    .doubleValue();
        }

        intensityValueVariable = inputFile.findVariable("intensity_values");
        if (intensityValueVariable == null) {
            logger.error("Could not find variable intensity_values");
            throw (new MSDKException(
                    "Could not find variable intensity_values"));
        }
        assert(intensityValueVariable.getRank() == 1);

        Attribute intScaleFacAttr = intensityValueVariable
                .findAttribute("scale_factor");
        if (intScaleFacAttr != null) {
            intensityValueScaleFactor = intScaleFacAttr.getNumericValue()
                    .doubleValue();
        }

        // Read number of scans
        Variable scanIndexVariable = inputFile.findVariable("scan_index");
        if (scanIndexVariable == null) {
            throw (new MSDKException("Could not find variable scan_index"));
        }
        totalScans = scanIndexVariable.getShape()[0];
        logger.debug("Found " + totalScans + " scans");

        // Read scan start position. An extra element is required, because
        // element totalScans+1 is used to
        // find the stop position for last scan
        scanStartPositions = new int[totalScans + 1];

        Array scanIndexArray = scanIndexVariable.read();
        IndexIterator scanIndexIterator = scanIndexArray.getIndexIterator();
        int ind = 0;
        while (scanIndexIterator.hasNext()) {
            scanStartPositions[ind] = ((Integer) scanIndexIterator.next());
            ind++;
        }

        // Calc stop position for the last scan
        // This defines the end index of the last scan
        scanStartPositions[totalScans] = (int) massValueVariable.getSize();

        // Start scan RT
        scanRetentionTimes = new float[totalScans];
        Variable scanTimeVariable = inputFile
                .findVariable("scan_acquisition_time");
        if (scanTimeVariable == null) {
            throw (new IOException(
                    "Could not find variable scan_acquisition_time from file "
                            + sourceFile));
        }
        Array scanTimeArray = null;
        scanTimeArray = scanTimeVariable.read();
        IndexIterator scanTimeIterator = scanTimeArray.getIndexIterator();
        ind = 0;
        while (scanTimeIterator.hasNext()) {
            if (scanTimeVariable.getDataType()
                    .getPrimitiveClassType() == float.class) {
                scanRetentionTimes[ind] = (Float) (scanTimeIterator.next());
            }
            if (scanTimeVariable.getDataType()
                    .getPrimitiveClassType() == double.class) {
                scanRetentionTimes[ind] = ((Double) scanTimeIterator.next())
                        .floatValue();
            }
            ind++;
        }
        // End scan RT

        /*
         * Fix problems caused by new QStar data converter:
         * 
         * 1) assume scan is missing when scan_index[i]<0 for these scans
         * 
         * 2) fix variables:
         * 
         * - scan_acquisition_time: interpolate/extrapolate using times of
         * present scans
         * 
         * - scan_index: fill with following good value
         *
         * TODO: need junit test for this
         */

        // Calculate number of good scans
        int numberOfGoodScans = 0;
        for (int i = 0; i < totalScans; i++) {
            if (scanStartPositions[i] >= 0) {
                numberOfGoodScans++;
            }
        }

        // Is there need to fix something?
        if (numberOfGoodScans < totalScans) {

            // Fix scan_acquisition_time
            // - calculate average delta time between present scans
            double sumDelta = 0;
            int n = 0;
            for (int i = 0; i < totalScans; i++) {
                // Is this a present scan?
                if (scanStartPositions[i] >= 0) {
                    // Yes, find next present scan
                    for (int j = i + 1; j < totalScans; j++) {
                        if (scanStartPositions[j] >= 0) {
                            sumDelta += (scanRetentionTimes[j]
                                    - scanRetentionTimes[i])
                                    / ((double) (j - i));
                            n++;
                            break;
                        }
                    }
                }
            }
            double avgDelta = sumDelta / (double) n;
            // - fill missing scan times using nearest good scan and avgDelta
            for (int i = 0; i < totalScans; i++) {
                // Is this a missing scan?
                if (scanStartPositions[i] < 0) {
                    // Yes, find nearest present scan
                    int nearestI = Integer.MAX_VALUE;
                    for (int j = 1; 1 < 2; j++) {
                        if ((i + j) < totalScans) {
                            if (scanStartPositions[i + j] >= 0) {
                                nearestI = i + j;
                                break;
                            }
                        }
                        if ((i - j) >= 0) {
                            if (scanStartPositions[i - j] >= 0) {
                                nearestI = i + j;
                                break;
                            }
                        }

                        // Out of bounds?
                        if (((i + j) >= totalScans) && ((i - j) < 0)) {
                            break;
                        }
                    }

                    if (nearestI != Integer.MAX_VALUE) {

                        scanRetentionTimes[i] = (float) (scanRetentionTimes[nearestI]
                                + (i - nearestI) * avgDelta);

                    } else {
                        if (i > 0) {
                            scanRetentionTimes[i] = scanRetentionTimes[i - 1];
                        } else {
                            scanRetentionTimes[i] = 0;
                        }
                        logger.error(
                                "ERROR: Could not fix incorrect QStar scan times.");
                    }
                }
            }

            // Fix scanStartPositions by filling gaps with next good value
            for (int i = 0; i < totalScans; i++) {
                if (scanStartPositions[i] < 0) {
                    for (int j = i + 1; j < (totalScans + 1); j++) {
                        if (scanStartPositions[j] >= 0) {
                            scanStartPositions[i] = scanStartPositions[j];
                            break;
                        }
                    }
                }
            }
        }

    }

    /**
     * Reads one scan from the file. Requires that general information has
     * already been read.
     * 
     * @throws MSDKException
     * @throws InvalidRangeException
     */
    @SuppressWarnings("null")
    private @Nonnull MsScan readNextScan(int scanIndex)
            throws IOException, MSDKException, InvalidRangeException {

        // Set a simple MS function, always MS level 1 for netCDF data
        final MsFunction msFunction = MSDKObjectBuilder.getMsFunction(1);

        // Scan number
        final Integer scanNumber = scanIndex + 1;

        MsScan scan = MSDKObjectBuilder.getMsScan(dataStore, scanNumber,
                msFunction);

        // Extract and store the data points
        extractDataPoints(scanIndex, dataPoints);
        scan.setDataPoints(dataPoints);

        // Auto-detect whether this scan is centroided
        SpectrumTypeDetectionMethod detector = new SpectrumTypeDetectionMethod(
                scan);
        MsSpectrumType spectrumType = detector.execute();
        scan.setSpectrumType(spectrumType);

        // TODO set correct separation type from global netCDF file attributes
        ChromatographyInfo chromData = MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.UNKNOWN,
                        scanRetentionTimes[scanIndex]);

        scan.setChromatographyInfo(chromData);

        return scan;

    }

    /**
     * 
     * @param scanIndex
     * @param dataPoints
     * @throws InvalidRangeException
     * @throws IOException
     */
    private void extractDataPoints(int scanIndex,
            MsSpectrumDataPointList dataPoints)
                    throws IOException, InvalidRangeException {

        // Find the Index of mass and intensity values
        final int scanStartPosition[] = { scanStartPositions[scanIndex] };
        final int scanLength[] = { scanStartPositions[scanIndex + 1]
                - scanStartPositions[scanIndex] };
        final Array massValueArray = massValueVariable.read(scanStartPosition,
                scanLength);
        final Array intensityValueArray = intensityValueVariable
                .read(scanStartPosition, scanLength);
        final Index massValuesIndex = massValueArray.getIndex();
        final Index intensityValuesIndex = intensityValueArray.getIndex();

        // Get number of data points
        final int arrayLength = massValueArray.getShape()[0];

        // Load the data points
        dataPoints.clear();
        dataPoints.allocate(arrayLength);
        double mzValues[] = dataPoints.getMzBuffer();
        float intValues[] = dataPoints.getIntensityBuffer();

        for (int i = 0; i < arrayLength; i++) {
            final Index massIndex0 = massValuesIndex.set0(i);
            final Index intensityIndex0 = intensityValuesIndex.set0(i);
            mzValues[i] = massValueArray.getDouble(massIndex0)
                    * massValueScaleFactor;
            intValues[i] = (float) (intensityValueArray
                    .getDouble(intensityIndex0) * intensityValueScaleFactor);
        }

        // Update the size of data point list
        dataPoints.setSize(arrayLength);

    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public RawDataFile getResult() {
        return newRawFile;
    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) parsedScans / totalScans;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
