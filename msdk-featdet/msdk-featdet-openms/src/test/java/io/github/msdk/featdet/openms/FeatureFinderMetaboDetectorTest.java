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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import io.github.msdk.MSDKException;

public class FeatureFinderMetaboDetectorTest {

	private static final String TEST_DATA_PATH = "src/test/resources/";

	@Test
	public void testValidmzMLfile() throws SAXException, IOException, ParserConfigurationException, MSDKException {

		final String inputFileName = TEST_DATA_PATH + "msms.mzML";
		final File inputFile = new File(inputFileName);

		Assert.assertNotNull(inputFile);
		FeatureFinderMetaboDetector mzDetector = new FeatureFinderMetaboDetector(inputFile);
		Assert.assertEquals(233, mzDetector.execute().size());

		Assert.assertNotNull(new File(inputFileName));
		FeatureFinderMetaboDetector mzDetector2 = new FeatureFinderMetaboDetector(inputFileName);
		Assert.assertEquals(233, mzDetector2.execute().size());
	}

	@Test(expected = MSDKException.class)
	public void testInvalidmzMLFile() throws SAXException, IOException, ParserConfigurationException, MSDKException {
		final String truncatedFileName = TEST_DATA_PATH + "truncated.mzML";
		final File truncatedFile = new File(truncatedFileName);

		Assert.assertNotNull(truncatedFile);
		FeatureFinderMetaboDetector mzDetector3 = new FeatureFinderMetaboDetector(truncatedFile);
		Assert.assertEquals(0, mzDetector3.execute().size());

		Assert.assertNotNull(new File(truncatedFileName));
		FeatureFinderMetaboDetector mzDetector4 = new FeatureFinderMetaboDetector(truncatedFileName);
		Assert.assertEquals(0, mzDetector4.execute().size());
	}

}
