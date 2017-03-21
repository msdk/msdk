package io.github.msdk.featdet.openms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import io.github.msdk.datamodel.rawdata.RawDataFile;

public class FeatureFinderMetaboMZValueDetector {
	private final String OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME = "FeatureFinderMetabo";

	private final @Nonnull File mzMLFile;
	private final @Nonnull File featureXMLFile;

	public FeatureFinderMetaboMZValueDetector(String mzMLFileName, String featureXMLFileName) {
		this(new File(mzMLFileName), new File(featureXMLFileName));
	}

	public FeatureFinderMetaboMZValueDetector(File mzMLFile, File featureXMLFile) {
		this.mzMLFile = mzMLFile;
		this.featureXMLFile = featureXMLFile;
	}

	public List<Double> execute() throws SAXException, IOException, ParserConfigurationException {
		List<Double> mzValuesResult = new ArrayList<Double>();

		System.out.println(execShellCommand(OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + " -in "
				+ mzMLFile.getAbsolutePath() + " -out " + featureXMLFile.getAbsolutePath()));

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(featureXMLFile);
		doc.getDocumentElement().normalize();

		NodeList featureMap = doc.getElementsByTagName("featureMap").item(0).getChildNodes();
		NodeList featureList = null;
		for (int i = 0; i < featureMap.getLength(); i++) {
			Node node = featureMap.item(i);
			if (node.getNodeName() == "featureList")
				featureList = node.getChildNodes();
		}

		for (int i = 0; i < featureList.getLength(); i++) {
			Node node = featureList.item(i);
			if (node.getNodeName() == "feature") {
				Element featureElement = (Element) node;
				mzValuesResult
						.add(Double.valueOf(featureElement.getElementsByTagName("position").item(1).getTextContent()));
			}
		}
		return mzValuesResult;

	}

	private String execShellCommand(String cmd) {
		String out = "";
		try {
			Process cmdProc = Runtime.getRuntime().exec(cmd);
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
			String line;
			while ((line = stdoutReader.readLine()) != null) {
				out += line + "\n";
			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return out;
	}
}
