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

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionMethod;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFileImportMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private NetcdfFile inputFile;

    private int parsedScans;
    private int totalScans = 0, numberOfGoodScans, scanNum = 0;

    private Hashtable<Integer, Integer[]> scansIndex;
    private Hashtable<Integer, Double> scansRetentionTimes;

    private final @Nonnull File sourceFile;
    private RawDataFile newRawFile;
    private boolean canceled = false;

    private Variable massValueVariable, intensityValueVariable;

    // Some software produces netcdf files with a scale factor such as 0.05
    private double massValueScaleFactor = 1;
    private double intensityValueScaleFactor = 1;

    public NetCDFFileImportMethod(@Nonnull File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public RawDataFile execute() throws MSDKException {

        logger.info("Started parsing file " + sourceFile);

        newRawFile = MSDKObjectBuilder.getRawDataFile();
        newRawFile.setName(sourceFile.getName());

        try {

            // Open NetCDF-file
            inputFile = NetcdfFile.open(sourceFile.getPath());

            // Read NetCDF variables
            readVariables();

            // Parse scans
            MsScan buildingScan;
            while ((buildingScan = readNextScan()) != null) {

                // Check if cancel is requested
                if (canceled) {
                    return null;
                }
                // buildingFile.addScan(scan);
                newRawFile.addScan(buildingScan);
                parsedScans++;

            }

            // Close file
            inputFile.close();

        } catch (Throwable e) {
            throw new MSDKException(e);
        }

        logger.info("Finished parsing " + sourceFile + ", parsed "
                + parsedScans + " scans");

        return newRawFile;

    }

    private void readVariables() throws IOException {

        /*
         * DEBUG: dump all variables for (Variable v : inputFile.getVariables())
         * { System.out.println("variable " + v.getShortName()); }
         */

        // Find mass_values and intensity_values variables
        massValueVariable = inputFile.findVariable("mass_values");
        if (massValueVariable == null) {
            logger.error("Could not find variable mass_values");
            throw (new IOException("Could not find variable mass_values"));
        }
        assert (massValueVariable.getRank() == 1);

        Attribute massScaleFacAttr = massValueVariable
                .findAttribute("scale_factor");
        if (massScaleFacAttr != null) {
            massValueScaleFactor = massScaleFacAttr.getNumericValue()
                    .doubleValue();
        }

        intensityValueVariable = inputFile.findVariable("intensity_values");
        if (intensityValueVariable == null) {
            logger.error("Could not find variable intensity_values");
            throw (new IOException("Could not find variable intensity_values"));
        }
        assert (intensityValueVariable.getRank() == 1);

        Attribute intScaleFacAttr = intensityValueVariable
                .findAttribute("scale_factor");
        if (intScaleFacAttr != null) {
            intensityValueScaleFactor = intScaleFacAttr.getNumericValue()
                    .doubleValue();
        }

        // Read number of scans
        Variable scanIndexVariable = inputFile.findVariable("scan_index");
        if (scanIndexVariable == null) {
            throw (new IOException(
                    "Could not find variable scan_index from file "
                            + sourceFile));
        }
        totalScans = scanIndexVariable.getShape()[0];

        // Read scan start positions
        // Extra element is required, because element totalScans+1 is used to
        // find the stop position for last scan
        int[] scanStartPositions = new int[totalScans + 1];

        Array scanIndexArray = null;
        scanIndexArray = scanIndexVariable.read();

        IndexIterator scanIndexIterator = scanIndexArray.getIndexIterator();
        int ind = 0;
        while (scanIndexIterator.hasNext()) {
            scanStartPositions[ind] = ((Integer) scanIndexIterator.next())
                    .intValue();
            ind++;
        }
        scanIndexIterator = null;
        scanIndexArray = null;
        scanIndexVariable = null;

        // Calc stop position for the last scan
        // This defines the end index of the last scan
        scanStartPositions[totalScans] = (int) massValueVariable.getSize();

        // Start scan RT
        double[] retentionTimes = new double[totalScans];
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
            if (scanTimeVariable.getDataType().getPrimitiveClassType() == float.class) {
                retentionTimes[ind] = ((Double) scanTimeIterator.next()) / 60d;
            }
            if (scanTimeVariable.getDataType().getPrimitiveClassType() == double.class) {
                retentionTimes[ind] = ((Double) scanTimeIterator.next()) / 60d;
            }
            ind++;
        }
        // End scan RT

        // Cleanup
        scanTimeIterator = null;
        scanTimeArray = null;
        scanTimeVariable = null;

        // Fix problems caused by new QStar data converter
        // assume scan is missing when scan_index[i]<0
        // for these scans, fix variables:
        // - scan_acquisition_time: interpolate/extrapolate using times of
        // present scans
        // - scan_index: fill with following good value

        // Calculate number of good scans
        numberOfGoodScans = 0;
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
                            sumDelta += (retentionTimes[j] - retentionTimes[i])
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

                        retentionTimes[i] = retentionTimes[nearestI]
                                + (i - nearestI) * avgDelta;

                    } else {
                        if (i > 0) {
                            retentionTimes[i] = retentionTimes[i - 1];
                        } else {
                            retentionTimes[i] = 0;
                        }
                        logger.error("ERROR: Could not fix incorrect QStar scan times.");
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

        // Collect information about retention times, start positions and
        // lengths for scans
        scansRetentionTimes = new Hashtable<Integer, Double>();
        scansIndex = new Hashtable<Integer, Integer[]>();
        for (int i = 0; i < totalScans; i++) {

            Integer scanNum = new Integer(i);

            Integer[] startAndLength = new Integer[2];
            startAndLength[0] = scanStartPositions[i];
            startAndLength[1] = scanStartPositions[i + 1]
                    - scanStartPositions[i];

            scansRetentionTimes.put(scanNum, new Double(retentionTimes[i]));
            scansIndex.put(scanNum, startAndLength);

        }

        scanStartPositions = null;
        retentionTimes = null;

    }

    /**
     * Reads one scan from the file. Requires that general information has
     * already been read.
     * 
     * @throws MSDKException
     */
    private MsScan readNextScan() throws IOException, MSDKException {

        MsScan scan = MSDKObjectBuilder.getMsScan(null, null, null);

        // Set scan number
        scanNum++;
        scan.setScanNumber(scanNum);

        // NetCDF only supports MS level 1
        // scan.setMSLevel(1);

        // Get scan starting position and length
        int[] scanStartPosition = new int[1];
        int[] scanLength = new int[1];
        Integer[] startAndLength = scansIndex.get(scanNum);

        // End of file
        if (startAndLength == null) {
            return null;
        }
        scanStartPosition[0] = startAndLength[0];
        scanLength[0] = startAndLength[1];

        // Get retention time of the scan
        Double retentionTime = scansRetentionTimes.get(scanNum);
        if (retentionTime == null) {
            logger.error("Could not find retention time for scan " + scanNum);
            throw (new IOException("Could not find retention time for scan "
                    + scanNum));
        }

        // Read mass and intensity values
        Array massValueArray;
        Array intensityValueArray;
        try {
            massValueArray = massValueVariable.read(scanStartPosition,
                    scanLength);
            intensityValueArray = intensityValueVariable.read(
                    scanStartPosition, scanLength);
        } catch (Exception e) {
            logger.error(
                    "Could not read from variables mass_values and/or intensity_values.",
                    e);
            throw (new IOException(
                    "Could not read from variables mass_values and/or intensity_values."));
        }

        Index massValuesIndex = massValueArray.getIndex();
        Index intensityValuesIndex = intensityValueArray.getIndex();

        int arrayLength = massValueArray.getShape()[0];

        DataPoint dataPoints[] = new DataPoint[arrayLength];

        for (int j = 0; j < arrayLength; j++) {
            Index massIndex0 = massValuesIndex.set0(j);
            Index intensityIndex0 = intensityValuesIndex.set0(j);

            double mz = massValueArray.getDouble(massIndex0)
                    * massValueScaleFactor;
            double intensity = intensityValueArray.getDouble(intensityIndex0)
                    * intensityValueScaleFactor;
            // dataPoints[j] = MSDKObjectBuilder.getDataPoint(mz, intensity);

        }

        // Auto-detect whether this scan is centroided
        SpectrumTypeDetectionMethod detector = new SpectrumTypeDetectionMethod(
                scan);
        detector.execute();
        MassSpectrumType spectrumType = detector.getResult();
        scan.setSpectrumType(spectrumType);

        // TODO set correct separation type from global netCDF file attributes
        ChromatographyInfo chromData = MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.GC,
                        retentionTime.floatValue());
        scan.setChromatographyInfo(chromData);
        return scan;

    }

    @Override
    @Nullable
    public RawDataFile getResult() {
        return newRawFile;
    }

    @Override
    public Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) parsedScans / totalScans;
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }

}
