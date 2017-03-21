package io.github.msdk.featdet.openms;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class FeatureFinderMetaboMZValueDetectorTest {

	private static final String TEST_DATA_PATH = "src/test/resources/";

	@Test
	public void test() throws SAXException, IOException, ParserConfigurationException {

		final String inputFileName = TEST_DATA_PATH + "msms.mzML";
		final String outputFileName = TEST_DATA_PATH + "msms.featureXML";
		final File inputFile = new File(inputFileName);
		final File outputFile = new File(outputFileName);

		// Initialize using File Object
		Assert.assertNotNull(inputFile);
		FeatureFinderMetaboMZValueDetector mzDetector = new FeatureFinderMetaboMZValueDetector(inputFile, outputFile);
		Assert.assertEquals(233, mzDetector.execute().size());
		Assert.assertNotNull(outputFile);

		// Initialize using FileName
		Assert.assertNotNull(new File(inputFileName));
		FeatureFinderMetaboMZValueDetector mzDetector2 = new FeatureFinderMetaboMZValueDetector(inputFileName,
				outputFileName);
		Assert.assertEquals(233, mzDetector2.execute().size());
		Assert.assertNotNull(new File(outputFileName));
	}

}
