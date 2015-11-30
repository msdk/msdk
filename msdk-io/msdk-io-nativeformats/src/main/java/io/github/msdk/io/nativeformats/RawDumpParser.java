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

package io.github.msdk.io.nativeformats;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionMethod;

class RawDumpParser {

    private boolean canceled = false;

    private int parsedScans, totalScans = 0;

    private int scanNumber = 0, msLevel = 0, numOfDataPoints;
    private String scanId;
    private PolarityType polarity;
    private Range<Double> scanningMzRange;
    private float retentionTime;
    private Double precursorMz;
    private Integer precursorCharge;

    private final RawDataFile newRawFile;
    private final DataPointStore dataStore;
    private byte byteBuffer[] = new byte[100000];

    private final MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
            .getMsSpectrumDataPointList();

    RawDumpParser(RawDataFile newRawFile, DataPointStore dataStore) {
        this.newRawFile = newRawFile;
        this.dataStore = dataStore;
    }

    /**
     * This method reads the dump of the RAW data file produced by RAWdump.exe
     * utility (see RAWdump.cpp source for details).
     * 
     * @throws IOException
     * @throws NumberFormatException
     */
    void readRAWDump(InputStream dumpStream)
            throws MSDKException, NumberFormatException, IOException {

        String line;

        while ((line = TextUtils.readLineFromStream(dumpStream)) != null) {

            if (canceled)
                return;

            parseLine(line, dumpStream);

        }

        if (parsedScans == 0) {
            throw (new MSDKException("No scans found"));
        }

        if (parsedScans != totalScans) {
            throw (new MSDKException(
                    "RAW dump process crashed before all scans were extracted ("
                            + parsedScans + " out of " + totalScans + ")"));
        }

    }

    @SuppressWarnings("null")
    private void parseLine(String line, InputStream dumpStream)
            throws MSDKException, IOException {

        if (line.startsWith("ERROR: ")) {
            throw (new MSDKException(line));
        }

        if (line.startsWith("NUMBER OF SCANS: ")) {
            totalScans = Integer
                    .parseInt(line.substring("NUMBER OF SCANS: ".length()));
        }

        if (line.startsWith("SCAN NUMBER: ")) {
            scanNumber = Integer
                    .parseInt(line.substring("SCAN NUMBER: ".length()));
        }

        if (line.startsWith("SCAN ID: ")) {
            scanId = line.substring("SCAN ID: ".length());
        }

        if (line.startsWith("MS LEVEL: ")) {
            msLevel = Integer.parseInt(line.substring("MS LEVEL: ".length()));
        }

        if (line.startsWith("POLARITY: ")) {
            if (line.contains("-"))
                polarity = PolarityType.NEGATIVE;
            else if (line.contains("+"))
                polarity = PolarityType.POSITIVE;
            else
                polarity = PolarityType.UNKNOWN;
        }

        if (line.startsWith("RETENTION TIME: ")) {
            // Retention time is reported in minutes.
            retentionTime = Float.parseFloat(
                    line.substring("RETENTION TIME: ".length())) * 60.0f;
        }

        if (line.startsWith("PRECURSOR: ")) {
            String tokens[] = line.split(" ");
            double token2 = Double.parseDouble(tokens[1]);
            int token3 = Integer.parseInt(tokens[2]);
            if (token2 > 0) {
                precursorMz = token2;
                precursorCharge = token3;
            }
        }

        if (line.startsWith("MASS VALUES: ")) {
            Pattern p = Pattern.compile("MASS VALUES: (\\d+) x (\\d+) BYTES");
            Matcher m = p.matcher(line);
            if (!m.matches())
                throw new MSDKException("Could not parse line " + line);
            numOfDataPoints = Integer.parseInt(m.group(1));
            dataPoints.allocate(numOfDataPoints);
            dataPoints.setSize(numOfDataPoints);

            final int byteSize = Integer.parseInt(m.group(2));

            final int numOfBytes = numOfDataPoints * byteSize;
            if (byteBuffer.length < numOfBytes)
                byteBuffer = new byte[numOfBytes * 2];
            dumpStream.read(byteBuffer, 0, numOfBytes);

            ByteBuffer mzByteBuffer = ByteBuffer.wrap(byteBuffer, 0, numOfBytes)
                    .order(ByteOrder.LITTLE_ENDIAN);

            double mzValuesBuffer[] = dataPoints.getMzBuffer();

            for (int i = 0; i < numOfDataPoints; i++) {
                if (byteSize == 8)
                    mzValuesBuffer[i] = mzByteBuffer.getDouble();
                else
                    mzValuesBuffer[i] = mzByteBuffer.getFloat();
            }

        }

        if (line.startsWith("INTENSITY VALUES: ")) {
            Pattern p = Pattern
                    .compile("INTENSITY VALUES: (\\d+) x (\\d+) BYTES");
            Matcher m = p.matcher(line);
            if (!m.matches())
                throw new MSDKException("Could not parse line " + line);
            // numOfDataPoints must be same for MASS VALUES and INTENSITY
            // VALUES
            if (numOfDataPoints != Integer.parseInt(m.group(1))) {
                throw new MSDKException("Scan " + scanNumber + " contained "
                        + numOfDataPoints + " mass values, but " + m.group(1)
                        + " intensity values");
            }
            final int byteSize = Integer.parseInt(m.group(2));

            final int numOfBytes = numOfDataPoints * byteSize;
            if (byteBuffer.length < numOfBytes)
                byteBuffer = new byte[numOfBytes * 2];
            dumpStream.read(byteBuffer, 0, numOfBytes);

            ByteBuffer intensityByteBuffer = ByteBuffer
                    .wrap(byteBuffer, 0, numOfBytes)
                    .order(ByteOrder.LITTLE_ENDIAN);

            float intensityValuesBuffer[] = dataPoints.getIntensityBuffer();

            for (int i = 0; i < numOfDataPoints; i++) {
                if (byteSize == 8)
                    intensityValuesBuffer[i] = (float) intensityByteBuffer
                            .getDouble();
                else
                    intensityValuesBuffer[i] = intensityByteBuffer.getFloat();
            }
        }

        if (line.startsWith("END OF SCAN")) {

            // Auto-detect whether this scan is centroided
            SpectrumTypeDetectionMethod detector = new SpectrumTypeDetectionMethod(
                    dataPoints);
            MsSpectrumType spectrumType = detector.execute();

            // Create a new scan
            MsFunction msFunction = MSDKObjectBuilder.getMsFunction(msLevel);

            MsScan newScan = MSDKObjectBuilder.getMsScan(dataStore, scanNumber,
                    msFunction);

            ChromatographyInfo chromInfo = MSDKObjectBuilder
                    .getChromatographyInfo1D(SeparationType.UNKNOWN,
                            retentionTime);
            newScan.setChromatographyInfo(chromInfo);

            newScan.setDataPoints(dataPoints);
            newScan.setSpectrumType(spectrumType);
            newScan.setPolarity(polarity);
            newScan.setScanningRange(scanningMzRange);
            newScan.setScanDefinition(scanId);

            if (precursorMz != null) {
                IsolationInfo isolation = MSDKObjectBuilder.getIsolationInfo(
                        Range.singleton(precursorMz), null, precursorMz,
                        precursorCharge, null);
                newScan.getIsolations().add(isolation);
            }

            // Add the scan to the file
            newRawFile.addScan(newScan);
            parsedScans++;

            // Clean the variables for next scan
            scanNumber = 0;
            scanId = null;
            polarity = null;
            scanningMzRange = null;
            msLevel = 0;
            retentionTime = 0;
            precursorMz = null;
            precursorCharge = null;
            numOfDataPoints = 0;

        }
    }

    Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) parsedScans / totalScans;
    }

    void cancel() {
        this.canceled = true;
    }
}
