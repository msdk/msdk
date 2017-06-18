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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.util.ByteBufferInputStreamAdapter;
import io.github.msdk.io.mzml2.util.MzMLFileMemoryMapper;
import it.unimi.dsi.io.ByteBufferInputStream;
import javolution.osgi.internal.OSGiServices;
import javolution.xml.stream.XMLInputFactory;
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
  private RawDataFile newRawFile;
  private Integer lastScanNumber = 0;
  private boolean canceled;
  private Float progress;

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
  public RawDataFile execute() throws MSDKException {

    try {
      MzMLFileMemoryMapper mapper = new MzMLFileMemoryMapper();
      ByteBufferInputStream is = mapper.mapToMemory(mzMLFile);

      List<Chromatogram> chromatogramsList = new ArrayList<>();
      List<MsFunction> msFunctionsList = new ArrayList<>();

      // Create the MzMLRawDataFile object
      final MzMLRawDataFile newRawFile =
          new MzMLRawDataFile(mzMLFile, msFunctionsList, spectrumList, chromatogramsList);
      this.newRawFile = newRawFile;

      XMLInputFactory xmlInputFactory = OSGiServices.getXMLInputFactory();
      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);

      boolean insideSpectrumListFlag = false;
      boolean insideBinaryDataArrayFlag = false;
      boolean insideReferenceableParamGroupList = false;
      int defaultArrayLength = 0;
      MzMLSpectrum spectrum = null;
      MzMLBinaryDataInfo binaryDataInfo = null;
      MzMLReferenceableParamGroup referenceableParamGroup = null;
      while (xmlStreamReader.hasNext()) {
        if (canceled) {
          xmlStreamReader.close();
          return null;
        }
        xmlStreamReader.next();
        if (xmlStreamReader.isStartElement()) {
          if (xmlStreamReader.hasNext()) {
            if (xmlStreamReader.getLocalName().contentEquals("spectrumList"))
              insideSpectrumListFlag = true;
            if (xmlStreamReader.getLocalName().contentEquals("referenceableParamGroupList"))
              insideReferenceableParamGroupList = true;

            if (insideReferenceableParamGroupList) {
              if (xmlStreamReader.getLocalName().contentEquals("referenceableParamGroup")) {
                String id = xmlStreamReader.getAttributeValue(null, "id").toString();
                referenceableParamGroup = new MzMLReferenceableParamGroup(id);
              }
              if (xmlStreamReader.getLocalName().contentEquals("cvParam")) {
                String accession = xmlStreamReader.getAttributeValue(null, "accession").toString();
                CharSequence value = xmlStreamReader.getAttributeValue(null, "value");
                CharSequence unitAccession =
                    xmlStreamReader.getAttributeValue(null, "unitAccession");
                MzMLCVParam cvParam = new MzMLCVParam(accession, null, null);
                if (value != null)
                  cvParam.setValue(value.toString());
                if (unitAccession != null)
                  cvParam.setUnitAccession(unitAccession.toString());
                referenceableParamGroup.addReferenceableCvParam(cvParam);
              }
            }

            if (insideBinaryDataArrayFlag && xmlStreamReader.getLocalName().equals("cvParam")
                && binaryDataInfo != null) {
              String accession = xmlStreamReader.getAttributeValue(null, "accession").toString();
              if (binaryDataInfo.isBitLengthAccession(accession)) {
                binaryDataInfo.setBitLength(accession);
              } else if (binaryDataInfo.isCompressionTypeAccession(accession)) {
                binaryDataInfo.setCompressionType(accession);
              } else if (binaryDataInfo.isArrayTypeAccession(accession)) {
                binaryDataInfo.setArrayType(accession);
              } else {
                break; // A better approach to skip UV Scans would
                       // be to only break accession which define
                       // the array type and isn't either m/z or
                       // intensity values. We would have to list
                       // out all array types in that case.
              }
            }

            if (insideSpectrumListFlag) {
              if (xmlStreamReader.getLocalName().contentEquals("spectrum")) {
                spectrum = new MzMLSpectrum(newRawFile);
                String id = xmlStreamReader.getAttributeValue(null, "id").toString();
                defaultArrayLength =
                    xmlStreamReader.getAttributeValue(null, "defaultArrayLength").toInt();
                spectrum.setId(id);
                spectrum.setScanNumber(getScanNumber(id));
                spectrum.setByteBufferInputStream(is);
              }
              if (xmlStreamReader.getLocalName().contentEquals("binaryDataArray")) {
                insideBinaryDataArrayFlag = true;
                binaryDataInfo = new MzMLBinaryDataInfo();
                int encodedLength =
                    xmlStreamReader.getAttributeValue(null, "encodedLength").toInt();
                CharSequence arrayLength = xmlStreamReader.getAttributeValue(null, "arrayLength");
                binaryDataInfo.setEncodedLength(encodedLength);
                if (arrayLength != null) {
                  defaultArrayLength = Integer.valueOf(arrayLength.toString());
                }
                binaryDataInfo.setArrayLength(defaultArrayLength);

              }
              if (xmlStreamReader.getLocalName().contentEquals("cvParam")) {
                if (!insideBinaryDataArrayFlag && spectrum != null) {
                  String accession =
                      xmlStreamReader.getAttributeValue(null, "accession").toString();
                  CharSequence value = xmlStreamReader.getAttributeValue(null, "value");
                  CharSequence unitAccession =
                      xmlStreamReader.getAttributeValue(null, "unitAccession");
                  MzMLCVParam cvParam = new MzMLCVParam(accession, null, null);
                  if (value != null)
                    cvParam.setValue(value.toString());
                  if (unitAccession != null)
                    cvParam.setUnitAccession(unitAccession.toString());
                  spectrum.getCVParams().add(cvParam);
                }
              }
              if (xmlStreamReader.getLocalName().contentEquals("binary")) {
                if (spectrum != null) {
                  binaryDataInfo.setPosition(xmlStreamReader.getLocation().getCharacterOffset());
                  // ByteBufferInputStreamAdapter decodedIs = new ByteBufferInputStreamAdapter(
                  // is.copy(), binaryDataInfo.getPosition(), binaryDataInfo.getEncodedLength());
                  // System.out.println(new String(IOUtils.toByteArray(decodedIs)));
                }
              }
              if (xmlStreamReader.getLocalName().contentEquals("referenceableParamGroupRef")) {
                String refValue = xmlStreamReader.getAttributeValue(null, "ref").toString();
                for (MzMLReferenceableParamGroup ref : referenceableParamGroupList) {
                  if (ref.getParamGroupName().equals(refValue)) {
                    spectrum.getCVParams().addAll(ref.getReferenceableCvParams());
                    break;
                  }
                }
              }
            }
          }
        }

        if (xmlStreamReader.isEndElement()) {
          if (xmlStreamReader.getLocalName().contentEquals("spectrumList"))
            insideSpectrumListFlag = false;

          if (xmlStreamReader.getLocalName().contentEquals("referenceableParamGroup"))
            referenceableParamGroupList.add(referenceableParamGroup);

          if (xmlStreamReader.getLocalName().contentEquals("referenceableParamGroupList"))
            insideReferenceableParamGroupList = false;

          if (insideSpectrumListFlag) {
            if (xmlStreamReader.getLocalName().contentEquals("binaryDataArray")) {
              if (binaryDataInfo.getArrayType().getValue().equals("MS:1000514"))
                spectrum.setMzBinaryDataInfo(binaryDataInfo);
              if (binaryDataInfo.getArrayType().getValue().equals("MS:1000515"))
                spectrum.setIntensityBinaryDataInfo(binaryDataInfo);
              insideBinaryDataArrayFlag = false;
            }
            if (xmlStreamReader.getLocalName().contentEquals("spectrum"))
              spectrumList.add(spectrum);
          }
        }
      }

      xmlStreamReader.close();
    } catch (Exception e) {
      throw (new MSDKException(e));
    }

    progress = 1f;
    return newRawFile;
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
}
