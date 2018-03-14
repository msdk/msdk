/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.io.nativeformats;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.SimpleIsolationInfo;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.datamodel.SimpleRawDataFile;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;

class RawDumpParser {

  private final String thermoMsFunctions[] =
      {"sim", "srm", "mrm", "crm", "q1ms", "q3ms", "pr", "cnl"};

  private boolean canceled = false;

  private int parsedScans, totalScans = 0;

  private int scanNumber = 0, msLevel = 0;
  private String scanId;
  private PolarityType polarity;
  private Range<Double> scanningMzRange;
  private float retentionTime;
  private Double precursorMz;
  private Integer precursorCharge;

  private final SimpleRawDataFile newRawFile;
  private byte byteBuffer[] = new byte[100000];

  private double mzValues[] = new double[10000];
  private float intensityValues[] = new float[10000];
  private int numOfDataPoints;

  RawDumpParser(SimpleRawDataFile newRawFile) {
    this.newRawFile = newRawFile;
  }

  /**
   * This method reads the dump of the RAW data file produced by RAWdump.exe utility (see
   * RAWdump.cpp source for details).
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
      throw (new MSDKException("RAW dump process crashed before all scans were extracted ("
          + parsedScans + " out of " + totalScans + ")"));
    }

  }


  private void parseLine(String line, InputStream dumpStream) throws MSDKException, IOException {

    if (line.startsWith("ERROR: ")) {
      throw (new MSDKException(line));
    }

    if (line.startsWith("NUMBER OF SCANS: ")) {
      totalScans = Integer.parseInt(line.substring("NUMBER OF SCANS: ".length()));
    }

    if (line.startsWith("SCAN NUMBER: ")) {
      scanNumber = Integer.parseInt(line.substring("SCAN NUMBER: ".length()));
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

      // For Thermo RAW files, the polarity is sometimes not recognized.
      // In such case, we can parse it from the scan filter line (scanId).
      if ((polarity == PolarityType.UNKNOWN)
          && (newRawFile.getRawDataFileType() == FileType.THERMO_RAW)
          && (!Strings.isNullOrEmpty(scanId))) {
        if (scanId.startsWith("-"))
          polarity = PolarityType.NEGATIVE;
        else if (scanId.startsWith("+"))
          polarity = PolarityType.POSITIVE;
      }

    }

    if (line.startsWith("RETENTION TIME: ")) {
      // Retention time is reported in minutes.
      retentionTime = Float.parseFloat(line.substring("RETENTION TIME: ".length())) * 60.0f;
    }

    if (line.startsWith("PRECURSOR: ")) {
      String tokens[] = line.split(" ");
      double token2 = Double.parseDouble(tokens[1]);
      int token3 = Integer.parseInt(tokens[2]);

      precursorMz = token2;
      precursorCharge = token3;

      // For Thermo RAW files, the MSFileReader library sometimes
      // returns 0.0 for precursor m/z. In such case, we can parse
      // the precursor m/z from the scan filter line (scanId).
      // Examples:
      // + c ESI SRM ms2 165.000 [118.600-119.600]
      // FTMS + p ESI d Full ms2 279.16@hcd25.00 [50.00-305.00]
      if ((precursorMz == 0.0) && (newRawFile.getRawDataFileType() == FileType.THERMO_RAW)
          && (!Strings.isNullOrEmpty(scanId))) {
        Pattern precursorPattern = Pattern.compile(".* ms\\d+ (\\d+\\.\\d+)[@ ]");
        Matcher m = precursorPattern.matcher(scanId);
        if (m.find()) {
          String precursorMzString = m.group(1);
          try {
            precursorMz = Double.parseDouble(precursorMzString);
          } catch (Exception e) {
            e.printStackTrace();
            // ignore
          }
        }
      }

    }

    if (line.startsWith("MASS VALUES: ")) {
      Pattern p = Pattern.compile("MASS VALUES: (\\d+) x (\\d+) BYTES");
      Matcher m = p.matcher(line);
      if (!m.matches())
        throw new MSDKException("Could not parse line " + line);
      numOfDataPoints = Integer.parseInt(m.group(1));

      // Allocate space
      if (mzValues.length < numOfDataPoints)
        mzValues = new double[numOfDataPoints * 2];
      if (intensityValues.length < numOfDataPoints)
        intensityValues = new float[numOfDataPoints * 2];

      final int byteSize = Integer.parseInt(m.group(2));

      final int numOfBytes = numOfDataPoints * byteSize;
      if (byteBuffer.length < numOfBytes)
        byteBuffer = new byte[numOfBytes * 2];
      dumpStream.read(byteBuffer, 0, numOfBytes);

      ByteBuffer mzByteBuffer =
          ByteBuffer.wrap(byteBuffer, 0, numOfBytes).order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < numOfDataPoints; i++) {
        if (byteSize == 8)
          mzValues[i] = mzByteBuffer.getDouble();
        else
          mzValues[i] = mzByteBuffer.getFloat();
      }

    }

    if (line.startsWith("INTENSITY VALUES: ")) {
      Pattern p = Pattern.compile("INTENSITY VALUES: (\\d+) x (\\d+) BYTES");
      Matcher m = p.matcher(line);
      if (!m.matches())
        throw new MSDKException("Could not parse line " + line);
      // numOfDataPoints must be same for MASS VALUES and INTENSITY
      // VALUES
      if (numOfDataPoints != Integer.parseInt(m.group(1))) {
        throw new MSDKException("Scan " + scanNumber + " contained " + numOfDataPoints
            + " mass values, but " + m.group(1) + " intensity values");
      }
      final int byteSize = Integer.parseInt(m.group(2));

      final int numOfBytes = numOfDataPoints * byteSize;
      if (byteBuffer.length < numOfBytes)
        byteBuffer = new byte[numOfBytes * 2];
      dumpStream.read(byteBuffer, 0, numOfBytes);

      ByteBuffer intensityByteBuffer =
          ByteBuffer.wrap(byteBuffer, 0, numOfBytes).order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < numOfDataPoints; i++) {
        if (byteSize == 8)
          intensityValues[i] = (float) intensityByteBuffer.getDouble();
        else
          intensityValues[i] = intensityByteBuffer.getFloat();
      }
    }

    if (line.startsWith("END OF SCAN")) {

      // Auto-detect whether this scan is centroided
      MsSpectrumType spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(mzValues,
          intensityValues, numOfDataPoints);

      // Create a new MS function
      String msFunction = null;
      if ((newRawFile.getRawDataFileType() == FileType.THERMO_RAW)
          && (!Strings.isNullOrEmpty(scanId))) {
        // Parse the MS function from the scan filter line, e.g.
        // + c SRM ms2 469.40@cid23.00 [423.30-425.30]
        // + p ESI Q1MS [181.653-182.582, 507.779-508.708]

        String scanIdLowerCase = scanId.toLowerCase();

        for (String fn : thermoMsFunctions) {
          if (scanIdLowerCase.contains(fn)) {
            msFunction = fn;
            break;
          }
        }

      }

      // Create a new scan
      SimpleMsScan newScan = new SimpleMsScan(scanNumber);
      newScan.setMsFunction(msFunction);
      newScan.setMsLevel(msLevel);
      newScan.setRetentionTime(retentionTime);
      newScan.setDataPoints(mzValues, intensityValues, numOfDataPoints);
      newScan.setSpectrumType(spectrumType);
      newScan.setPolarity(polarity);
      newScan.setScanningRange(scanningMzRange);
      newScan.setScanDefinition(scanId);

      if (precursorMz != null) {
        // TODO Also parse precursor scan number
        IsolationInfo isolation = new SimpleIsolationInfo(Range.singleton(precursorMz), null,
            precursorMz, precursorCharge, null, null);
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
