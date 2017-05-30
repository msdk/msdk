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

package io.github.msdk.io.mzxml;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Strings;
import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.impl.SimpleMsScan;
import io.github.msdk.datamodel.impl.SimpleRawDataFile;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.spectra.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;

/**
 * This class reads mzXML file format.
 */
public class MzXMLFileImportMethod implements MSDKMethod<RawDataFile> {

  private Logger logger = Logger.getLogger(this.getClass().getName());

  private final @Nonnull File sourceFile;
  private final @Nonnull FileType fileType = FileType.MZXML;

  private SimpleRawDataFile newRawDataFile;
  private final @Nonnull DataPointStore dataStore;

  private int totalScans = 0, parsedScans;

  private int peaksCount = 0;
  private boolean compressFlag = false;

  private final MzXMLHandler handler = new MzXMLHandler();

  private String precision;
  private Integer precursorCharge;

  // Buffers
  private final StringBuilder charBuffer = new StringBuilder(1 << 18);
  private double mzValues[] = new double[1000];
  private float intensityValues[] = new float[1000];

  // Retention time parser
  private DatatypeFactory dataTypeFactory;

  private boolean canceled = false;

  /*
   * This variable hold the present scan or fragment, it is send to the stack when another
   * scan/fragment appears as a parser.startElement
   */
  private SimpleMsScan buildingScan;

  /**
   * <p>
   * Constructor for MzXMLFileImportMethod.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   * @param dataStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   */
  public MzXMLFileImportMethod(@Nonnull File sourceFile, @Nonnull DataPointStore dataStore) {
    this.sourceFile = sourceFile;
    this.dataStore = dataStore;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile execute() throws MSDKException {

    try {

      logger.info("Started parsing file " + sourceFile);

      // Create the XMLBasedRawDataFile object
      newRawDataFile =
          new SimpleRawDataFile(sourceFile.getName(), sourceFile, fileType, dataStore);

      // Use the default (non-validating) parser
      SAXParserFactory factory = SAXParserFactory.newInstance();
      dataTypeFactory = DatatypeFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(sourceFile, handler);

      logger.info("Finished parsing " + sourceFile + ", parsed " + parsedScans + " scans");

      return newRawDataFile;

    } catch (Throwable e) {

      // We may already have set the status to CANCELED. In that case the
      // caught exception simply indicates end of SAX parsing.
      if (canceled)
        return null;
      else
        throw new MSDKException(e);

    }

  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    return totalScans == 0 ? 0 : (float) parsedScans / totalScans;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile getResult() {
    return newRawDataFile;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

  private class MzXMLHandler extends DefaultHandler {

    public void startElement(String namespaceURI, String lName, // local
        // name
        String qName, // qualified name
        Attributes attrs) throws SAXException {

      if (canceled)
        throw new SAXException("Parsing Cancelled");

      // <msRun>
      if (qName.equals("msRun")) {
        String s = attrs.getValue("scanCount");
        if (s != null)
          totalScans = Integer.parseInt(s);
      }

      // <scan>
      if (qName.equalsIgnoreCase("scan")) {

        /*
         * Only num, msLevel & peaksCount values are required according with mzXML standard, the
         * others are optional
         */
        int scanNumber = Integer.parseInt(attrs.getValue("num"));
        int msLevel = Integer.parseInt(attrs.getValue("msLevel"));
        peaksCount = Integer.parseInt(attrs.getValue("peaksCount"));

        // MS function
        String msFuncName = attrs.getValue("scanType");
        if (Strings.isNullOrEmpty(msFuncName))
          msFuncName = MsFunction.DEFAULT_MS_FUNCTION_NAME;
        MsFunction msFunc = MSDKObjectBuilder.getMsFunction(msFuncName, msLevel);
        buildingScan = new SimpleMsScan(dataStore, scanNumber, msFunc);
        buildingScan.setRawDataFile(newRawDataFile);

        // Scan type & definition
        buildingScan.setMsScanType(MsScanType.UNKNOWN);
        String filterLine = attrs.getValue("filterLine");
        buildingScan.setScanDefinition(filterLine);

        // Polarity
        PolarityType polarity;
        String polarityAttr = attrs.getValue("polarity");
        switch (polarityAttr) {
          case "+":
            polarity = PolarityType.POSITIVE;
            break;
          case "-":
            polarity = PolarityType.NEGATIVE;
            break;
          default:
            polarity = PolarityType.UNKNOWN;
            break;
        }
        buildingScan.setPolarity(polarity);

        // Parse retention time
        String retentionTimeStr = attrs.getValue("retentionTime");
        if (retentionTimeStr != null) {
          Date currentDate = new Date();
          Duration dur = dataTypeFactory.newDuration(retentionTimeStr);
          final float rt = (float) (dur.getTimeInMillis(currentDate) / 1000.0);
          buildingScan.setRetentionTime(rt);
        }

      }

      // <peaks>
      if (qName.equalsIgnoreCase("peaks")) {
        // clean the current char buffer for the new element
        charBuffer.setLength(0);
        compressFlag = false;
        String compressionType = attrs.getValue("compressionType");
        if ((compressionType == null) || (compressionType.equals("none")))
          compressFlag = false;
        else
          compressFlag = true;
        precision = attrs.getValue("precision");

      }

      // <precursorMz>
      if (qName.equalsIgnoreCase("precursorMz")) {
        // clean the current char buffer for the new element
        charBuffer.setLength(0);
        String precursorChargeAttr = attrs.getValue("precursorCharge");
        if (precursorChargeAttr != null)
          precursorCharge = Integer.parseInt(precursorChargeAttr);
      }

    }

    /**
     * endElement()
     */
    public void endElement(String namespaceURI, String sName, // simple name
        String qName // qualified name
    ) throws SAXException {

      // </scan>
      if (qName.equalsIgnoreCase("scan")) {
        newRawDataFile.addScan(buildingScan);
        parsedScans++;
        return;
      }

      // <precursorMz>
      if (qName.equalsIgnoreCase("precursorMz")) {
        final String textContent = charBuffer.toString();
        double precursorMz = 0d;
        if (!textContent.isEmpty())
          precursorMz = Double.parseDouble(textContent);
        IsolationInfo newIsolation = MSDKObjectBuilder.getIsolationInfo(
            Range.singleton(precursorMz), null, precursorMz, precursorCharge, null);
        buildingScan.getIsolations().add(newIsolation);

        return;
      }

      // <peaks>
      if (qName.equalsIgnoreCase("peaks")) {

        // Base64 decoder
        byte[] peakBytes = DatatypeConverter.parseBase64Binary(charBuffer.toString());

        if (compressFlag) {
          try {
            peakBytes = ZlibCompressionUtil.decompress(peakBytes);
          } catch (DataFormatException e) {
            throw new SAXException(e);
          }
        }

        // make a data input stream
        DataInputStream peakStream = new DataInputStream(new ByteArrayInputStream(peakBytes));

        if (peaksCount > mzValues.length) {
          mzValues = new double[peaksCount];
          intensityValues = new float[peaksCount];
        }

        try {
          for (int i = 0; i < peaksCount; i++) {

            // Always respect this order pairOrder="m/z-int"
            if ("64".equals(precision)) {
              mzValues[i] = peakStream.readDouble();
              intensityValues[i] = (float) peakStream.readDouble();
            } else {
              mzValues[i] = (double) peakStream.readFloat();
              intensityValues[i] = peakStream.readFloat();
            }

          }
        } catch (IOException eof) {
          throw new SAXException(eof);
        }
        // Set the final data points to the scan
        buildingScan.setDataPoints(mzValues, intensityValues, peaksCount);

        // Auto-detect whether this scan is centroided
        MsSpectrumType spectrumType = SpectrumTypeDetectionAlgorithm.detectSpectrumType(mzValues,
            intensityValues, peaksCount);
        buildingScan.setSpectrumType(spectrumType);

        return;
      }
    }

    /**
     * characters()
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char buf[], int offset, int len) throws SAXException {
      charBuffer.append(buf, offset, len);
    }
  }

}
