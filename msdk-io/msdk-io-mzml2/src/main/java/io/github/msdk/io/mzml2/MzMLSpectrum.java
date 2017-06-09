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

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.zip.DataFormatException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.MsScanType;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.tolerances.MzTolerance;

public class MzMLSpectrum implements MsScan {
	private HashMap<String, String> cvParamValues;
	private MzMLBinaryDataInfo mzBinaryDataInfo;
	private MzMLBinaryDataInfo intensityBinaryDataInfo;
	private MappedByteBufferInputStream mappedByteBufferInputStream;

	public MzMLSpectrum() {
		cvParamValues = new HashMap<>();
	}

	public void add(String accession, String value) {
		cvParamValues.put(accession, value);
	}

	public HashMap<String, String> getSpectrumData() {
		return cvParamValues;
	}

	public int getSpectrumDataSize() {
		return cvParamValues.size();
	}

	public MzMLBinaryDataInfo getMzBinaryDataInfo() {
		return mzBinaryDataInfo;
	}

	public void setMzBinaryDataInfo(MzMLBinaryDataInfo mzBinaryDataInfo) {
		this.mzBinaryDataInfo = mzBinaryDataInfo;
	}

	public MzMLBinaryDataInfo getIntensityBinaryDataInfo() {
		return intensityBinaryDataInfo;
	}

	public void setIntensityBinaryDataInfo(MzMLBinaryDataInfo intensityBinaryDataInfo) {
		this.intensityBinaryDataInfo = intensityBinaryDataInfo;
	}

	public MappedByteBufferInputStream getMappedByteBufferInputStream() {
		return mappedByteBufferInputStream;
	}

	public void setMappedByteBufferInputStream(MappedByteBufferInputStream mappedByteBufferInputStream) {
		this.mappedByteBufferInputStream = mappedByteBufferInputStream;
	}

	// TODO Configure implemented methods
	@Override
	public MsSpectrumType getSpectrumType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getNumberOfDataPoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getMzValues() {
		double[] result = null;
		byte[] bytesIn = new byte[getMzBinaryDataInfo().getEncodedLength()];
		int precision = 0;
		EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions = EnumSet
				.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);
		try {
			mappedByteBufferInputStream.setPosition(getMzBinaryDataInfo().getPosition());
			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(mappedByteBufferInputStream);

			if (getMzBinaryDataInfo().getBitLength().toString().startsWith("THIRTY_TWO")) {
				precision = 32;
			} else if (getMzBinaryDataInfo().getBitLength().toString().startsWith("SIXTY_FOUR")) {
				precision = 64;
			}
			// System.out.println(new
			// Scanner(mappedByteBufferInputStream).useDelimiter("//A").next());
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("binary") && xmlEventReader.hasNext()) {
						xmlEvent = xmlEventReader.nextEvent();
						bytesIn = xmlEvent.asCharacters().getData().getBytes();
						compressions.add(getMzBinaryDataInfo().getCompressionType());
						result = MzMLPeaksDecoder.decode(bytesIn, getMzBinaryDataInfo().getArrayLength(),
								new Integer(precision), getMzBinaryDataInfo().getEncodedLength(), compressions).arr;
						break;
					}
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MSDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public float[] getIntensityValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getTIC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range<Double> getMzRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MzTolerance getMzTolerance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RawDataFile getRawDataFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getScanNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScanDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MsFunction getMsFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MsScanType getMsScanType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Range<Double> getScanningRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PolarityType getPolarity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActivationInfo getSourceInducedFragmentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IsolationInfo> getIsolations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Float getRetentionTime() {
		// TODO Auto-generated method stub
		return null;
	}
}
