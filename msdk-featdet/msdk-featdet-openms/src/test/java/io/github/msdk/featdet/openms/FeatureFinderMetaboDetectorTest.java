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

package io.github.msdk.featdet.openms;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import com.google.common.base.Strings;

import io.github.msdk.MSDKException;

public class FeatureFinderMetaboDetectorTest {

	private static final String TEST_DATA_PATH = "src/test/resources/";

	@Before
	public void runCondition() throws MSDKException {
		// This code will ensure that the tests are executed only when
		// FeatureFinderMetabo is installed
		String programLocation = FeatureFinderMetaboLocator.findFeatureFinderMetabo();
		Assume.assumeFalse(Strings.isNullOrEmpty(programLocation));
	}

	@Test
	public void testValidmzMLfile() throws IOException, MSDKException, XMLStreamException {

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
	public void testInvalidmzMLFile() throws IOException, MSDKException, XMLStreamException {
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
