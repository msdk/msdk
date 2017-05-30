/* 
 * (C) Copyright 2015-2017 by MSDK Development Team
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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

public class MzMLFileMemoryMapper {
	private final @Nonnull File mzMLFile;
	private final @Nonnull ArrayList<MzMLSpectrum> spectrumList;

	public MzMLFileMemoryMapper(String mzMLFilePath) {
		this(new File(mzMLFilePath));
	}

	public MzMLFileMemoryMapper(Path mzMLFilePath) {
		this(mzMLFilePath.toFile());
	}

	public MzMLFileMemoryMapper(File mzMLFile) {
		this.mzMLFile = mzMLFile;
		this.spectrumList = new ArrayList<>();
	}

	public InputStream execute() throws IOException, XMLStreamException, MSDKException {

		RandomAccessFile aFile = new RandomAccessFile(mzMLFile, "r");
		FileChannel inChannel = aFile.getChannel();
		MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
		MappedByteBufferInputStream is = new MappedByteBufferInputStream(buffer);

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);

		boolean insideSpectrumListFlag = false;
		while (xmlEventReader.hasNext()) {
			XMLEvent xmlEvent = xmlEventReader.nextEvent();
			if (xmlEvent.isStartElement()) {
				MzMLSpectrum spectrum = new MzMLSpectrum();
				StartElement startElement = xmlEvent.asStartElement();
				if (startElement.getName().getLocalPart().equals("spectrumList") && xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					insideSpectrumListFlag = true;
				}
				if (insideSpectrumListFlag && startElement.getName().getLocalPart().equals("spectrum")
						&& xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					spectrum = new MzMLSpectrum();
				}
				if (insideSpectrumListFlag && startElement.getName().getLocalPart().equals("cvParam")
						&& xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					Attribute accessionAttr = startElement.getAttributeByName(new QName("accession"));
					Attribute valueAttr = startElement.getAttributeByName(new QName("value"));
					spectrum.add(accessionAttr.getValue(), valueAttr.getValue());
				}
			}
			if (xmlEvent.isEndElement()) {
				EndElement endElement = xmlEvent.asEndElement();
				if (endElement.getName().getLocalPart().equals("spectrumList") && xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					insideSpectrumListFlag = false;
				}
			}
		}

		aFile.close();
		is.close();
		return is;
	}
}
