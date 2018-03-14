package io.github.msdk.io.mzxml;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.IsolationInfo;
import io.github.msdk.datamodel.MsScanType;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.PolarityType;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleIsolationInfo;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.spectra.centroidprofiledetection.SpectrumTypeDetectionAlgorithm;
import it.unimi.dsi.io.ByteBufferInputStream;
import javolution.text.CharArray;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamReader;

/**
 * <p>
 * MzXMLFileParser class.
 * </p>
 *
 */
public class MzXMLFileParser implements MSDKMethod<RawDataFile> {

  private final @Nonnull File mzXMLFile;
  private MzXMLRawDataFile newRawFile;
  private volatile boolean canceled;
  private Float progress;
  private int lastLoggedProgress;
  private Logger logger;

  private SimpleMsScan buildingScan;
  private DatatypeFactory dataTypeFactory;

  final static String TAG_MS_RUN = "msRun";
  final static String TAG_SCAN = "scan";
  final static String TAG_PEAKS = "peaks";
  final static String TAG_PRECURSOR_MZ = "precursorMz";

  /**
   * <p>
   * Constructor for MzXMLFileParser.
   * </p>
   *
   * @param mzXMLFile a {@link java.io.File} object.
   */
  public MzXMLFileParser(File mzXMLFile) {
    this.mzXMLFile = mzXMLFile;
    this.canceled = false;
    this.progress = 0f;
    this.lastLoggedProgress = 0;
    this.logger = LoggerFactory.getLogger(this.getClass());
  }

  /**
   * <p>
   * Constructor for MzXMLFileParser.
   * </p>
   *
   * @param mzXMLFileName a {@link java.lang.String} object.
   */
  public MzXMLFileParser(String mzXMLFileName) {
    this(new File(mzXMLFileName));
  }

  /**
   * <p>
   * Constructor for MzXMLFileParser.
   * </p>
   *
   * @param mzXMLFilePath a {@link java.nio.file.Path} object.
   */
  public MzXMLFileParser(Path mzXMLFilePath) {
    this(mzXMLFilePath.toFile());
  }

  /**
   * <p>
   * execute.
   * </p>
   *
   * @return a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @throws io.github.msdk.MSDKException if any.
   */
  public RawDataFile execute() throws MSDKException {

    try {
      MzXMLFileMemoryMapper mapper = new MzXMLFileMemoryMapper();
      ByteBufferInputStream is = mapper.mapToMemory(mzXMLFile);

      final XMLStreamReaderImpl xmlStreamReader = new XMLStreamReaderImpl();
      xmlStreamReader.setInput(is, "UTF-8");

      newRawFile = new MzXMLRawDataFile(mzXMLFile);
      dataTypeFactory = DatatypeFactory.newInstance();
      Vars vars = new Vars();

      int eventType;
      try {

        do {
          // check if parsing has been cancelled?
          if (canceled)
            return null;

          eventType = xmlStreamReader.next();

          progress = ((float) xmlStreamReader.getLocation().getCharacterOffset() / is.length());

          // Log progress after every 10% completion
          if ((int) (progress * 100) >= lastLoggedProgress + 10) {
            lastLoggedProgress = (int) (progress * 10) * 10;
            logger.debug("Parsing in progress... " + lastLoggedProgress + "% completed");
          }

          switch (eventType) {
            case XMLStreamConstants.START_ELEMENT:
              final CharArray openingTagName = xmlStreamReader.getLocalName();
              vars.currentTag = openingTagName;

              if (openingTagName.contentEquals(TAG_SCAN)) {
                CharArray scanNumber = getRequiredAttribute(xmlStreamReader, "num");
                CharArray msLevel = getRequiredAttribute(xmlStreamReader, "msLevel");
                CharArray peaksCount = getRequiredAttribute(xmlStreamReader, "peaksCount");

                int scanNumberInt = scanNumber.toInt();
                int msLevelInt = msLevel.toInt();
                vars.peaksCount = peaksCount.toInt();

                CharArray msFuncName = xmlStreamReader.getAttributeValue(null, "scanType");

                buildingScan = new SimpleMsScan(scanNumberInt);

                // MS function
                if (msFuncName != null) {
                  buildingScan.setRawDataFile(newRawFile);
                  buildingScan.setMsLevel(msLevelInt);
                  buildingScan.setMsFunction(msFuncName.toString());

                  // Scan type & definition
                  buildingScan.setMsScanType(MsScanType.UNKNOWN);

                  // String filterLine = attrs.getValue("filterLine"); //Copied from the current
                  // parser, always null
                  buildingScan.setScanDefinition(null);
                }

                // Polarity
                PolarityType polarity = PolarityType.UNKNOWN;
                CharArray polarityAttr = xmlStreamReader.getAttributeValue(null, "polarity");
                if (polarityAttr != null) {
                  switch (polarityAttr.charAt(0)) {
                    case '+':
                      polarity = PolarityType.POSITIVE;
                      break;
                    case '-':
                      polarity = PolarityType.NEGATIVE;
                      break;
                  }
                }
                buildingScan.setPolarity(polarity);

                // Parse retention time
                CharArray retentionTimeStr =
                    xmlStreamReader.getAttributeValue(null, "retentionTime");
                if (retentionTimeStr != null) {
                  Date currentDate = new Date();
                  Duration dur = dataTypeFactory.newDuration(retentionTimeStr.toString());
                  final float rt = (float) (dur.getTimeInMillis(currentDate) / 1000.0);
                  buildingScan.setRetentionTime(rt);
                }

              } else if (openingTagName.contentEquals(TAG_PEAKS)) {
                vars.compressionFlag = false;
                CharArray compressionType =
                    xmlStreamReader.getAttributeValue(null, "compressionType");
                if (compressionType != null && !compressionType.contentEquals("none"))
                  vars.compressionFlag = true;

                CharArray precision = getRequiredAttribute(xmlStreamReader, "precision");
                vars.precision = precision.toString();

                vars.peaksStart = xmlStreamReader.getLocation().getCharacterOffset();

              } else if (openingTagName.contentEquals(TAG_PRECURSOR_MZ)) {
                CharArray precursorCharge =
                    xmlStreamReader.getAttributeValue(null, "precursorCharge");
                if (precursorCharge != null)
                  vars.precursorCharge = precursorCharge.toInt();

                CharArray precursorScanNumber =
                    xmlStreamReader.getAttributeValue(null, "precursorScanNum");
                if (precursorScanNumber != null)
                  vars.precursorScanNumber = precursorScanNumber.toInt();
              }

              break;

            case XMLStreamConstants.END_ELEMENT:

              final CharArray closingTagName = xmlStreamReader.getLocalName();

              switch (closingTagName.toString()) {
                case TAG_SCAN:
                  newRawFile.addScan(buildingScan);
                  break;
                case TAG_PEAKS:
                  double[] mzValues = new double[vars.peaksCount];
                  float[] intensityValues = new float[vars.peaksCount];

                  // Base64 decoder
                  InputStream decodedIs = Base64.getDecoder().wrap(vars.peaksChars);
                  InflaterInputStream iis = null;
                  DataInputStream peakStream = null;

                  // Decompress if the array is compressed
                  if (vars.compressionFlag) {
                    iis = new InflaterInputStream(decodedIs);
                    peakStream = new DataInputStream(iis);
                  } else {
                    peakStream = new DataInputStream(decodedIs);
                  }

                  for (int i = 0; i < vars.peaksCount; i++) {

                    // Always respect this order pairOrder="m/z-int"
                    if ("64".equals(vars.precision)) {
                      mzValues[i] = peakStream.readDouble();
                      intensityValues[i] = (float) peakStream.readDouble();
                    } else {
                      mzValues[i] = (double) peakStream.readFloat();
                      intensityValues[i] = peakStream.readFloat();
                    }

                  }

                  // Set the final data points to the scan
                  buildingScan.setDataPoints(mzValues, intensityValues, vars.peaksCount);

                  // Auto-detect whether this scan is centroided
                  MsSpectrumType spectrumType = SpectrumTypeDetectionAlgorithm
                      .detectSpectrumType(mzValues, intensityValues, vars.peaksCount);
                  buildingScan.setSpectrumType(spectrumType);
                  break;
              }
              break;

            case XMLStreamConstants.CHARACTERS:
              if (vars.currentTag != null) {
                switch (vars.currentTag.toString()) {
                  case TAG_PEAKS:
                    vars.peaksChars = new ByteBufferInputStreamAdapter(is.copy(), vars.peaksStart,
                        xmlStreamReader.getTextLength());
                    break;
                  case TAG_PRECURSOR_MZ:
                    IsolationInfo newIsolation = new SimpleIsolationInfo(
                        Range.singleton(xmlStreamReader.getText().toDouble()), null,
                        xmlStreamReader.getText().toDouble(), vars.precursorCharge, null, vars.precursorScanNumber);
                    buildingScan.getIsolations().add(newIsolation);
                    break;
                }
              }
              break;
          }

        } while (eventType != XMLStreamConstants.END_DOCUMENT);

      } finally {
        if (xmlStreamReader != null) {
          xmlStreamReader.close();
        }
      }
    } catch (Exception e) {
      throw (new MSDKException(e));
    }
    return newRawFile;

  }

  /**
   * <p>
   * Gets the required attribute from xmlStreamReader, throws an exception of the attribute is not
   * found
   * </p>
   *
   * @return a CharArray containing the value of the attribute.
   * @param xmlStreamReader a {@link javolution.xml.stream.XMLStreamReader} object.
   * @param attr a {@link java.lang.String} object.
   */
  public CharArray getRequiredAttribute(XMLStreamReader xmlStreamReader, String attr) {
    CharArray attrValue = xmlStreamReader.getAttributeValue(null, attr);
    if (attrValue == null)
      throw new IllegalStateException("Tag " + xmlStreamReader.getLocalName() + " must provide an `"
          + attr + "`attribute (Line " + xmlStreamReader.getLocation().getLineNumber() + ")");
    return attrValue;
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    return progress;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile getResult() {
    return newRawFile;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

}



class Vars {
  String precision;
  Integer precursorCharge;
  Integer precursorScanNumber;
  int peaksCount;
  boolean compressionFlag;
  CharArray currentTag;
  Integer peaksStart;
  InputStream peaksChars;

  Vars() {
    precision = null;
    precursorCharge = null;
    precursorScanNumber = null;
    peaksCount = 0;
    compressionFlag = false;
    currentTag = null;
    peaksStart = 0;
    peaksChars = null;
  }
}
