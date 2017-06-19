/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.util.ByteBufferInputStreamAdapter;
import io.github.msdk.io.mzml2.util.MzMLFileMemoryMapper;
import it.unimi.dsi.io.ByteBufferInputStream;
import javolution.text.CharArray;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

/**
 * <p>
 * MzMLFileParser class.
 * </p>
 *
 * @author plusik
 * @version $Id: $Id
 */
public class MzMLFileParser implements MSDKMethod<RawDataFile> {
  private final @Nonnull File mzMLFile;
  private final @Nonnull ArrayList<MsScan> spectrumList;
  private final ArrayList<MzMLReferenceableParamGroup> referenceableParamGroupList;
  private MzMLRawDataFile newRawFile;
  private Integer lastScanNumber = 0;
  private volatile boolean canceled;
  private Float progress;

  final static String ATTR_ACCESSION = "accession";
  final static String ATTR_VALUE = "value";
  final static String ATTR_UNIT_ACCESSION = "unitAccession";

  final static String TAG_SPECTRUM = "spectrum";
  final static String TAG_SPECTRUM_LIST = "spectrumList";
  final static String TAG_REF_PARAM_GROUP = "referenceableParamGroup";
  final static String TAG_REF_PARAM_GROUP_LIST = "referenceableParamGroupList";
  final static String TAG_CV_PARAM = "cvParam";
  final static String TAG_BINARY = "binary";
  final static String TAG_BINARY_DATA_ARRAY = "binaryDataArray";

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.lang.String} object.
   */
  public MzMLFileParser(String mzMLFilePath) {
    this(new File(mzMLFilePath));
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.nio.file.Path} object.
   */
  public MzMLFileParser(Path mzMLFilePath) {
    this(mzMLFilePath.toFile());
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFile a {@link java.io.File} object.
   */
  public MzMLFileParser(File mzMLFile) {
    this.mzMLFile = mzMLFile;
    this.spectrumList = new ArrayList<>();
    this.referenceableParamGroupList = new ArrayList<>();
    this.canceled = false;
    this.progress = 0f;
  }

  /**
   * <p>
   * execute.
   * </p>
   *
   * @return a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object. @throws
   *         io.github.msdk.MSDKException if any. @throws
   */
  public MzMLRawDataFile execute() throws MSDKException {

    try {
      MzMLFileMemoryMapper mapper = new MzMLFileMemoryMapper();
      ByteBufferInputStream is = mapper.mapToMemory(mzMLFile);

      List<Chromatogram> chromatogramsList = new ArrayList<>();
      List<MsFunction> msFunctionsList = new ArrayList<>();

      // Create the MzMLRawDataFile object
      final MzMLRawDataFile newRawFile =
          new MzMLRawDataFile(mzMLFile, msFunctionsList, spectrumList, chromatogramsList);
      this.newRawFile = newRawFile;

      // XMLInputFactory xmlInputFactory = OSGiServices.getXMLInputFactory();
      // XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);

      // It's ok to directly create this particular reader, this class is `public final`
      // and we precisely want that fast UFT-8 reader implementation
      final XMLStreamReaderImpl xmlStreamReader = new XMLStreamReaderImpl();
      xmlStreamReader.setInput(is, "UTF-8");

      Vars vars = new Vars();
      Logger logger = LoggerFactory.getLogger(this.getClass());

      int eventType;
      try {
        loop: do {
          // check if parsing has been cancelled?
          if (canceled)
            return null;

          eventType = xmlStreamReader.next();

          switch (eventType) {
            case XMLStreamConstants.START_ELEMENT:
              // opening tag
              final CharArray openingTagName = xmlStreamReader.getLocalName();

              if (openingTagName.contentEquals(TAG_SPECTRUM_LIST)) {
                vars.insideSpectrumList = true;
                continue;

              } else if (openingTagName.contentEquals(TAG_REF_PARAM_GROUP_LIST)) {
                vars.insideReferenceableParamGroupList = true;
                continue;
              }

              if (vars.insideReferenceableParamGroupList) {

                if (openingTagName.contentEquals(TAG_REF_PARAM_GROUP)) {
                  final CharArray id = xmlStreamReader.getAttributeValue(null, "id");
                  if (id == null) {
                    throw new IllegalStateException(
                        "Tag " + TAG_REF_PARAM_GROUP + " must provide an `id` attribute.");
                  }
                  vars.referenceableParamGroup = new MzMLReferenceableParamGroup(id.toString());

                } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                  MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                  vars.referenceableParamGroup.addReferenceableCvParam(cvParam);

                }
                continue;
              }

              if (vars.insideSpectrumList) {
                if (openingTagName.contentEquals(TAG_SPECTRUM)) {
                  vars.spectrum = new MzMLSpectrum(newRawFile);
                  String id = xmlStreamReader.getAttributeValue(null, "id").toString();
                  vars.defaultArrayLength =
                      xmlStreamReader.getAttributeValue(null, "defaultArrayLength").toInt();
                  vars.spectrum.setId(id);
                  vars.spectrum.setScanNumber(getScanNumber(id));
                  vars.spectrum.setByteBufferInputStream(is);


                } else if (openingTagName.contentEquals(TAG_BINARY_DATA_ARRAY)) {
                  vars.insideBinaryDataArrayFlag = true;
                  vars.binaryDataInfo = new MzMLBinaryDataInfo();
                  int encodedLength =
                      xmlStreamReader.getAttributeValue(null, "encodedLength").toInt();
                  vars.binaryDataInfo.setEncodedLength(encodedLength);
                  final CharArray arrayLength =
                      xmlStreamReader.getAttributeValue(null, "arrayLength");
                  if (arrayLength != null) {
                    vars.defaultArrayLength = arrayLength.toInt();
                  }
                  vars.binaryDataInfo.setArrayLength(vars.defaultArrayLength);


                } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                  if (!vars.insideBinaryDataArrayFlag && vars.spectrum != null) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    vars.spectrum.getCVParams().add(cvParam);
                  }


                } else if (openingTagName.contentEquals(TAG_BINARY)) {
                  if (vars.spectrum != null) {
                    vars.binaryDataInfo
                        .setPosition(xmlStreamReader.getLocation().getCharacterOffset());
                    ByteBufferInputStreamAdapter decodedIs = new ByteBufferInputStreamAdapter(
                        is.copy(), vars.binaryDataInfo.getPosition(),
                        vars.binaryDataInfo.getEncodedLength());
                    logger.debug(new String(IOUtils.toByteArray(decodedIs)));
                  }


                } else if (openingTagName.contentEquals(TAG_REF_PARAM_GROUP)) {
                  String refValue = xmlStreamReader.getAttributeValue(null, "ref").toString();

                  for (MzMLReferenceableParamGroup ref : referenceableParamGroupList) {
                    if (ref.getParamGroupName().equals(refValue)) {
                      vars.spectrum.getCVParams().addAll(ref.getReferenceableCvParams());
                      break;
                    }
                  }

                }
              }

              if (vars.insideBinaryDataArrayFlag && openingTagName.contentEquals(TAG_CV_PARAM)
                  && vars.binaryDataInfo != null) {
                String accession = xmlStreamReader.getAttributeValue(null, "accession").toString();
                if (vars.binaryDataInfo.isBitLengthAccession(accession)) {
                  vars.binaryDataInfo.setBitLength(accession);
                } else if (vars.binaryDataInfo.isCompressionTypeAccession(accession)) {
                  vars.binaryDataInfo.setCompressionType(accession);
                } else if (vars.binaryDataInfo.isArrayTypeAccession(accession)) {
                  vars.binaryDataInfo.setArrayType(accession);
                } else {
                  break loop; // A better approach to skip UV Scans would
                  // be to only break accession which define
                  // the array type and isn't either m/z or
                  // intensity values. We would have to list
                  // out all array types in that case.
                }

              }

              break;

            case XMLStreamConstants.END_ELEMENT:
              // closing tag
              final CharArray closingTagName = xmlStreamReader.getLocalName();
              switch (closingTagName.toString()) {
                case TAG_SPECTRUM_LIST:
                  vars.insideSpectrumList = false;
                  break;
                case TAG_REF_PARAM_GROUP:
                  referenceableParamGroupList.add(vars.referenceableParamGroup);
                  break;
                case TAG_REF_PARAM_GROUP_LIST:
                  vars.insideReferenceableParamGroupList = false;
                  break;
              }
              if (vars.insideSpectrumList) {
                switch (closingTagName.toString()) {
                  case TAG_BINARY_DATA_ARRAY:
                    if ("MS:1000514".equals(vars.binaryDataInfo.getArrayType().getValue())) {
                      vars.spectrum.setMzBinaryDataInfo(vars.binaryDataInfo);
                    }
                    if ("MS:1000515".equals(vars.binaryDataInfo.getArrayType().getValue())) {
                      vars.spectrum.setIntensityBinaryDataInfo(vars.binaryDataInfo);
                    }
                    vars.insideBinaryDataArrayFlag = false;
                    break;
                  case TAG_SPECTRUM:
                    spectrumList.add(vars.spectrum);
                }
              }

              break;

            case XMLStreamConstants.CHARACTERS:
              break;
          }

        } while (eventType != XMLStreamConstants.END_DOCUMENT);
      } finally {
        if (xmlStreamReader != null) {
          xmlStreamReader.close();
        }
      }
      progress = 1f;
    } catch (IOException | XMLStreamException | javax.xml.stream.XMLStreamException e) {
      throw (new MSDKException(e));
    }

    progress = 1f;
    return newRawFile;
  }

  private MzMLCVParam createMzMLCVParam(XMLStreamReader xmlStreamReader) {
    CharArray accession = xmlStreamReader.getAttributeValue(null, ATTR_ACCESSION);
    CharArray value = xmlStreamReader.getAttributeValue(null, ATTR_VALUE);
    CharArray unitAccession = xmlStreamReader.getAttributeValue(null, ATTR_UNIT_ACCESSION);

    // accession is a required attribute
    if (accession == null) {
      throw new IllegalStateException("Any cvParam must have an accession.");
    }

    // these attributes are optional
    String valueStr = value == null ? null : value.toString();
    String unitAccessionStr = unitAccession == null ? null : unitAccession.toString();

    return new MzMLCVParam(accession.toString(), valueStr, unitAccessionStr);
  }

  /**
   * <p>
   * Getter for the field <code>spectrumList</code>.
   * </p>
   *
   * @return a {@link java.util.ArrayList} object.
   */
  public ArrayList<MsScan> getSpectrumList() {
    return spectrumList;
  }

  /**
   * <p>
   * getScanNumber.
   * </p>
   *
   * @param spectrumId a {@link java.lang.String} object.
   * @return a {@link java.lang.Integer} object.
   */
  public Integer getScanNumber(String spectrumId) {
    final Pattern pattern = Pattern.compile("scan=([0-9]+)");
    final Matcher matcher = pattern.matcher(spectrumId);
    boolean scanNumberFound = matcher.find();

    // Some vendors include scan=XX in the ID, some don't, such as
    // mzML converted from WIFF files. See the definition of nativeID in
    // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
    if (scanNumberFound) {
      Integer scanNumber = Integer.parseInt(matcher.group(1));
      lastScanNumber = scanNumber;
      return scanNumber;
    }

    Integer scanNumber = lastScanNumber + 1;
    lastScanNumber++;
    return scanNumber;
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

  private class Vars {

    boolean insideSpectrumList;
    boolean insideBinaryDataArrayFlag;
    boolean insideReferenceableParamGroupList;
    int defaultArrayLength;
    MzMLSpectrum spectrum;
    MzMLBinaryDataInfo binaryDataInfo;
    MzMLReferenceableParamGroup referenceableParamGroup;

    Vars() {
      insideSpectrumList = false;
      insideBinaryDataArrayFlag = false;
      insideReferenceableParamGroupList = false;
      defaultArrayLength = 0;
      spectrum = null;
      binaryDataInfo = null;
      referenceableParamGroup = null;
    }
  }
}
