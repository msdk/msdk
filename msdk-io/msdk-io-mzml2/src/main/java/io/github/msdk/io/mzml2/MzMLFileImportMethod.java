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
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo;
import io.github.msdk.io.mzml2.data.MzMLCV;
import io.github.msdk.io.mzml2.data.MzMLCVParam;
import io.github.msdk.io.mzml2.data.MzMLCompressionType;
import io.github.msdk.io.mzml2.data.MzMLIsolationWindow;
import io.github.msdk.io.mzml2.data.MzMLPrecursorActivation;
import io.github.msdk.io.mzml2.data.MzMLPrecursorElement;
import io.github.msdk.io.mzml2.data.MzMLPrecursorSelectedIon;
import io.github.msdk.io.mzml2.data.MzMLPrecursorSelectedIonList;
import io.github.msdk.io.mzml2.data.MzMLProduct;
import io.github.msdk.io.mzml2.data.MzMLReferenceableParamGroup;
import io.github.msdk.io.mzml2.data.MzMLScan;
import io.github.msdk.io.mzml2.data.MzMLScanWindow;
import io.github.msdk.io.mzml2.data.MzMLScanWindowList;
import io.github.msdk.io.mzml2.util.MzMLFileMemoryMapper;
import io.github.msdk.io.mzml2.util.MzMLTags;
import io.github.msdk.io.mzml2.util.TagTracker;
import io.github.msdk.io.mzml2.util.bytebufferinputstream.ByteBufferInputStream;
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
public class MzMLFileImportMethod implements MSDKMethod<RawDataFile> {
  private final @Nonnull File mzMLFile;
  private final ArrayList<MzMLReferenceableParamGroup> referenceableParamGroupList;
  private MzMLRawDataFile newRawFile;
  private volatile boolean canceled;
  private Float progress;
  private int lastLoggedProgress;
  private TagTracker tracker;
  Logger logger;

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.lang.String} object.
   */
  public MzMLFileImportMethod(String mzMLFilePath) {
    this(new File(mzMLFilePath));
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.nio.file.Path} object.
   */
  public MzMLFileImportMethod(Path mzMLFilePath) {
    this(mzMLFilePath.toFile());
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFile a {@link java.io.File} object.
   */
  public MzMLFileImportMethod(File mzMLFile) {
    this.mzMLFile = mzMLFile;
    this.referenceableParamGroupList = new ArrayList<>();
    this.canceled = false;
    this.progress = 0f;
    this.lastLoggedProgress = 0;
    this.logger = LoggerFactory.getLogger(this.getClass());
    this.tracker = new TagTracker();
  }

  /**
   * <p>
   * execute.
   * </p>
   *
   * @return a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object. @throws
   *         io.github.msdk.MSDKException if any. @throws
   */
  @Override
  public MzMLRawDataFile execute() throws MSDKException {

    try {
      logger.info("Began parsing file: " + mzMLFile.getAbsolutePath());
      ByteBufferInputStream is = MzMLFileMemoryMapper.mapToMemory(mzMLFile);

      List<MsScan> spectrumList = new ArrayList<>();
      List<Chromatogram> chromatogramsList = new ArrayList<>();

      // TODO populate the list
      List<String> msFunctionsList = new ArrayList<>();

      // Create the MzMLRawDataFile object
      final MzMLRawDataFile newRawFile =
          new MzMLRawDataFile(mzMLFile, msFunctionsList, spectrumList, chromatogramsList);
      this.newRawFile = newRawFile;

      // It's ok to directly create this particular reader, this class is `public final`
      // and we precisely want that fast UFT-8 reader implementation
      final XMLStreamReaderImpl xmlStreamReader = new XMLStreamReaderImpl();
      xmlStreamReader.setInput(is, "UTF-8");

      Vars vars = new Vars();
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

              tracker.enter(openingTagName);

              if (tracker.inside(MzMLTags.TAG_REF_PARAM_GROUP_LIST)) {

                if (openingTagName.contentEquals(MzMLTags.TAG_REF_PARAM_GROUP)) {
                  final CharArray id = getRequiredAttribute(xmlStreamReader, "id");
                  vars.referenceableParamGroup = new MzMLReferenceableParamGroup(id.toString());

                } else if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                  MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                  vars.referenceableParamGroup.addCVParam(cvParam);

                }
                continue;
              }

              if (tracker.inside(MzMLTags.TAG_SPECTRUM_LIST)) {
                if (openingTagName.contentEquals(MzMLTags.TAG_SPECTRUM)) {
                  String id = getRequiredAttribute(xmlStreamReader, "id").toString();
                  Integer index = getRequiredAttribute(xmlStreamReader, "index").toInt();
                  vars.defaultArrayLength =
                      getRequiredAttribute(xmlStreamReader, "defaultArrayLength").toInt();
                  Integer scanNumber = getScanNumber(id).orElse(index + 1);
                  vars.spectrum =
                      new MzMLSpectrum(newRawFile, is, id, scanNumber, vars.defaultArrayLength);


                } else if (openingTagName.contentEquals(MzMLTags.TAG_BINARY_DATA_ARRAY)) {
                  vars.skipBinaryDataArray = false;
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


                } else if (openingTagName.contentEquals(MzMLTags.TAG_SCAN)) {
                  vars.scan = new MzMLScan();

                } else if (openingTagName.contentEquals(MzMLTags.TAG_SCAN_WINDOW_LIST)) {
                  vars.scanWindowList = new MzMLScanWindowList();

                } else if (openingTagName.contentEquals(MzMLTags.TAG_SCAN_WINDOW)) {
                  vars.scanWindow = new MzMLScanWindow();

                } else if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                  if (!tracker.inside(MzMLTags.TAG_BINARY_DATA_ARRAY_LIST)
                      && !tracker.inside(MzMLTags.TAG_PRODUCT_LIST)
                      && !tracker.inside(MzMLTags.TAG_PRECURSOR_LIST)
                      && !tracker.inside(MzMLTags.TAG_SCAN_LIST) && vars.spectrum != null) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    vars.spectrum.getCVParams().addCVParam(cvParam);;
                  } else if (tracker.inside(MzMLTags.TAG_SCAN_LIST)) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    if (!tracker.inside(MzMLTags.TAG_SCAN_WINDOW))
                      if (!tracker.inside(MzMLTags.TAG_SCAN))
                        vars.spectrum.getScanList().addCVParam(cvParam);
                      else
                        vars.scan.addCVParam(cvParam);
                    else
                      vars.scanWindow.addCVParam(cvParam);

                  } else if (tracker.inside(MzMLTags.TAG_SPECTRUM)
                      && tracker.inside(MzMLTags.TAG_BINARY_DATA_ARRAY)
                      && !vars.skipBinaryDataArray) {
                    String accession =
                        getRequiredAttribute(xmlStreamReader, "accession").toString();
                    if (vars.binaryDataInfo.isBitLengthAccession(accession)) {
                      vars.binaryDataInfo.setBitLength(accession);
                    } else if (vars.binaryDataInfo.isCompressionTypeAccession(accession)) {
                      manageCompression(vars.binaryDataInfo, accession);
                    } else if (vars.binaryDataInfo.isArrayTypeAccession(accession)) {
                      vars.binaryDataInfo.setArrayType(accession);
                    } else {
                      vars.skipBinaryDataArray = true;
                    }

                  }


                } else if (openingTagName.contentEquals(MzMLTags.TAG_BINARY)) {
                  if (vars.spectrum != null && !vars.skipBinaryDataArray) {
                    int bomOffset = xmlStreamReader.getLocation().getBomLength();
                    // TODO Fetch long value from getCharacterOffset()
                    vars.binaryDataInfo.setPosition(
                        xmlStreamReader.getLocation().getCharacterOffset() + bomOffset);
                  }


                } else if (openingTagName.contentEquals(MzMLTags.TAG_REF_PARAM_GROUP_REF)) {
                  String refValue = getRequiredAttribute(xmlStreamReader, "ref").toString();
                  for (MzMLReferenceableParamGroup ref : referenceableParamGroupList) {
                    if (ref.getParamGroupName().equals(refValue)) {
                      vars.spectrum.getCVParams().getCVParamsList().addAll(ref.getCVParamsList());
                      break;
                    }
                  }

                }

                if (tracker.inside(MzMLTags.TAG_PRECURSOR_LIST)) {

                  if (openingTagName.contentEquals(MzMLTags.TAG_PRECURSOR)) {
                    final CharArray spectrumRef =
                        xmlStreamReader.getAttributeValue(null, MzMLTags.ATTR_SPECTRUM_REF);
                    String spectrumRefString = spectrumRef == null ? null : spectrumRef.toString();
                    vars.precursor = new MzMLPrecursorElement(spectrumRefString);

                  } else if (openingTagName.contentEquals(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();

                  } else if (openingTagName.contentEquals(MzMLTags.TAG_SELECTED_ION_LIST)) {
                    vars.selectedIonList = new MzMLPrecursorSelectedIonList();

                  } else if (openingTagName.contentEquals(MzMLTags.TAG_ACTIVATION)) {
                    vars.activation = new MzMLPrecursorActivation();

                  } else if (tracker.inside(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.isolationWindow.addCVParam(cvParam);
                    }

                  } else if (tracker.inside(MzMLTags.TAG_SELECTED_ION_LIST)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_SELECTED_ION)) {
                      vars.selectedIon = new MzMLPrecursorSelectedIon();
                    } else if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.selectedIon.addCVParam(cvParam);
                    }

                  } else if (tracker.inside(MzMLTags.TAG_ACTIVATION)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.activation.addCVParam(cvParam);
                    }
                  }
                }

                if (tracker.inside(MzMLTags.TAG_PRODUCT_LIST)) {
                  if (openingTagName.contentEquals(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();

                  } else if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    vars.isolationWindow.addCVParam(cvParam);
                  }

                }

              } else if (tracker.inside(MzMLTags.TAG_CHROMATOGRAM_LIST)) {
                if (openingTagName.contentEquals(MzMLTags.TAG_CHROMATOGRAM)) {
                  String chromatogramId = getRequiredAttribute(xmlStreamReader, "id").toString();
                  Integer chromatogramNumber =
                      getRequiredAttribute(xmlStreamReader, "index").toInt() + 1;
                  vars.defaultArrayLength =
                      getRequiredAttribute(xmlStreamReader, "defaultArrayLength").toInt();
                  vars.chromatogram = new MzMLChromatogram(newRawFile, is, chromatogramId,
                      chromatogramNumber, vars.defaultArrayLength);

                } else if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                  if (!tracker.inside(MzMLTags.TAG_BINARY_DATA_ARRAY)
                      && !tracker.inside(MzMLTags.TAG_PRECURSOR)
                      && !tracker.inside(MzMLTags.TAG_PRODUCT) && vars.chromatogram != null) {
                    MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                    vars.chromatogram.getCVParams().add(cvParam);
                  }

                } else if (openingTagName.contentEquals(MzMLTags.TAG_BINARY_DATA_ARRAY)) {
                  vars.skipBinaryDataArray = false;
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

                } else if (openingTagName.contentEquals(MzMLTags.TAG_BINARY)) {
                  if (vars.chromatogram != null && !vars.skipBinaryDataArray) {
                    int bomOffset = xmlStreamReader.getLocation().getBomLength();
                    // TODO Fetch long value from getCharacterOffset()
                    vars.binaryDataInfo.setPosition(
                        xmlStreamReader.getLocation().getCharacterOffset() + bomOffset);
                  }

                } else if (openingTagName.contentEquals(MzMLTags.TAG_REF_PARAM_GROUP_REF)) {
                  String refValue = xmlStreamReader.getAttributeValue(null, "ref").toString();
                  for (MzMLReferenceableParamGroup ref : referenceableParamGroupList) {
                    if (ref.getParamGroupName().equals(refValue)) {
                      vars.chromatogram.getCVParams().addAll(ref.getCVParamsList());
                      break;
                    }
                  }

                }

                if (tracker.inside(MzMLTags.TAG_CHROMATOGRAM)
                    && tracker.inside(MzMLTags.TAG_BINARY_DATA_ARRAY)
                    && openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)
                    && vars.binaryDataInfo != null && !vars.skipBinaryDataArray) {
                  String accession = getRequiredAttribute(xmlStreamReader, "accession").toString();
                  if (vars.binaryDataInfo.isBitLengthAccession(accession)) {
                    vars.binaryDataInfo.setBitLength(accession);
                  } else if (vars.binaryDataInfo.isCompressionTypeAccession(accession)) {
                    manageCompression(vars.binaryDataInfo, accession);
                  } else if (vars.binaryDataInfo.isArrayTypeAccession(accession)) {
                    vars.binaryDataInfo.setArrayType(accession);
                  } else {
                    vars.skipBinaryDataArray = true;
                  }

                }

                if (openingTagName.contentEquals(MzMLTags.TAG_PRECURSOR)) {
                  final CharArray spectrumRef =
                      xmlStreamReader.getAttributeValue(null, "spectrumRef");
                  String spectrumRefString = spectrumRef == null ? null : spectrumRef.toString();
                  vars.precursor = new MzMLPrecursorElement(spectrumRefString);

                } else if (openingTagName.contentEquals(MzMLTags.TAG_PRODUCT)) {
                  vars.product = new MzMLProduct();

                } else if (tracker.inside(MzMLTags.TAG_PRECURSOR)) {
                  if (openingTagName.contentEquals(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();
                    vars.selectedIonList = new MzMLPrecursorSelectedIonList();

                  } else if (openingTagName.contentEquals(MzMLTags.TAG_ACTIVATION)) {
                    vars.activation = new MzMLPrecursorActivation();

                  } else if (tracker.inside(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.isolationWindow.addCVParam(cvParam);
                    }

                  } else if (tracker.inside(MzMLTags.TAG_SELECTED_ION_LIST)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_SELECTED_ION)) {
                      vars.selectedIon = new MzMLPrecursorSelectedIon();
                    } else if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.selectedIon.addCVParam(cvParam);
                    }

                  } else if (tracker.inside(MzMLTags.TAG_ACTIVATION)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
                      MzMLCVParam cvParam = createMzMLCVParam(xmlStreamReader);
                      vars.activation.addCVParam(cvParam);
                    }
                  }
                } else if (tracker.inside(MzMLTags.TAG_PRODUCT)) {
                  if (openingTagName.contentEquals(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    vars.isolationWindow = new MzMLIsolationWindow();

                  } else if (tracker.inside(MzMLTags.TAG_ISOLATION_WINDOW)) {
                    if (openingTagName.contentEquals(MzMLTags.TAG_CV_PARAM)) {
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

              tracker.exit(closingTagName);

              CharArray s = closingTagName;
              if (s.equals(MzMLTags.TAG_SPECTRUM_LIST)) {
              } else if (s.equals(MzMLTags.TAG_REF_PARAM_GROUP)) {
                referenceableParamGroupList.add(vars.referenceableParamGroup);

              } else if (s.equals(MzMLTags.TAG_ISOLATION_WINDOW)) {
                if (tracker.inside(MzMLTags.TAG_PRECURSOR)) {
                  vars.precursor.setIsolationWindow(vars.isolationWindow);
                } else if (tracker.inside(MzMLTags.TAG_PRODUCT)) {
                  vars.product.setIsolationWindow(vars.isolationWindow);
                }

              } else if (s.equals(MzMLTags.TAG_PRODUCT)) {
                if (tracker.inside(MzMLTags.TAG_SPECTRUM))
                  vars.spectrum.getProductList().addProduct(vars.product);
                else if (tracker.inside(MzMLTags.TAG_CHROMATOGRAM))
                  vars.chromatogram.setProdcut(vars.product);

              } else if (s.equals(MzMLTags.TAG_SELECTED_ION_LIST)) {
                vars.precursor.setSelectedIonList(vars.selectedIonList);

              } else if (s.equals(MzMLTags.TAG_ACTIVATION)) {
                vars.precursor.setActivation(vars.activation);

              } else if (s.equals(MzMLTags.TAG_SELECTED_ION)) {
                vars.selectedIonList.addSelectedIon(vars.selectedIon);

              } else if (s.equals(MzMLTags.TAG_PRECURSOR)) {
                if (tracker.inside(MzMLTags.TAG_SPECTRUM))
                  vars.spectrum.getPrecursorList().addPrecursor(vars.precursor);
                else if (tracker.inside(MzMLTags.TAG_CHROMATOGRAM))
                  vars.chromatogram.setPrecursor(vars.precursor);

              } else if (s.equals(MzMLTags.TAG_SCAN_WINDOW)) {
                vars.scanWindowList.addScanWindow(vars.scanWindow);

              } else if (s.equals(MzMLTags.TAG_SCAN_WINDOW_LIST)) {
                if (tracker.inside(MzMLTags.TAG_SPECTRUM))
                  vars.scan.setScanWindowList(vars.scanWindowList);

              } else if (s.equals(MzMLTags.TAG_SCAN)) {
                if (tracker.inside(MzMLTags.TAG_SPECTRUM))
                  vars.spectrum.getScanList().addScan(vars.scan);

              }

              if (tracker.inside(MzMLTags.TAG_SPECTRUM_LIST)) {
                switch (closingTagName.toString()) {
                  case MzMLTags.TAG_BINARY_DATA_ARRAY:
                    if (!vars.skipBinaryDataArray) {
                      if (MzMLCV.cvMzArray.equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.spectrum.setMzBinaryDataInfo(vars.binaryDataInfo);
                      }
                      if (MzMLCV.cvIntensityArray
                          .equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.spectrum.setIntensityBinaryDataInfo(vars.binaryDataInfo);
                      }
                    }
                    break;
                  case MzMLTags.TAG_SPECTRUM:
                    if (vars.spectrum.getMzBinaryDataInfo() != null
                        && vars.spectrum.getIntensityBinaryDataInfo() != null)
                      spectrumList.add(vars.spectrum);
                    else {
                      // logger.warn("Didn't find m/z or intensity data array for spectrum scan
                      // (#"
                      // + vars.spectrum.getScanNumber() + "). Skipping scan.");
                    }
                    break;

                  default:
                    // we don't care about other tags
                    break;
                }

              }

              if (tracker.inside(MzMLTags.TAG_CHROMATOGRAM_LIST)) {
                switch (closingTagName.toString()) {
                  case MzMLTags.TAG_BINARY_DATA_ARRAY:
                    if (!vars.skipBinaryDataArray) {
                      if (MzMLCV.cvRetentionTimeArray
                          .equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.chromatogram.setRtBinaryDataInfo(vars.binaryDataInfo);
                      }
                      if (MzMLCV.cvIntensityArray
                          .equals(vars.binaryDataInfo.getArrayType().getValue())) {
                        vars.chromatogram.setIntensityBinaryDataInfo(vars.binaryDataInfo);
                      }
                    }
                    break;
                  case MzMLTags.TAG_CHROMATOGRAM:
                    if (vars.chromatogram.getRtBinaryDataInfo() != null
                        && vars.chromatogram.getIntensityBinaryDataInfo() != null)
                      chromatogramsList.add(vars.chromatogram);
                    else {
                      // logger.warn("Didn't find rt or intensity data array for spectrum scan (#"
                      // + vars.spectrum.getScanNumber() + "). Skipping scan.");
                    }
                    break;

                  default:
                    // we don't care about other tags
                    break;
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
    CharArray accession = xmlStreamReader.getAttributeValue(null, MzMLTags.ATTR_ACCESSION);
    CharArray value = xmlStreamReader.getAttributeValue(null, MzMLTags.ATTR_VALUE);
    CharArray name = xmlStreamReader.getAttributeValue(null, MzMLTags.ATTR_NAME);
    CharArray unitAccession = xmlStreamReader.getAttributeValue(null, MzMLTags.ATTR_UNIT_ACCESSION);

    // accession is a required attribute
    if (accession == null) {
      throw new IllegalStateException("Any cvParam must have an accession.");
    }

    // these attributes are optional
    String valueStr = value == null ? null : value.toString();
    String nameStr = name == null ? null : name.toString();
    String unitAccessionStr = unitAccession == null ? null : unitAccession.toString();

    return new MzMLCVParam(accession.toString(), valueStr, nameStr, unitAccessionStr);
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
   * @param xmlStreamReader XMLStreamReader instance used to parse
   * @param attr Attribute's value to be found
   * @return a CharArray containing the value of the attribute.
   */
  public CharArray getRequiredAttribute(XMLStreamReader xmlStreamReader, String attr) {
    CharArray attrValue = xmlStreamReader.getAttributeValue(null, attr);
    if (attrValue == null)
      throw new IllegalStateException("Tag " + xmlStreamReader.getLocalName() + " must provide an `"
          + attr + "`attribute (Line " + xmlStreamReader.getLocation().getLineNumber() + ")");
    return attrValue;
  }

  public void manageCompression(MzMLBinaryDataInfo binaryInfo, String accession) {
    if (binaryInfo.getCompressionType() == MzMLCompressionType.NO_COMPRESSION)
      binaryInfo.setCompressionType(accession);
    else {
      if (binaryInfo.getCompressionType(accession) == MzMLCompressionType.ZLIB) {
        switch (binaryInfo.getCompressionType()) {
          case NUMPRESS_LINPRED:
            binaryInfo.setCompressionType(MzMLCompressionType.NUMPRESS_LINPRED_ZLIB);
            break;
          case NUMPRESS_POSINT:
            binaryInfo.setCompressionType(MzMLCompressionType.NUMPRESS_POSINT_ZLIB);
            break;
          case NUMPRESS_SHLOGF:
            binaryInfo.setCompressionType(MzMLCompressionType.NUMPRESS_SHLOGF_ZLIB);
            break;
          default:
            break;
        }
      } else {
        switch (binaryInfo.getCompressionType(accession)) {
          case NUMPRESS_LINPRED:
            binaryInfo.setCompressionType(MzMLCompressionType.NUMPRESS_LINPRED_ZLIB);
            break;
          case NUMPRESS_POSINT:
            binaryInfo.setCompressionType(MzMLCompressionType.NUMPRESS_POSINT_ZLIB);
            break;
          case NUMPRESS_SHLOGF:
            binaryInfo.setCompressionType(MzMLCompressionType.NUMPRESS_SHLOGF_ZLIB);
            break;
          default:
            break;
        }
      }
    }
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

  private static class Vars {

    int defaultArrayLength;
    boolean skipBinaryDataArray;
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
    MzMLScan scan;
    MzMLScanWindowList scanWindowList;
    MzMLScanWindow scanWindow;

    Vars() {
      defaultArrayLength = 0;
      skipBinaryDataArray = false;
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
      scan = null;
      scanWindowList = null;
      scanWindow = null;
    }
  }
}
