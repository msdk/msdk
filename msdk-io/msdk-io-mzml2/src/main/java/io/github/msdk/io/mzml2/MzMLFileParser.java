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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo;
import io.github.msdk.io.mzml2.data.MzMLCVParam;
import io.github.msdk.io.mzml2.data.MzMLIsolationWindow;
import io.github.msdk.io.mzml2.data.MzMLPrecursorActivation;
import io.github.msdk.io.mzml2.data.MzMLPrecursorElement;
import io.github.msdk.io.mzml2.data.MzMLPrecursorSelectedIon;
import io.github.msdk.io.mzml2.data.MzMLPrecursorSelectedIonList;
import io.github.msdk.io.mzml2.data.MzMLProduct;
import io.github.msdk.io.mzml2.data.MzMLRawDataFile;
import io.github.msdk.io.mzml2.data.MzMLReferenceableParamGroup;
import io.github.msdk.io.mzml2.util.MzMLFileMemoryMapper;
import io.github.msdk.io.mzml2.util.XMLTagsTracker;
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
 */
public class MzMLFileParser implements MSDKMethod<RawDataFile> {
  private final @Nonnull File mzMLFile;
  private final ArrayList<MzMLReferenceableParamGroup> referenceableParamGroupList;
  private MzMLRawDataFile newRawFile;
  private volatile boolean canceled;
  private Float progress;
  private int lastLoggedProgress;
  Logger logger;

  final static String ATTR_ACCESSION = "accession";
  final static String ATTR_VALUE = "value";
  final static String ATTR_UNIT_ACCESSION = "unitAccession";

  final static String TAG_SPECTRUM = "spectrum";
  final static String TAG_SPECTRUM_LIST = "spectrumList";
  final static String TAG_REF_PARAM_GROUP = "referenceableParamGroup";
  final static String TAG_REF_PARAM_GROUP_REF = "referenceableParamGroupRef";
  final static String TAG_REF_PARAM_GROUP_LIST = "referenceableParamGroupList";
  final static String TAG_CV_PARAM = "cvParam";
  final static String TAG_BINARY = "binary";
  final static String TAG_BINARY_DATA_ARRAY = "binaryDataArray";
  final static String TAG_PRECURSOR = "precursor";
  final static String TAG_PRECURSOR_LIST = "precursorList";
  final static String TAG_ISOLATION_WINDOW = "isolationWindow";
  final static String TAG_SELECTED_ION_LIST = "selectedIonList";
  final static String TAG_SELECTED_ION = "selectedIon";
  final static String TAG_ACTIVATION = "activation";
  final static String TAG_CHROMATOGRAM = "chromatogram";
  final static String TAG_CHROMATOGRAM_LIST = "chromatogramList";
  final static String TAG_PRODUCT = "product";


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
    this.referenceableParamGroupList = new ArrayList<>();
    this.canceled = false;
    this.progress = 0f;
    this.logger = LoggerFactory.getLogger(this.getClass());
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
      logger.info("Began parsing file: " + mzMLFile.getAbsolutePath());
      ByteBufferInputStream is = MzMLFileMemoryMapper.mapToMemory(mzMLFile);

      List<MsScan> spectrumList = new ArrayList<>();
      List<Chromatogram> chromatogramsList = new ArrayList<>();
      List<MsFunction> msFunctionsList = new ArrayList<>();

      // Create the MzMLRawDataFile object
      final MzMLRawDataFile newRawFile =
          new MzMLRawDataFile(mzMLFile, msFunctionsList, spectrumList, chromatogramsList);
      this.newRawFile = newRawFile;

      // It's ok to directly create this particular reader, this class is `public final`
      // and we precisely want that fast UFT-8 reader implementation
      final XMLStreamReaderImpl xmlStreamReader = new XMLStreamReaderImpl();
      xmlStreamReader.setInput(is, "UTF-8");

      Vars vars = new Vars();
      XMLTagsTracker tagsTracker = new XMLTagsTracker();
      lastLoggedProgress = 0;

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
              // opening tag
              final CharArray openingTagName = xmlStreamReader.getLocalName();
              // TODO Implement CharArray in place of string in XMLTagsTracker to avoid String
              // Garbage
              tagsTracker.enteredTag(openingTagName.toString());

              if (tagsTracker.isInside(TAG_REF_PARAM_GROUP_LIST)) {

                if (openingTagName.contentEquals(TAG_REF_PARAM_GROUP)) {
                  final CharArray id = getRequiredAttribute(xmlStreamReader, "id");
                  vars.referenceableParamGroup = new MzMLReferenceableParamGroup(id.toString());

                } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                  MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                  vars.referenceableParamGroup.addCVParam(cvParam);

                }
                continue;
              }

              if (tagsTracker.isInside(TAG_SPECTRUM_LIST) && !vars.skipScan) {
                if (openingTagName.contentEquals(TAG_SPECTRUM)) {
                  String id = getRequiredAttribute(xmlStreamReader, "id").toString();
                  Integer index = getRequiredAttribute(xmlStreamReader, "index").toInt();
                  vars.defaultArrayLength =
                      getRequiredAttribute(xmlStreamReader, "defaultArrayLength").toInt();
                  Integer scanNumber = getScanNumber(id).orElse(index + 1);
                  vars.spectrum =
                      new MzMLSpectrum(newRawFile, is, id, scanNumber, vars.defaultArrayLength);


                } else if (openingTagName.contentEquals(TAG_BINARY_DATA_ARRAY)) {
                  int encodedLength =
                      getRequiredAttribute(xmlStreamReader, "encodedLength").toInt();
                  final CharArray arrayLength =
                      xmlStreamReader.getAttributeValue(null, "arrayLength");
                  if (arrayLength != null) {
                    vars.binaryDataInfo =
                        new MzMLBinaryDataInfo(encodedLength, arrayLength.toInt());
                  } else {
                    vars.binaryDataInfo =
                        new MzMLBinaryDataInfo(encodedLength, vars.defaultArrayLength);
                  }


                } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                  if (!tagsTracker.isInside(TAG_BINARY_DATA_ARRAY) && vars.spectrum != null) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    vars.spectrum.getCVParams().add(cvParam);
                  }


                } else if (openingTagName.contentEquals(TAG_BINARY)) {
                  if (vars.spectrum != null) {
                    int bomOffset = xmlStreamReader.getLocation().getBomLength();
                    // TODO Fetch long value from getCharacterOffset()
                    vars.binaryDataInfo.setPosition(
                        xmlStreamReader.getLocation().getCharacterOffset() + bomOffset);
                  }


                } else if (openingTagName.contentEquals(TAG_REF_PARAM_GROUP_REF)) {
                  String refValue = getRequiredAttribute(xmlStreamReader, "ref").toString();
                  for (MzMLReferenceableParamGroup ref : referenceableParamGroupList) {
                    if (ref.getParamGroupName().equals(refValue)) {
                      vars.spectrum.getCVParams().addAll(ref.getCVParams());
                      break;
                    }
                  }

                }

                if (tagsTracker.isInside(TAG_SPECTRUM)
                    && tagsTracker.isInside(TAG_BINARY_DATA_ARRAY)
                    && openingTagName.contentEquals(TAG_CV_PARAM) && vars.binaryDataInfo != null) {
                  String accession = getRequiredAttribute(xmlStreamReader, "accession").toString();
                  if (vars.binaryDataInfo.isBitLengthAccession(accession)) {
                    vars.binaryDataInfo.setBitLength(accession);
                  } else if (vars.binaryDataInfo.isCompressionTypeAccession(accession)) {
                    vars.binaryDataInfo.setCompressionType(accession);
                  } else if (vars.binaryDataInfo.isArrayTypeAccession(accession)) {
                    vars.binaryDataInfo.setArrayType(accession);
                  } else {
                    // TODO Omitting warnings, can add later if required (Create a verbose mode for
                    // output maybe)

                    // logger.warn(
                    // "Found binary array other than m/z and intensity! Skipping spectrum scan
                    // (Line
                    // "
                    // + xmlStreamReader.getLocation().getLineNumber() + ")");
                    vars.skipScan = true;
                  }

                }

                if (tagsTracker.isInside(TAG_PRECURSOR_LIST)) {

                  if (openingTagName.contentEquals(TAG_PRECURSOR)) {
                    final CharArray spectrumRef =
                        xmlStreamReader.getAttributeValue(null, "spectrumRef");
                    String spectrumRefString = spectrumRef == null ? null : spectrumRef.toString();
                    vars.precursor = new MzMLPrecursorElement(spectrumRefString);

                  } else if (openingTagName.contentEquals(TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();

                  } else if (openingTagName.contentEquals(TAG_SELECTED_ION_LIST)) {
                    vars.selectedIonList = new MzMLPrecursorSelectedIonList();

                  } else if (openingTagName.contentEquals(TAG_ACTIVATION)) {
                    vars.activation = new MzMLPrecursorActivation();

                  } else if (tagsTracker.isInside(TAG_ISOLATION_WINDOW)) {
                    if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.isolationWindow.addCVParam(cvParam);
                    }

                  } else if (tagsTracker.isInside(TAG_SELECTED_ION_LIST)) {
                    if (openingTagName.contentEquals(TAG_SELECTED_ION)) {
                      vars.selectedIon = new MzMLPrecursorSelectedIon();
                    } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.selectedIon.addCVParam(cvParam);
                    }

                  } else if (tagsTracker.isInside(TAG_ACTIVATION)) {
                    if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.activation.addCVParam(cvParam);
                    }
                  }
                }
              }

              if (tagsTracker.isInside(TAG_CHROMATOGRAM_LIST)) {
                if (openingTagName.contentEquals(TAG_CHROMATOGRAM)) {
                  String chromatogramId = getRequiredAttribute(xmlStreamReader, "id").toString();
                  Integer chromatogramNumber =
                      getRequiredAttribute(xmlStreamReader, "index").toInt() + 1;
                  vars.defaultArrayLength =
                      getRequiredAttribute(xmlStreamReader, "defaultArrayLength").toInt();
                  vars.chromatogram = new MzMLChromatogram(newRawFile, is, chromatogramId,
                      chromatogramNumber, vars.defaultArrayLength);

                } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                  if (!tagsTracker.isInside(TAG_BINARY_DATA_ARRAY)
                      && !tagsTracker.isInside(TAG_PRECURSOR) && !tagsTracker.isInside(TAG_PRODUCT)
                      && vars.chromatogram != null) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    vars.chromatogram.getCVParams().add(cvParam);
                  }

                } else if (openingTagName.contentEquals(TAG_BINARY_DATA_ARRAY)) {
                  int encodedLength =
                      getRequiredAttribute(xmlStreamReader, "encodedLength").toInt();
                  final CharArray arrayLength =
                      xmlStreamReader.getAttributeValue(null, "arrayLength");
                  if (arrayLength != null) {
                    vars.binaryDataInfo =
                        new MzMLBinaryDataInfo(encodedLength, arrayLength.toInt());
                  } else {
                    vars.binaryDataInfo =
                        new MzMLBinaryDataInfo(encodedLength, vars.defaultArrayLength);
                  }

                } else if (openingTagName.contentEquals(TAG_BINARY)) {
                  if (vars.chromatogram != null) {
                    int bomOffset = xmlStreamReader.getLocation().getBomLength();
                    // TODO Fetch long value from getCharacterOffset()
                    vars.binaryDataInfo.setPosition(
                        xmlStreamReader.getLocation().getCharacterOffset() + bomOffset);
                  }

                } else if (openingTagName.contentEquals(TAG_REF_PARAM_GROUP_REF)) {
                  String refValue = xmlStreamReader.getAttributeValue(null, "ref").toString();
                  for (MzMLReferenceableParamGroup ref : referenceableParamGroupList) {
                    if (ref.getParamGroupName().equals(refValue)) {
                      vars.chromatogram.getCVParams().addAll(ref.getCVParams());
                      break;
                    }
                  }

                }

                if (tagsTracker.isInside(TAG_CHROMATOGRAM)
                    && tagsTracker.isInside(TAG_BINARY_DATA_ARRAY)
                    && openingTagName.contentEquals(TAG_CV_PARAM) && vars.binaryDataInfo != null) {
                  String accession = getRequiredAttribute(xmlStreamReader, "accession").toString();
                  if (vars.binaryDataInfo.isBitLengthAccession(accession)) {
                    vars.binaryDataInfo.setBitLength(accession);
                  } else if (vars.binaryDataInfo.isCompressionTypeAccession(accession)) {
                    vars.binaryDataInfo.setCompressionType(accession);
                  } else if (vars.binaryDataInfo.isArrayTypeAccession(accession)) {
                    vars.binaryDataInfo.setArrayType(accession);
                  } else {
                    // TODO Omitting warnings, can add later if required (Create a verbose mode for
                    // output maybe)

                    // logger.warn(
                    // "Found binary array other than m/z and intensity! Skipping chromatogram scan
                    // (Line"
                    // + xmlStreamReader.getLocation().getLineNumber() + ")");
                    vars.skipScan = true;
                  }

                }

                if (openingTagName.contentEquals(TAG_PRECURSOR)) {
                  final CharArray spectrumRef =
                      xmlStreamReader.getAttributeValue(null, "spectrumRef");
                  String spectrumRefString = spectrumRef == null ? null : spectrumRef.toString();
                  vars.precursor = new MzMLPrecursorElement(spectrumRefString);

                }

                if (openingTagName.contentEquals(TAG_PRODUCT)) {
                  vars.product = new MzMLProduct();

                }

                if (tagsTracker.isInside(TAG_PRECURSOR)) {
                  if (openingTagName.contentEquals(TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();

                  } else if (openingTagName.contentEquals(TAG_SELECTED_ION_LIST)) {
                    vars.selectedIonList = new MzMLPrecursorSelectedIonList();

                  } else if (openingTagName.contentEquals(TAG_ACTIVATION)) {
                    vars.activation = new MzMLPrecursorActivation();

                  } else if (tagsTracker.isInside(TAG_ISOLATION_WINDOW)) {
                    if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.isolationWindow.addCVParam(cvParam);
                    }

                  } else if (tagsTracker.isInside(TAG_SELECTED_ION_LIST)) {
                    if (openingTagName.contentEquals(TAG_SELECTED_ION)) {
                      vars.selectedIon = new MzMLPrecursorSelectedIon();
                    } else if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.selectedIon.addCVParam(cvParam);
                    }

                  } else if (tagsTracker.isInside(TAG_ACTIVATION)) {
                    if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.activation.addCVParam(cvParam);
                    }
                  }
                }

                if (tagsTracker.isInside(TAG_PRODUCT)) {
                  if (openingTagName.contentEquals(TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();

                  } else if (tagsTracker.isInside(TAG_ISOLATION_WINDOW)) {
                    if (openingTagName.contentEquals(TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.isolationWindow.addCVParam(cvParam);

                    }

                  }
                }

              }

              break;

            case XMLStreamConstants.END_ELEMENT:
              // closing tag
              final CharArray closingTagName = xmlStreamReader.getLocalName();
              tagsTracker.exitedTag(closingTagName.toString());

              switch (closingTagName.toString()) {
                case TAG_SPECTRUM_LIST:
                  break;
                case TAG_REF_PARAM_GROUP:
                  referenceableParamGroupList.add(vars.referenceableParamGroup);
                  break;
                case TAG_REF_PARAM_GROUP_LIST:
                  break;
                case TAG_PRECURSOR_LIST:
                  break;
                case TAG_ISOLATION_WINDOW:
                  if (tagsTracker.isInside(TAG_PRECURSOR)) {
                    vars.precursor.setIsolationWindow(vars.isolationWindow);
                  } else if (tagsTracker.isInside(TAG_PRODUCT)
                      && tagsTracker.isInside(TAG_CHROMATOGRAM)) {
                    vars.product.setIsolationWindow(vars.isolationWindow);
                  }
                  break;
                case TAG_SELECTED_ION_LIST:
                  vars.precursor.setSelectedIonList(vars.selectedIonList);
                  break;
                case TAG_ACTIVATION:
                  vars.precursor.setActivation(vars.activation);
                  break;
                case TAG_SELECTED_ION:
                  vars.selectedIonList.addSelectedIon(vars.selectedIon);
                  break;
                case TAG_PRECURSOR:
                  if (tagsTracker.isInside(TAG_SPECTRUM))
                    vars.spectrum.getPrecursorList().addPrecursor(vars.precursor);
                  else if (tagsTracker.isInside(TAG_CHROMATOGRAM))
                    vars.chromatogram.setPrecursor(vars.precursor);

                  break;
                case TAG_PRODUCT:
                  if (tagsTracker.isInside(TAG_CHROMATOGRAM))
                    vars.chromatogram.setProdcut(vars.product);
              }

              if (tagsTracker.isInside(TAG_SPECTRUM_LIST)) {
                switch (closingTagName.toString()) {
                  case TAG_BINARY_DATA_ARRAY:
                    if (!vars.skipScan) {
                      if ("MS:1000514".equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.spectrum.setMzBinaryDataInfo(vars.binaryDataInfo);
                      }
                      if ("MS:1000515".equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.spectrum.setIntensityBinaryDataInfo(vars.binaryDataInfo);
                      }
                    }
                    break;
                  case TAG_SPECTRUM:
                    if (!vars.skipScan)
                      spectrumList.add(vars.spectrum);
                    else
                      vars.skipScan = false;
                }

              }

              if (tagsTracker.isInside(TAG_CHROMATOGRAM_LIST)) {
                switch (closingTagName.toString()) {
                  case TAG_BINARY_DATA_ARRAY:
                    if (!vars.skipScan) {
                      if ("MS:1000595".equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.chromatogram.setRtBinaryDataInfo(vars.binaryDataInfo);
                      }
                      if ("MS:1000515".equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.chromatogram.setIntensityBinaryDataInfo(vars.binaryDataInfo);
                      }
                    }
                    break;
                  case TAG_CHROMATOGRAM:
                    if (!vars.skipScan)
                      chromatogramsList.add(vars.chromatogram);
                    else
                      vars.skipScan = false;
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
      logger.info("Parsing Complete");
    } catch (IOException | XMLStreamException e) {
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
   * getScanNumber.
   * </p>
   *
   * @param spectrumId a {@link java.lang.String} object.
   * @return a {@link java.lang.Integer} object.
   */
  public Optional<Integer> getScanNumber(String spectrumId) {
    final Pattern pattern = Pattern.compile("scan=([0-9]+)");
    final Matcher matcher = pattern.matcher(spectrumId);
    boolean scanNumberFound = matcher.find();

    // Some vendors include scan=XX in the ID, some don't, such as
    // mzML converted from WIFF files. See the definition of nativeID in
    // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
    // So, get the value of the index tag if the scanNumber is not present in the ID
    if (scanNumberFound) {
      Integer scanNumber = Integer.parseInt(matcher.group(1));
      return Optional.ofNullable(scanNumber);
    }

    return Optional.ofNullable(null);
  }

  /**
   * <p>
   * Gets the required attribute from xmlStreamReader, throws an exception of the attribute is not
   * found
   * </p>
   *
   * @param XMLStreamReader instance used to parse
   * @param Attribute's value to be found
   * @return a CharArray containing the value of the attribute.
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

  private class Vars {

    int defaultArrayLength;
    boolean skipScan;
    MzMLSpectrum spectrum;
    MzMLChromatogram chromatogram;
    MzMLBinaryDataInfo binaryDataInfo;
    MzMLReferenceableParamGroup referenceableParamGroup;
    MzMLPrecursorElement precursor;
    MzMLProduct product;
    MzMLIsolationWindow isolationWindow;
    MzMLPrecursorSelectedIonList selectedIonList;
    MzMLPrecursorSelectedIon selectedIon;
    MzMLPrecursorActivation activation;

    Vars() {
      defaultArrayLength = 0;
      skipScan = false;
      spectrum = null;
      chromatogram = null;
      binaryDataInfo = null;
      referenceableParamGroup = null;
      precursor = null;
      product = null;
      isolationWindow = null;
      selectedIonList = null;
      selectedIon = null;
      activation = null;
    }
  }
}
