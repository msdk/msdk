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

package io.github.msdk.featuredetection.openms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;

import io.github.msdk.MSDKException;

/**
 * <p>FeatureFinderMetaboDetector class.</p>
 *
 *         This class creates a list m/z values (double) for a mzML input file
 *         using OpenMS TOPP Tools
 */
public class FeatureFinderMetaboDetector {
	private final String OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME;
	private final @Nonnull File FEATURE_XML_FILE;
	private final @Nonnull String FEATURE_XML_FILE_PATH;

	private final @Nonnull File mzMLFile;
	private final @Nonnull String mzMLFILE_PATH;

	/**
	 *
	 * <p>
	 * Constructor for FeatureFinderMetaboDetector.
	 * </p>
	 *
	 * @param mzMLFilePath
	 *            Path to mzML File to be processed
	 * @throws io.github.msdk.MSDKException if any.
	 */
	public FeatureFinderMetaboDetector(String mzMLFilePath) throws MSDKException {
		this(new File(mzMLFilePath));
	}

	/**
	 *
	 * <p>
	 * Constructor for FeatureFinderMetaboDetector.
	 * </p>
	 *
	 * @param mzMLFile
	 *            {@link java.io.File} reference to mzML File to be processed
	 * @throws io.github.msdk.MSDKException if any.
	 */
	public FeatureFinderMetaboDetector(File mzMLFile) throws MSDKException {
		this.mzMLFile = mzMLFile;
		FEATURE_XML_FILE = new File(System.getProperty("java.io.tmpdir") + UUID.randomUUID() + ".featureXML");
		OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME = FeatureFinderMetaboLocator.findFeatureFinderMetabo();
		mzMLFILE_PATH = mzMLFile.getAbsolutePath();
		FEATURE_XML_FILE_PATH = FEATURE_XML_FILE.getAbsolutePath();
	}

	/**
	 * Runs FeatureFinderMetabo on the output file, processes the output file
	 * for mz values data.
	 *
	 * @return List of m/z Values
	 * @throws javax.xml.stream.XMLStreamException if any.
	 * @throws java.io.IOException if any.
	 * @throws io.github.msdk.MSDKException if any.
	 */
	public List<Double> execute() throws XMLStreamException, IOException, MSDKException {
		List<Double> mzValuesResult = new ArrayList<Double>();
		createTempFeatureXMLFile();

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileReader(FEATURE_XML_FILE));

		while (xmlEventReader.hasNext()) {
			XMLEvent xmlEvent = xmlEventReader.nextEvent();
			if (xmlEvent.isStartElement()) {
				StartElement startElement = xmlEvent.asStartElement();
				if (startElement.getName().getLocalPart().equals("position") && xmlEventReader.hasNext()) {
					xmlEvent = xmlEventReader.nextEvent();
					Attribute idAttr = startElement.getAttributeByName(new QName("dim"));
					if (idAttr.getValue().equals("1")) {
						Characters characters = xmlEvent.asCharacters();
						/*
						 * Stores 1573.19798906671 in the ArrayList from the XML
						 * line: <position dim="1">1573.19798906671</position>
						 */
						try {
							mzValuesResult.add(Double.valueOf(characters.getData()));
						} catch (NumberFormatException e) {
							throw new MSDKException("The featureXML contains an invalid m/z value.\n" + e.getMessage());
						}
					}

				}
			}
		}

		return mzValuesResult;

	}

	/**
	 * Creates a temporary FeatureXML file based on the input mzML file using
	 * the FeatureFinderMetabo library. The FeatureXML file is stored in a
	 * temporary directory (specified by the environment variable
	 * java.io.tmpdir)
	 * 
	 * @throws IOException
	 * 
	 * @throws MSDKException
	 */
	private void createTempFeatureXMLFile() throws IOException, MSDKException {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		if (Strings.isNullOrEmpty(OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME)) {
			throw new MSDKException("FeatureFinderMetabo not found. Please install OpenMS on your machine.");
		}
		final String cmd = OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + " -in \"" + mzMLFILE_PATH + "\" -out \"" + FEATURE_XML_FILE_PATH + "\"";
		final String cmdOutput = execShellCommand(cmd);
		logger.debug("Executing command " + cmd);
		if (!FEATURE_XML_FILE.exists()) {
			String error = "";
			Scanner cmdOutputParser = new Scanner(cmdOutput);
			while (cmdOutputParser.hasNext()) {
				String curLine = cmdOutputParser.nextLine();
				if (curLine.toLowerCase().contains("error"))
					error += curLine;
			}
			cmdOutputParser.close();
			throw new MSDKException("The input mzML could not be processed.\n" + error);
		}
		FEATURE_XML_FILE.deleteOnExit();
	}

	/**
	 * Executes the specified string command in a separate process.
	 * 
	 * @param cmd
	 *            The command to be executed
	 * 
	 * @return The output obtained after executing the passed cmd
	 */
	private String execShellCommand(String cmd) throws MSDKException {
		String out = "";
		try {
			Process cmdProc = Runtime.getRuntime().exec(cmd);
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
			String line;
			while ((line = stdoutReader.readLine()) != null) {
				out += line + "\n";
			}
		} catch (IOException e) {
			throw new MSDKException(e.getMessage());
		}
		return out;
	}
}
