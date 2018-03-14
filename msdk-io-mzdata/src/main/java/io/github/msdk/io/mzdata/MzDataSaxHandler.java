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

package io.github.msdk.io.mzdata;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.SimpleIsolationInfo;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.datamodel.SimpleRawDataFile;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;

class MzDataSaxHandler extends DefaultHandler {

  private SimpleRawDataFile newRawFile;

  private boolean canceled = false;
  private long totalScans = 0, parsedScans;

  private final StringBuilder charBuffer;

  private boolean precursorFlag = false;
  private boolean spectrumInstrumentFlag = false;
  private boolean mzArrayBinaryFlag = false;
  private boolean intenArrayBinaryFlag = false;
  private String precision, endian;
  private int scanNumber;
  private int msLevel;
  private PolarityType polarity = PolarityType.UNKNOWN;
  private Float retentionTime;
  private Double precursorMz;
  private Integer precursorCharge;

  private double mzBuffer[] = new double[10000];
  private float intensityBuffer[] = new float[10000];
  private int peaksCount = 0;

  /**
   * <p>
   * Constructor for MzDataSaxHandler.
   * </p>
   *
   * @param newRawFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   */
  public MzDataSaxHandler(SimpleRawDataFile newRawFile) {
    this.newRawFile = newRawFile;
    charBuffer = new StringBuilder();
  }

  /** {@inheritDoc} */
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs)
      throws SAXException {

    if (canceled)
      throw new SAXException("Parsing Cancelled");

    // <spectrumList>
    if (qName.equals("spectrumList")) {
      String s = attrs.getValue("count");
      if (s != null)
        totalScans = Integer.parseInt(s);
    }

    // <spectrum>
    if (qName.equalsIgnoreCase("spectrum")) {
      msLevel = 1;
      retentionTime = null;
      polarity = PolarityType.UNKNOWN;
      precursorMz = null;
      precursorCharge = null;
      scanNumber = Integer.parseInt(attrs.getValue("id"));
    }

    // <spectrumInstrument> 1.05 version, <acqInstrument> 1.04 version
    if ((qName.equalsIgnoreCase("spectrumInstrument"))
        || (qName.equalsIgnoreCase("acqInstrument"))) {
      msLevel = Integer.parseInt(attrs.getValue("msLevel"));
      spectrumInstrumentFlag = true;
    }

    // <cvParam>
    /*
     * The terms time.min, time.sec & mz belongs to mzData 1.04 standard.
     */
    if (qName.equalsIgnoreCase("cvParam")) {
      if (spectrumInstrumentFlag) {
        if ((attrs.getValue("accession").equals(MzDataCV.cvPolarity))
            || (attrs.getValue("name").equals("Polarity"))) {
          if (attrs.getValue("value").toLowerCase().equals("positive"))
            polarity = PolarityType.POSITIVE;
          else if (attrs.getValue("value").toLowerCase().equals("negative"))
            polarity = PolarityType.NEGATIVE;
          else
            polarity = PolarityType.UNKNOWN;
        }
        if ((attrs.getValue("accession").equals(MzDataCV.cvTimeMin))
            || (attrs.getValue("name").equals("time.min"))) {
          retentionTime = Float.parseFloat(attrs.getValue("value")) * 60f;
        }

        if ((attrs.getValue("accession").equals(MzDataCV.cvTimeSec))
            || (attrs.getValue("name").equals("time.sec"))) {
          retentionTime = Float.parseFloat(attrs.getValue("value"));
        }
      }
      if (precursorFlag) {
        if ((attrs.getValue("accession").equals(MzDataCV.cvPrecursorMz))
            || (attrs.getValue("name").equals("mz"))) {
          precursorMz = Double.parseDouble(attrs.getValue("value"));
        }
        if (attrs.getValue("accession").equals(MzDataCV.cvPrecursorCharge)) {
          precursorCharge = Integer.parseInt(attrs.getValue("value"));
        }
      }
    }

    // <mzArrayBinary>
    if (qName.equalsIgnoreCase("mzArrayBinary")) {
      // clean the current char buffer for the new element
      mzArrayBinaryFlag = true;
    }

    // <intenArrayBinary>
    if (qName.equalsIgnoreCase("intenArrayBinary")) {
      // clean the current char buffer for the new element
      intenArrayBinaryFlag = true;
    }

    // <data>
    if (qName.equalsIgnoreCase("data")) {
      // clean the current char buffer for the new element
      charBuffer.setLength(0);
      if (mzArrayBinaryFlag) {
        endian = attrs.getValue("endian");
        precision = attrs.getValue("precision");
        String len = attrs.getValue("length");
        if (len != null)
          peaksCount = Integer.parseInt(len);
      }
      if (intenArrayBinaryFlag) {
        endian = attrs.getValue("endian");
        precision = attrs.getValue("precision");
        String len = attrs.getValue("length");
        if (len != null)
          peaksCount = Integer.parseInt(len);
      }
    }

    // <precursor>
    if (qName.equalsIgnoreCase("precursor")) {
      precursorFlag = true;
    }
  }

  /**
   * {@inheritDoc}
   *
   * endElement()
   */
  public void endElement(String namespaceURI, String sName, String qName) throws SAXException {

    if (canceled)
      throw new SAXException("Parsing Cancelled");

    // <spectrumInstrument>
    if (qName.equalsIgnoreCase("spectrumInstrument")) {
      spectrumInstrumentFlag = false;
    }

    // <precursor>
    if (qName.equalsIgnoreCase("precursor")) {
      precursorFlag = false;
    }

    // <spectrum>
    if (qName.equalsIgnoreCase("spectrum")) {

      spectrumInstrumentFlag = false;

      // Auto-detect whether this scan is centroided
      MsSpectrumType spectrumType =
          SpectrumTypeDetectionAlgorithm.detectSpectrumType(mzBuffer, intensityBuffer, peaksCount);

      SimpleMsScan newScan = new SimpleMsScan(scanNumber);

      newScan.setMsLevel(msLevel);
      newScan.setDataPoints(mzBuffer, intensityBuffer, peaksCount);
      newScan.setSpectrumType(spectrumType);
      newScan.setPolarity(polarity);

      if (retentionTime != null) {
        newScan.setRetentionTime(retentionTime);
      }

      if (precursorMz != null) {
        // TODO Also parse precursor scan number
        IsolationInfo isolation = new SimpleIsolationInfo(Range.singleton(precursorMz),
            null, precursorMz, precursorCharge, null, null);
        newScan.getIsolations().add(isolation);
      }

      // Add the scan to the file
      newRawFile.addScan(newScan);
      parsedScans++;

    }

    // <mzArrayBinary>
    if (qName.equalsIgnoreCase("mzArrayBinary")) {

      mzArrayBinaryFlag = false;

      // Allocate space for the whole array
      if (mzBuffer.length < peaksCount)
        mzBuffer = new double[peaksCount * 2];

      byte[] peakBytes = Base64.getMimeDecoder().decode(charBuffer.toString());

      ByteBuffer currentMzBytes = ByteBuffer.wrap(peakBytes);

      if (endian.equals("big")) {
        currentMzBytes = currentMzBytes.order(ByteOrder.BIG_ENDIAN);
      } else {
        currentMzBytes = currentMzBytes.order(ByteOrder.LITTLE_ENDIAN);
      }

      for (int i = 0; i < peaksCount; i++) {
        if (precision == null || precision.equals("32"))
          mzBuffer[i] = (double) currentMzBytes.getFloat();
        else
          mzBuffer[i] = currentMzBytes.getDouble();
      }

    }

    // <intenArrayBinary>
    if (qName.equalsIgnoreCase("intenArrayBinary")) {

      intenArrayBinaryFlag = false;

      // Allocate space for the whole array
      if (intensityBuffer.length < peaksCount)
        intensityBuffer = new float[peaksCount * 2];

      byte[] peakBytes = Base64.getMimeDecoder().decode(charBuffer.toString());

      ByteBuffer currentIntensityBytes = ByteBuffer.wrap(peakBytes);

      if (endian.equals("big")) {
        currentIntensityBytes = currentIntensityBytes.order(ByteOrder.BIG_ENDIAN);
      } else {
        currentIntensityBytes = currentIntensityBytes.order(ByteOrder.LITTLE_ENDIAN);
      }

      for (int i = 0; i < peaksCount; i++) {
        if (precision == null || precision.equals("32"))
          intensityBuffer[i] = currentIntensityBytes.getFloat();
        else
          intensityBuffer[i] = (float) currentIntensityBytes.getDouble();
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * characters()
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  public void characters(char buf[], int offset, int len) throws SAXException {
    if (canceled)
      throw new SAXException("Parsing Cancelled");
    charBuffer.append(buf, offset, len);
  }

  /**
   * <p>
   * endDocument.
   * </p>
   *
   * @throws org.xml.sax.SAXException if any.
   */
  public void endDocument() throws SAXException {
    if (canceled)
      throw new SAXException("Parsing Cancelled");
  }

  /**
   * <p>
   * getFinishedPercentage.
   * </p>
   *
   * @return a {@link java.lang.Float} object.
   */
  public Float getFinishedPercentage() {
    if (totalScans == 0)
      return null;
    else
      return (float) parsedScans / totalScans;
  }

  /**
   * <p>
   * cancel.
   * </p>
   */
  public void cancel() {
    this.canceled = true;
  }

}
