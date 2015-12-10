/* 
 * (C) Copyright 2015 by MSDK Development Team
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

package io.github.msdk.filtering.rowfilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.featuredetection.chromatogramtofeaturetable.ChromatogramToFeatureTableMethod;
import io.github.msdk.featuredetection.targeteddetection.TargetedDetectionMethod;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class RowFilterMethodTest {

	private static final String TEST_DATA_PATH = "src/test/resources/";

	@Test
	public void testOrbitrap() throws MSDKException {

		// Create the data structures
		final DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();

		// Import the file
		File inputFile = new File(TEST_DATA_PATH + "orbitrap_300-600mz.mzML");
		Assert.assertTrue("Cannot read test data", inputFile.canRead());
		MzMLFileImportMethod importer = new MzMLFileImportMethod(inputFile);
		RawDataFile rawFile = importer.execute();
		Assert.assertNotNull(rawFile);
		Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);

		// Ion 1
		List<IonAnnotation> ionAnnotations = new ArrayList<IonAnnotation>();
		IonAnnotation ion1 = MSDKObjectBuilder.getSimpleIonAnnotation();
		ion1.setExpectedMz(332.56);
		ion1.setAnnotationId("Feature 332.56");
		ion1.setChromatographyInfo(MSDKObjectBuilder.getChromatographyInfo1D(SeparationType.LC, (float) 772.8));
		ionAnnotations.add(ion1);

		// Ion 2
		IonAnnotation ion2 = MSDKObjectBuilder.getSimpleIonAnnotation();
		ion2.setExpectedMz(508.004);
		ion2.setAnnotationId("Feature 508.004");
		ion2.setChromatographyInfo(MSDKObjectBuilder.getChromatographyInfo1D(SeparationType.LC, (float) 868.8));
		ionAnnotations.add(ion2);

		// Ion 3
		IonAnnotation ion3 = MSDKObjectBuilder.getSimpleIonAnnotation();
		ion3.setExpectedMz(362.102);
		ion3.setAnnotationId("Feature 362.102");
		ion3.setChromatographyInfo(MSDKObjectBuilder.getChromatographyInfo1D(SeparationType.LC, (float) 643.2));
		ionAnnotations.add(ion3);

		// Variables
		final MZTolerance mzTolerance = new MZTolerance(0.003, 5.0);
		final RTTolerance rtTolerance = new RTTolerance(3, false);
		final Double intensityTolerance = 0.10d;
		final Double noiseLevel = 5000d;

		TargetedDetectionMethod chromBuilder = new TargetedDetectionMethod(ionAnnotations, rawFile, dataStore,
				mzTolerance, rtTolerance, intensityTolerance, noiseLevel);
		final List<Chromatogram> detectedChromatograms = chromBuilder.execute();
		Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);
		List<Chromatogram> chromatograms = chromBuilder.getResult();
		Assert.assertNotNull(chromatograms);
		Assert.assertEquals(3, chromatograms.size());

		// Create a new feature table
		FeatureTable featureTable = MSDKObjectBuilder.getFeatureTable("orbitrap_300-600mz", dataStore);
		Sample sample = MSDKObjectBuilder.getSimpleSample("orbitrap_300-600mz");

		ChromatogramToFeatureTableMethod tableBuilder = new ChromatogramToFeatureTableMethod(chromatograms,
				featureTable, sample);
		tableBuilder.execute();

	}

}
