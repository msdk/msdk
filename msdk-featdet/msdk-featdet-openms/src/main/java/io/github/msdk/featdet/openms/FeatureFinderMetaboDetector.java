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

import io.github.msdk.MSDKException;

public class FeatureFinderMetaboDetector {
	private final String OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME;
	private final File FEATURE_XML_FILE;

	private final @Nonnull File mzMLFile;

	public FeatureFinderMetaboDetector(String mzMLFileName) throws MSDKException {
		this(new File(mzMLFileName));
	}

	public FeatureFinderMetaboDetector(File mzMLFile) throws MSDKException {
		this.mzMLFile = mzMLFile;
		FEATURE_XML_FILE = new File(System.getProperty("java.io.tmpdir") + UUID.randomUUID() + ".featureXML");
		OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME = FeatureFinderMetaboLocator.findFeatureFinderMetabo();
	}

	public List<Double> execute() throws SAXException, IOException, ParserConfigurationException, MSDKException {
		List<Double> mzValuesResult = new ArrayList<Double>();

		if (OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME.equals(null)) {
			throw new MSDKException(
					"FeatureFinderMetabo library not found. Please install OpenMS and add FeatureFinderMetabo to Path.");

		} else {
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
					mzValuesResult.add(
							Double.valueOf(featureElement.getElementsByTagName("position").item(1).getTextContent()));
				}
			}

		}
		return mzValuesResult;

	}

	private void createTempFeatureXMLFile() throws IOException, MSDKException {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.info(execShellCommand(OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + " -in " + mzMLFile.getAbsolutePath()
				+ " -out " + FEATURE_XML_FILE.getAbsolutePath()));
		if (!FEATURE_XML_FILE.exists())
			throw new MSDKException("Could not create featureXML File");
		FEATURE_XML_FILE.deleteOnExit();
	}

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
