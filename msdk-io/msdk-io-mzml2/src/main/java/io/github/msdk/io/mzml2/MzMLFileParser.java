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

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.rawdata.RawDataFile;

public class MzMLFileParser implements MSDKMethod<RawDataFile> {
  private final @Nonnull File mzMLFile;
  private final @Nonnull ArrayList<MzMLSpectrum> spectrumList;

  public MzMLFileParser(String mzMLFilePath) {
    this(new File(mzMLFilePath));
  }

  public MzMLFileParser(Path mzMLFilePath) {
    this(mzMLFilePath.toFile());
  }

  public MzMLFileParser(File mzMLFile) {
    this.mzMLFile = mzMLFile;
    this.spectrumList = new ArrayList<>();
  }

  public MzMLRawDataFile execute() throws MSDKException {

    try {
      MzMLFileMemoryMapper mapper = new MzMLFileMemoryMapper();
      MappedByteBufferInputStream is = mapper.mapToMemory(mzMLFile);

      XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
      XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);

      boolean insideSpectrumListFlag = false;
      boolean insideBinaryDataArrayFlag = false;
      int defaultArrayLength = 0;
      MzMLSpectrum spectrum = null;
      MzMLBinaryDataInfo binaryDataInfo = null;
      while (xmlEventReader.hasNext()) {
        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        if (xmlEvent.isStartElement()) {
          StartElement startElement = xmlEvent.asStartElement();

          if (startElement.getName().getLocalPart().equals("spectrumList")
              && xmlEventReader.hasNext()) {
            xmlEvent = xmlEventReader.nextEvent();
            insideSpectrumListFlag = true;
          }

          if (insideSpectrumListFlag && xmlEventReader.hasNext()) {
            switch (startElement.getName().getLocalPart()) {
              case "spectrum":
                xmlEvent = xmlEventReader.nextEvent();
                if (spectrum != null)
                  spectrumList.add(spectrum);
                spectrum = new MzMLSpectrum();
                Attribute arrayLengthAttr =
                    startElement.getAttributeByName(new QName("defaultArrayLength"));
                defaultArrayLength = Integer.valueOf(arrayLengthAttr.getValue());
                spectrum.setMappedByteBufferInputStream(is);
                break;
              case "binaryDataArray":
                xmlEvent = xmlEventReader.nextEvent();
                insideBinaryDataArrayFlag = true;
                binaryDataInfo = new MzMLBinaryDataInfo();
                Attribute encodedLengthAttr =
                    startElement.getAttributeByName(new QName("encodedLength"));
                binaryDataInfo.setEncodedLength(Integer.valueOf(encodedLengthAttr.getValue()));
                Attribute arrayLengthAttr2 =
                    startElement.getAttributeByName(new QName("arrayLength"));
                if (arrayLengthAttr2 != null) {
                  defaultArrayLength = Integer.valueOf(arrayLengthAttr2.getValue());
                }
                binaryDataInfo.setArrayLength(defaultArrayLength);
                break;
              case "cvParam":
                if (!insideBinaryDataArrayFlag && spectrum != null) {
                  xmlEvent = xmlEventReader.nextEvent();
                  Attribute accessionAttr = startElement.getAttributeByName(new QName("accession"));
                  Attribute valueAttr = startElement.getAttributeByName(new QName("value"));
                  spectrum.add(accessionAttr.getValue(), valueAttr.getValue());
                }
                break;
              case "binary":
                if (spectrum != null) {
                  while (xmlEventReader.hasNext()) {
                    xmlEvent = xmlEventReader.nextEvent();
                    if (xmlEvent.isCharacters()) {
                      binaryDataInfo.setPosition(xmlEvent.getLocation().getCharacterOffset());
                      break;
                    }
                  }
                }
                break;
            }
          }



          if (insideBinaryDataArrayFlag && startElement.getName().getLocalPart().equals("cvParam")
              && binaryDataInfo != null && xmlEventReader.hasNext()) {
            xmlEvent = xmlEventReader.nextEvent();
            Attribute accessionAttr = startElement.getAttributeByName(new QName("accession"));
            if (binaryDataInfo.isBitLengthAccession(accessionAttr.getValue())) {
              binaryDataInfo.setBitLength(accessionAttr.getValue());
            } else if (binaryDataInfo.isCompressionTypeAccession(accessionAttr.getValue())) {
              binaryDataInfo.setCompressionType(accessionAttr.getValue());
            } else if (binaryDataInfo.isArrayTypeAccession(accessionAttr.getValue())) {
              binaryDataInfo.setArrayType(accessionAttr.getValue());
            } else {
              break; // A better approach to skip UV Scans would
                     // be to only break accession which define
                     // the array type and isn't either m/z or
                     // intensity values. We would have to list
                     // out all array types in that case.
            }
          }

        }

        if (xmlEvent.isEndElement()) {
          EndElement endElement = xmlEvent.asEndElement();
          if (endElement.getName().getLocalPart().equals("spectrumList")
              && xmlEventReader.hasNext()) {
            xmlEvent = xmlEventReader.nextEvent();
            insideSpectrumListFlag = false;
          }
        }

        if (insideSpectrumListFlag && xmlEvent.isEndElement()) {
          EndElement endElement = xmlEvent.asEndElement();
          if (endElement.getName().getLocalPart().equals("binaryDataArray")
              && xmlEventReader.hasNext()) {
            xmlEvent = xmlEventReader.nextEvent();
            if (binaryDataInfo.getArrayType().getValue().equals("MS:1000514"))
              spectrum.setMzBinaryDataInfo(binaryDataInfo);
            if (binaryDataInfo.getArrayType().getValue().equals("MS:1000515"))
              spectrum.setIntensityBinaryDataInfo(binaryDataInfo);
            insideBinaryDataArrayFlag = false;
          }
        }

      }
    } catch (IOException e) {
      throw (new MSDKException(e));
    } catch (XMLStreamException e) {
      throw (new MSDKException(e));
    }

    spectrumList.get(0).getIntensityValues();

    return null;
  }

  @Override
  public Float getFinishedPercentage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RawDataFile getResult() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void cancel() {
    // TODO Auto-generated method stub

  }
}
