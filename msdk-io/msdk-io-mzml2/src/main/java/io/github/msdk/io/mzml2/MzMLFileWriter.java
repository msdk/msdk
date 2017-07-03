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

package io.github.msdk.io.mzml2;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.data.MzMLArrayType;
import io.github.msdk.io.mzml2.data.MzMLBitLength;
import io.github.msdk.io.mzml2.data.MzMLCV;
import io.github.msdk.io.mzml2.data.MzMLCVParam;
import io.github.msdk.io.mzml2.data.MzMLCompressionType;
import io.github.msdk.io.mzml2.util.MzMLPeaksEncoder;

public class MzMLFileWriter implements MSDKMethod<Void> {

  private static final String dataProcessingId = "MSDK_mzml_export";
  private static final String softwareId = "MSDK";
  private static final String XML_ENCODING = "UTF-8";
  private static final String XML_VERSION = "1.0";
  private static final String MZML_NAMESPACE = "http://psi.hupo.org/ms/mzml";
  private static final String DEFAULT_VERSION = "1.1.0";
  private static final String CV_REF_MS = "MS";

  private static final String TAG_MZML = "mzML";
  private static final String TAG_CV_LIST = "cvList";
  private static final String TAG_DATA_PROCESSING_LIST = "dataProcessingList";
  private static final String TAG_DATA_PROCESSING = "dataProcessing";
  private static final String TAG_PROCESSING_METHOD = "processingMethod";
  private static final String TAG_RUN = "run";
  private static final String TAG_SPECTRUM_LIST = "spectrumList";
  private static final String TAG_SPECTRUM = "spectrum";
  private static final String TAG_CV_PARAM = "cvParam";
  private static final String TAG_SCAN_LIST = "scanList";
  private static final String TAG_SCAN = "scan";
  private static final String TAG_SCAN_WINDOW_LIST = "scanWindowList";
  private static final String TAG_SCAN_WINDOW = "scanWindow";
  private static final String TAG_BINARY_DATA_ARRAY_LIST = "binaryDataArrayList";
  private static final String TAG_BINARY_DATA_ARRAY = "binaryDataArray";
  private static final String TAG_BINARY = "binary";
  private static final String TAG_CHROMATOGRAM_LIST = "chromatogramList";
  private static final String TAG_CHROMATOGRAM = "chromatogram";
  private static final String TAG_PRECURSOR = "precursor";
  private static final String TAG_ISOLATION_WINDOW = "isolationWindow";
  private static final String TAG_ACTIVATION = "activation";
  private static final String TAG_PRODUCT = "product";

  private static final String ATTR_ID = "id";
  private static final String ATTR_VERSION = "version";
  private static final String ATTR_COUNT = "count";
  private static final String ATTR_SOFTWARE_REF = "softwareRef";
  private static final String ATTR_ORDER = "order";
  private static final String ATTR_DEFAULT_ARRAY_LENGTH = "defaultArrayLength";
  private static final String ATTR_INDEX = "index";
  private static final String ATTR_CV_REF = "cvRef";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_ACCESSION = "accession";
  private static final String ATTR_VALUE = "value";
  private static final String ATTR_UNIT_ACCESSION = "unitAccession";
  private static final String ATTR_ENCODED_LENGTH = "encodedLength";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull RawDataFile rawDataFile;
  private final @Nonnull File target;
  private final @Nonnull MzMLCompressionType doubleValuesCompression;
  private final @Nonnull MzMLCompressionType floatValuesCompression;

  private boolean canceled = false;

  private long totalScans = 0, totalChromatograms = 0, parsedScans, parsedChromatograms;

  public MzMLFileWriter(@Nonnull RawDataFile rawDataFile, @Nonnull File target,
      @Nonnull MzMLCompressionType doubleValuesCompression,
      MzMLCompressionType floatValuesCompression) {
    this.rawDataFile = rawDataFile;
    this.target = target;
    this.doubleValuesCompression = doubleValuesCompression;
    this.floatValuesCompression = floatValuesCompression;
  }

  @Override
  public Void execute() throws MSDKException {

    logger.info("Started export of " + rawDataFile.getName() + " to " + target);

    List<MsScan> scans = rawDataFile.getScans();
    List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
    totalScans = scans.size();
    totalChromatograms = chromatograms.size();

    try {

      FileWriter writer = new FileWriter(target);
      XMLStreamWriter xmlStreamWriter =
          XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
      xmlStreamWriter = new IndentingXMLStreamWriter(xmlStreamWriter);
      xmlStreamWriter.setDefaultNamespace(MZML_NAMESPACE);

      // <?xml>
      xmlStreamWriter.writeStartDocument(XML_ENCODING, XML_VERSION);

      // <mzML>
      xmlStreamWriter.writeStartElement(TAG_MZML);
      xmlStreamWriter.writeDefaultNamespace(MZML_NAMESPACE);
      xmlStreamWriter.writeAttribute(ATTR_ID, rawDataFile.getName());
      xmlStreamWriter.writeAttribute(ATTR_VERSION, DEFAULT_VERSION);

      // <cvList>
      xmlStreamWriter.writeStartElement(TAG_CV_LIST);
      // TODO: Hold cvList in the existing RawDataFile model
      xmlStreamWriter.writeEndElement();

      // <dataProcessingList>
      xmlStreamWriter.writeStartElement(TAG_DATA_PROCESSING_LIST);
      xmlStreamWriter.writeAttribute(ATTR_COUNT, "1");

      // <dataProcessing>
      xmlStreamWriter.writeStartElement(TAG_DATA_PROCESSING);
      xmlStreamWriter.writeAttribute(ATTR_ID, dataProcessingId);

      // <processingMethod>
      xmlStreamWriter.writeStartElement(TAG_PROCESSING_METHOD);
      xmlStreamWriter.writeAttribute(ATTR_SOFTWARE_REF, softwareId);
      xmlStreamWriter.writeAttribute(ATTR_ORDER, "0");

      // Closing tags
      xmlStreamWriter.writeEndElement(); // </processingMethod>
      xmlStreamWriter.writeEndElement(); // </dataProcessing>
      xmlStreamWriter.writeEndElement(); // </dataProcessingList>

      // <run>
      xmlStreamWriter.writeStartElement(TAG_RUN);
      xmlStreamWriter.writeAttribute(ATTR_ID, rawDataFile.getName());

      // <spectrumList>
      xmlStreamWriter.writeStartElement(TAG_SPECTRUM_LIST);
      xmlStreamWriter.writeAttribute(ATTR_COUNT, String.valueOf(scans.size()));

      byte[] mzBuffer = null;
      byte[] intensityBuffer = null;

      for (MsScan scan : scans) {

        if (canceled) {
          writer.close();
          xmlStreamWriter.close();
          target.delete();
          return null;
        }

        // <spectrum>
        xmlStreamWriter.writeStartElement(TAG_SPECTRUM);
        xmlStreamWriter.writeAttribute(ATTR_INDEX, String.valueOf(parsedScans));
        xmlStreamWriter.writeAttribute(ATTR_ID, "scan=" + scan.getScanNumber());
        xmlStreamWriter.writeAttribute(ATTR_DEFAULT_ARRAY_LENGTH,
            String.valueOf(scan.getNumberOfDataPoints()));

        // spectrum type CV param
        if (scan.getSpectrumType() == MsSpectrumType.CENTROIDED)
          writeCVParam(xmlStreamWriter, MzMLCV.centroidCvParam);
        else
          writeCVParam(xmlStreamWriter, MzMLCV.profileCvParam);

        // ms level CV param
        if (scan.getMsLevel() != null) {
          Integer msLevel = scan.getMsLevel();
          writeCVParam(xmlStreamWriter,
              new MzMLCVParam(MzMLCV.cvMSLevel, String.valueOf(msLevel), "ms level", null));
        }

        // total ion current CV param
        if (scan.getTIC() != null) {
          Float tic = scan.getTIC();
          writeCVParam(xmlStreamWriter,
              new MzMLCVParam(MzMLCV.cvTIC, String.valueOf(tic), "total ion current", null));
        }

        // m/z range CV param
        if (scan.getMzRange() != null) {
          Double lowestMz = scan.getMzRange().lowerEndpoint();
          Double highestMz = scan.getMzRange().upperEndpoint();
          writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvLowestMz, String.valueOf(lowestMz),
              "lowest observed m/z", MzMLCV.cvMz));
          writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvHighestMz,
              String.valueOf(highestMz), "highest observed m/z", MzMLCV.cvMz));
        }

        // <scanList>
        xmlStreamWriter.writeStartElement(TAG_SCAN_LIST);
        xmlStreamWriter.writeAttribute(ATTR_COUNT, "1");

        // <scan>
        xmlStreamWriter.writeStartElement(TAG_SCAN);

        // scan definition CV param
        if (scan.getScanDefinition() != null) {
          String scanDefinition = scan.getScanDefinition();
          writeCVParam(xmlStreamWriter,
              new MzMLCVParam(MzMLCV.cvScanFilterString, scanDefinition, "filter string", null));
        }

        // retention time CV param
        if (scan.getRetentionTime() != null) {
          Float rt = scan.getRetentionTime();
          writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.MS_RT_SCAN_START, String.valueOf(rt),
              "scan time", MzMLCV.cvUnitsSec));
        }

        // scan polarity CV param
        if (scan.getPolarity() == PolarityType.POSITIVE)
          writeCVParam(xmlStreamWriter, MzMLCV.polarityPositiveCvParam);
        else if (scan.getPolarity() == PolarityType.NEGATIVE)
          writeCVParam(xmlStreamWriter, MzMLCV.polarityNegativeCvParam);

        // <scanWindowList>
        xmlStreamWriter.writeStartElement(TAG_SCAN_WINDOW_LIST);
        xmlStreamWriter.writeAttribute(ATTR_COUNT, "1");

        // <scanWindow>
        xmlStreamWriter.writeStartElement(TAG_SCAN_WINDOW);

        // scan window range CV param
        if (scan.getScanningRange() != null) {
          Double lowerLimit = scan.getScanningRange().lowerEndpoint();
          Double upperLimit = scan.getScanningRange().upperEndpoint();
          writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvScanWindowLowerLimit,
              String.valueOf(lowerLimit), "scan window lower limit", MzMLCV.cvMz));
          writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvScanWindowUpperLimit,
              String.valueOf(upperLimit), "scan window upper limit", MzMLCV.cvMz));
        }

        // Closing tags
        xmlStreamWriter.writeEndElement(); // </scanWindow>
        xmlStreamWriter.writeEndElement(); // </scanWindowList>
        xmlStreamWriter.writeEndElement(); // </scan>
        xmlStreamWriter.writeEndElement(); // </scanList>

        // <binaryDataArrayList>
        xmlStreamWriter.writeStartElement(TAG_BINARY_DATA_ARRAY_LIST);
        xmlStreamWriter.writeAttribute(ATTR_COUNT, "2");

        // <binaryDataArray> (m/z)
        mzBuffer = MzMLPeaksEncoder.encodeDouble(scan.getMzValues(), doubleValuesCompression);
        xmlStreamWriter.writeStartElement(TAG_BINARY_DATA_ARRAY);
        xmlStreamWriter.writeAttribute(ATTR_ENCODED_LENGTH, String.valueOf(mzBuffer.length));

        // data array precision CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLBitLength.SIXTY_FOUR_BIT_FLOAT.getValue(),
            "", "64-bit float", null));

        // data array compression CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(doubleValuesCompression.getAccession(), "",
            doubleValuesCompression.getName(), null));

        // data array type CV param
        writeCVParam(xmlStreamWriter,
            new MzMLCVParam(MzMLArrayType.MZ.getValue(), "", "m/z array", MzMLCV.cvMz));

        // <binary>
        xmlStreamWriter.writeStartElement(TAG_BINARY);
        xmlStreamWriter.writeCharacters(new String(mzBuffer));

        // Closing tags
        xmlStreamWriter.writeEndElement(); // </binary>
        xmlStreamWriter.writeEndElement(); // </binaryDataArray>

        // <binaryDataArray> (intensity)
        intensityBuffer =
            MzMLPeaksEncoder.encodeFloat(scan.getIntensityValues(), floatValuesCompression);
        xmlStreamWriter.writeStartElement(TAG_BINARY_DATA_ARRAY);
        xmlStreamWriter.writeAttribute(ATTR_ENCODED_LENGTH, String.valueOf(intensityBuffer.length));

        // data array precision CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLBitLength.THIRTY_TWO_BIT_FLOAT.getValue(),
            "", "32-bit float", null));

        // data array compression CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(floatValuesCompression.getAccession(), "",
            floatValuesCompression.getName(), null));

        // data array type CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLArrayType.INTENSITY.getValue(), "",
            "intensity array", MzMLCV.cvUnitsIntensity1));

        // <binary>
        xmlStreamWriter.writeStartElement(TAG_BINARY);
        xmlStreamWriter.writeCharacters(new String(intensityBuffer));

        // Closing tags
        xmlStreamWriter.writeEndElement(); // </binary>
        xmlStreamWriter.writeEndElement(); // </binaryDataArray>
        xmlStreamWriter.writeEndElement(); // </binaryDataArrayList
        xmlStreamWriter.writeEndElement(); // </spectrum>

        parsedScans++;

      }

      xmlStreamWriter.writeEndElement(); // </spectrumList>

      // <chromatogramList>
      xmlStreamWriter.writeStartElement(TAG_CHROMATOGRAM_LIST);
      xmlStreamWriter.writeAttribute(ATTR_COUNT, String.valueOf(chromatograms.size()));

      byte[] rtBuffer = null;
      byte[] intensityBuffer2 = null;

      for (Chromatogram chromatogram : chromatograms) {
        if (canceled) {
          writer.close();
          xmlStreamWriter.close();
          target.delete();
          return null;
        }

        // <chromatogram>
        xmlStreamWriter.writeStartElement(TAG_CHROMATOGRAM);
        xmlStreamWriter.writeAttribute(ATTR_INDEX, String.valueOf(parsedChromatograms));
        xmlStreamWriter.writeAttribute(ATTR_ID, chromatogram.getChromatogramType().name());
        xmlStreamWriter.writeAttribute(ATTR_DEFAULT_ARRAY_LENGTH,
            String.valueOf(chromatogram.getNumberOfDataPoints()));

        // chromatogram type CV param
        switch (chromatogram.getChromatogramType()) {
          case BPC:
            writeCVParam(xmlStreamWriter,
                new MzMLCVParam(MzMLCV.cvChromatogramBPC, "", "basepeak chromatogram", null));
            break;
          case MRM_SRM:
            writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvChromatogramMRM_SRM, "",
                "selected reaction monitoring chromatogram", null));
            break;
          case SIC:
            writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvChromatogramSIC, "",
                "selected ion current chromatogram", null));
            break;
          case TIC:
            writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvChromatogramTIC, "",
                "total ion current chromatogram", null));
            break;
          default:
            break;
        }

        // Isolation info
        if (!chromatogram.getIsolations().isEmpty()) {
          IsolationInfo isolationInfo = chromatogram.getIsolations().get(0);

          // <precursor>
          xmlStreamWriter.writeStartElement(TAG_PRECURSOR);

          if (isolationInfo.getPrecursorMz() != null) {
            // <isolationWindow>
            xmlStreamWriter.writeStartElement(TAG_ISOLATION_WINDOW);

            Double mz = isolationInfo.getPrecursorMz();
            writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvIsolationWindowTarget,
                String.valueOf(mz), "isolation window target m/z", MzMLCV.cvMz));

            xmlStreamWriter.writeEndElement(); // </isolationWindow>
          }

          if (isolationInfo.getActivationInfo() != null) {
            // <activation>
            xmlStreamWriter.writeStartElement(TAG_ACTIVATION);

            ActivationInfo activationInfo = isolationInfo.getActivationInfo();

            switch (activationInfo.getActivationType()) {
              case CID:
                writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvActivationCID, "",
                    "collision-induced dissociation", null));
                break;
              default:
                break;
            }

            if (activationInfo.getActivationEnergy() != null)
              writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvActivationEnergy,
                  String.valueOf(activationInfo.getActivationEnergy()), "collision energy", null));

            xmlStreamWriter.writeEndElement(); // </activation>
          }

          xmlStreamWriter.writeEndElement(); // </precursor>
        }


        // product m/z value CV param
        if (chromatogram.getMz() != null) {
          // <product>
          xmlStreamWriter.writeStartElement(TAG_PRODUCT);

          // <isolationWindow>
          xmlStreamWriter.writeStartElement(TAG_ISOLATION_WINDOW);

          Double mz = chromatogram.getMz();
          writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLCV.cvIsolationWindowTarget,
              String.valueOf(mz), "isolation window target m/z", MzMLCV.cvMz));

          // Closing tags
          xmlStreamWriter.writeEndElement(); // </isolationWindow>
          xmlStreamWriter.writeEndElement(); // </product>
        }

        // <binaryDataArrayList>
        xmlStreamWriter.writeStartElement(TAG_BINARY_DATA_ARRAY_LIST);
        xmlStreamWriter.writeAttribute(ATTR_COUNT, "2");

        // <binaryDataArray> (time)
        rtBuffer = MzMLPeaksEncoder.encodeFloat(chromatogram.getRetentionTimes(null),
            floatValuesCompression);
        xmlStreamWriter.writeStartElement(TAG_BINARY_DATA_ARRAY);
        xmlStreamWriter.writeAttribute(ATTR_ENCODED_LENGTH, String.valueOf(rtBuffer.length));

        // data array precision CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLBitLength.THIRTY_TWO_BIT_FLOAT.getValue(),
            "", "32-bit float", null));

        // data array compression CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(floatValuesCompression.getAccession(), "",
            floatValuesCompression.getName(), null));

        // data array type CV param
        writeCVParam(xmlStreamWriter,
            new MzMLCVParam(MzMLArrayType.TIME.getValue(), "", "time array", MzMLCV.cvUnitsMin2));

        // <binary>
        xmlStreamWriter.writeStartElement(TAG_BINARY);
        xmlStreamWriter.writeCharacters(new String(rtBuffer));

        // Closing tags
        xmlStreamWriter.writeEndElement(); // </binary>
        xmlStreamWriter.writeEndElement(); // </binaryDataArray>

        // <binaryDataArray> (intensity)
        intensityBuffer2 =
            MzMLPeaksEncoder.encodeFloat(chromatogram.getIntensityValues(), floatValuesCompression);
        xmlStreamWriter.writeStartElement(TAG_BINARY_DATA_ARRAY);
        xmlStreamWriter.writeAttribute(ATTR_ENCODED_LENGTH,
            String.valueOf(intensityBuffer2.length));

        // data array precision CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLBitLength.THIRTY_TWO_BIT_FLOAT.getValue(),
            "", "32-bit float", null));

        // data array compression CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(floatValuesCompression.getAccession(), "",
            floatValuesCompression.getName(), null));

        // data array type CV param
        writeCVParam(xmlStreamWriter, new MzMLCVParam(MzMLArrayType.INTENSITY.getValue(), "",
            "intensity array", MzMLCV.cvUnitsIntensity1));

        // <binary>
        xmlStreamWriter.writeStartElement(TAG_BINARY);
        xmlStreamWriter.writeCharacters(new String(intensityBuffer2));

        // Closing tags
        xmlStreamWriter.writeEndElement(); // </binary>
        xmlStreamWriter.writeEndElement(); // </binaryDataArray>
        xmlStreamWriter.writeEndElement(); // </binaryDataArrayList
        xmlStreamWriter.writeEndElement(); // </chromatogram>

        parsedChromatograms++;
      }

      // Closing tags
      xmlStreamWriter.writeEndElement(); // </chromatogramList>
      xmlStreamWriter.writeEndElement(); // </run>
      xmlStreamWriter.writeEndElement(); // </mzML>

      // Wrapping up
      xmlStreamWriter.writeEndDocument();
      xmlStreamWriter.close();

    } catch (Exception e) {
      throw new MSDKException(e);
    }

    return null;
  }

  @Override
  public Float getFinishedPercentage() {
    return (totalScans + totalChromatograms) == 0 ? null
        : (float) (parsedScans + parsedChromatograms) / (totalScans + totalChromatograms);
  }

  @Override
  public Void getResult() {
    return null;
  }

  @Override
  public void cancel() {
    this.canceled = true;
  }

  private void writeCVParam(XMLStreamWriter xmlStreamWriter, MzMLCVParam cvParam)
      throws XMLStreamException {

    // <cvParam>
    xmlStreamWriter.writeStartElement(TAG_CV_PARAM);

    // cvRef="MS"
    xmlStreamWriter.writeAttribute(ATTR_CV_REF, CV_REF_MS);

    // accession="..."
    xmlStreamWriter.writeAttribute(ATTR_ACCESSION, cvParam.getAccession());

    // Get optional CV param attribute such as value, name and unitAccession and write if they are
    // present
    Optional<String> value = cvParam.getValue(), name = cvParam.getName(),
        unitAccession = cvParam.getUnitAccession();

    if (name.isPresent())
      xmlStreamWriter.writeAttribute(ATTR_NAME, name.get());

    xmlStreamWriter.writeAttribute(ATTR_VALUE, value.orElse(""));

    if (unitAccession.isPresent())
      xmlStreamWriter.writeAttribute(ATTR_UNIT_ACCESSION, unitAccession.get());

    xmlStreamWriter.writeEndElement(); // </cvParam>

  }

}
