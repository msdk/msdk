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

package io.github.msdk.featdet.openms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import io.github.msdk.MSDKException;

/**
 * 
 * @author Adhithya
 * 
 *         This class creates a list m/z values (double) for a mzML input file
 *         using OpenMS TOPP Tools
 *
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
	 */
	public List<Double> execute() throws SAXException, IOException, ParserConfigurationException, MSDKException {
		List<Double> mzValuesResult = new ArrayList<Double>();

		createTempFeatureXMLFile();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(FEATURE_XML_FILE);
		doc.getDocumentElement().normalize();

		NodeList featureMap = doc.getElementsByTagName("featureMap").item(0).getChildNodes();
		NodeList featureList = null;
		for (int i = 0; i < featureMap.getLength(); i++) {
			Node node = featureMap.item(i);
			if (node.getNodeName().equals("featureList"))
				featureList = node.getChildNodes();
		}

		for (int i = 0; i < featureList.getLength(); i++) {
			Node node = featureList.item(i);
			if (node.getNodeName().equals("feature")) {
				Element featureElement = (Element) node;
				mzValuesResult
						.add(Double.valueOf(featureElement.getElementsByTagName("position").item(1).getTextContent()));
			}
		}

		return mzValuesResult;

	}

	private void createTempFeatureXMLFile() throws IOException, MSDKException {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		if (Strings.isNullOrEmpty(OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME)) {
			throw new MSDKException("FeatureFinderMetabo not found. Please install OpenMS on your machine.");
		}
		final String cmdOutput = execShellCommand(
				OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + " -in " + mzMLFILE_PATH + " -out " + FEATURE_XML_FILE_PATH);
		logger.info(cmdOutput);
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
