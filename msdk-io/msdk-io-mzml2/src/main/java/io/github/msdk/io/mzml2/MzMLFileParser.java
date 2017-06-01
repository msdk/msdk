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

public class MzMLFileParser {
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

	public MzMLRawDataFile execute() throws IOException, XMLStreamException, MSDKException {

		MzMLFileMemoryMapper mapper = new MzMLFileMemoryMapper();
		MappedByteBufferInputStream is = mapper.mapToMemory(mzMLFile);

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);

		boolean insideSpectrumListFlag = false;
		MzMLSpectrum spectrum = null;
		while (xmlEventReader.hasNext()) {
			XMLEvent xmlEvent = xmlEventReader.nextEvent();
			if (xmlEvent.isStartElement()) {
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
						&& spectrum != null && xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					Attribute accessionAttr = startElement.getAttributeByName(new QName("accession"));
					Attribute valueAttr = startElement.getAttributeByName(new QName("value"));
					spectrum.add(accessionAttr.getValue(), valueAttr.getValue());
				}
				if (insideSpectrumListFlag && startElement.getName().getLocalPart().equals("binary") && spectrum != null
						&& xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					spectrum.addBinaryDataPosition(is.getCurrentPosition());
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
		return null;
	}
}
