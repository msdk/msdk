/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
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
			MzMLSpectrum spectrum = null;
			MzMLBinaryDataInfo binaryDataInfo = null;
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("spectrumList") && xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						insideSpectrumListFlag = true;
					}
					if (insideSpectrumListFlag && startElement.getName().getLocalPart().equals("binaryDataArray")
							&& xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						insideBinaryDataArrayFlag = true;
						binaryDataInfo = new MzMLBinaryDataInfo();
					}
					if (insideSpectrumListFlag && startElement.getName().getLocalPart().equals("spectrum")
							&& xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						if (spectrum != null)
							spectrumList.add(spectrum);
						spectrum = new MzMLSpectrum();
					}
					if (insideBinaryDataArrayFlag && !insideSpectrumListFlag
							&& startElement.getName().getLocalPart().equals("cvParam") && spectrum != null
							&& xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						Attribute accessionAttr = startElement.getAttributeByName(new QName("accession"));
						Attribute valueAttr = startElement.getAttributeByName(new QName("value"));
						spectrum.add(accessionAttr.getValue(), valueAttr.getValue());
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
									// be to only break accession defines the
									// array type and isn't either m/z or
									// intensity values. We would have to list
									// out all array types in that case.
						}
					}
					if (insideSpectrumListFlag && startElement.getName().getLocalPart().equals("binary")
							&& spectrum != null && xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						binaryDataInfo.setPosition(is.getCurrentPosition());
					}
				}
				if (xmlEvent.isEndElement()) {
					EndElement endElement = xmlEvent.asEndElement();
					if (endElement.getName().getLocalPart().equals("spectrumList") && xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						insideSpectrumListFlag = false;
					}
				}
				if (insideSpectrumListFlag && xmlEvent.isEndElement()) {
					EndElement endElement = xmlEvent.asEndElement();
					if (endElement.getName().getLocalPart().equals("binaryDataArray") && xmlEventReader.hasNext()) {
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
