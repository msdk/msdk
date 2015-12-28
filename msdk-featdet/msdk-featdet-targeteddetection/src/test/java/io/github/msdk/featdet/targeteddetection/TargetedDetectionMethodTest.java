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

package io.github.msdk.featdet.targeteddetection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class TargetedDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testOrbitrap() throws MSDKException {

        // Create the data structures
        final DataPointStore dataStore = DataPointStoreFactory
                .getMemoryDataStore();
        ChromatographyInfo rtBuffer[] = new ChromatographyInfo[10000];
        double mzBuffer[] = new double[10000];
        float intensityBuffer[] = new float[10000];
        int numOfDataPoints;

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
        ion1.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 772.8));
        ionAnnotations.add(ion1);

        // Ion 2
        IonAnnotation ion2 = MSDKObjectBuilder.getSimpleIonAnnotation();
        ion2.setExpectedMz(508.004);
        ion2.setAnnotationId("Feature 508.004");
        ion2.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 868.8));
        ionAnnotations.add(ion2);

        // Ion 3
        IonAnnotation ion3 = MSDKObjectBuilder.getSimpleIonAnnotation();
        ion3.setExpectedMz(362.102);
        ion3.setAnnotationId("Feature 362.102");
        ion3.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 643.2));
        ionAnnotations.add(ion3);

        // Variables
        final MZTolerance mzTolerance = new MZTolerance(0.003, 5.0);
        final RTTolerance rtTolerance = new RTTolerance(0.2, false);
        final Double intensityTolerance = 0.10d;
        final Double noiseLevel = 5000d;

        TargetedDetectionMethod chromBuilder = new TargetedDetectionMethod(
                ionAnnotations, rawFile, dataStore, mzTolerance, rtTolerance,
                intensityTolerance, noiseLevel);
        final List<Chromatogram> chromatograms = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);
        Assert.assertNotNull(chromatograms);
        Assert.assertEquals(3, chromatograms.size());

        // Verify ion 1
        Chromatogram chromatogram1 = chromatograms.get(0);
        Assert.assertEquals(1,
                chromatogram1.getChromatogramNumber().intValue());
        rtBuffer = chromatogram1.getRetentionTimes(rtBuffer);
        mzBuffer = chromatogram1.getMzValues(mzBuffer);
        intensityBuffer = chromatogram1.getIntensityValues(intensityBuffer);
        numOfDataPoints = chromatogram1.getNumberOfDataPoints();
        Assert.assertEquals(20, numOfDataPoints);
        Assert.assertEquals(5513891.5, intensityBuffer[8], 0.000001);
        Assert.assertEquals(332.5622270372179, chromatogram1.getMz(), 0.000001);
        Assert.assertEquals(12.8835535, ChromatogramUtil.getRt(rtBuffer,
                intensityBuffer, numOfDataPoints) / 60, 0.000001);
        Assert.assertEquals(12.538729,
                ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(13.3210535,
                ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(0.7823242,
                ChromatogramUtil.getDuration(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(5513891.5,
                ChromatogramUtil.getMaxHeight(intensityBuffer, numOfDataPoints),
                0.000001);
        Assert.assertEquals(7.5199576E7, ChromatogramUtil.getArea(rtBuffer,
                intensityBuffer, numOfDataPoints), 0.000001);
        Assert.assertEquals(0.20610961, ChromatogramUtil.getFwhm(rtBuffer,
                intensityBuffer, numOfDataPoints) / 60, 0.000001);
        Assert.assertEquals(1.1181464, ChromatogramUtil.getTailingFactor(
                rtBuffer, intensityBuffer, numOfDataPoints), 0.001);
        Assert.assertEquals(1.1135141, ChromatogramUtil.getAsymmetryFactor(
                rtBuffer, intensityBuffer, numOfDataPoints), 0.000001);

        // Verify ion 2
        Chromatogram chromatogram2 = chromatograms.get(1);
        Assert.assertEquals(2,
                chromatogram2.getChromatogramNumber().intValue());
        rtBuffer = chromatogram2.getRetentionTimes(rtBuffer);
        mzBuffer = chromatogram2.getMzValues(mzBuffer);
        intensityBuffer = chromatogram2.getIntensityValues(intensityBuffer);
        numOfDataPoints = chromatogram2.getNumberOfDataPoints();
        Assert.assertEquals(18, numOfDataPoints);
        Assert.assertEquals(6317753.0, intensityBuffer[7], 0.000001);
        Assert.assertEquals(508.0034287396599, chromatogram2.getMz(), 0.000001);
        Assert.assertEquals(14.481896, ChromatogramUtil.getRt(rtBuffer,
                intensityBuffer, numOfDataPoints) / 60, 0.000001);
        Assert.assertEquals(14.16822,
                ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(14.9408865,
                ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(0.7726664,
                ChromatogramUtil.getDuration(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(6317753.0,
                ChromatogramUtil.getMaxHeight(intensityBuffer, numOfDataPoints),
                0.000001);
        Assert.assertEquals(8.4486608E7, ChromatogramUtil.getArea(rtBuffer,
                intensityBuffer, numOfDataPoints), 0.000001);
        Assert.assertEquals(0.19513144, ChromatogramUtil.getFwhm(rtBuffer,
                intensityBuffer, numOfDataPoints) / 60, 0.000001);
        Assert.assertEquals(1.3311311, ChromatogramUtil.getTailingFactor(
                rtBuffer, intensityBuffer, numOfDataPoints), 0.0001);
        Assert.assertEquals(1.7337204, ChromatogramUtil.getAsymmetryFactor(
                rtBuffer, intensityBuffer, numOfDataPoints), 0.0001);

        // Verify ion 3
        Chromatogram chromatogram3 = chromatograms.get(2);
        Assert.assertEquals(3,
                chromatogram3.getChromatogramNumber().intValue());
        rtBuffer = chromatogram3.getRetentionTimes(rtBuffer);
        mzBuffer = chromatogram3.getMzValues(mzBuffer);
        intensityBuffer = chromatogram3.getIntensityValues(intensityBuffer);
        numOfDataPoints = chromatogram3.getNumberOfDataPoints();
        Assert.assertEquals(17, numOfDataPoints);
        Assert.assertEquals(2609394.5, intensityBuffer[5], 0.000001);
        Assert.assertEquals(362.1021836224724, chromatogram3.getMz(), 0.000001);
        Assert.assertEquals(10.722553, ChromatogramUtil.getRt(rtBuffer,
                intensityBuffer, numOfDataPoints) / 60, 0.000001);
        Assert.assertEquals(10.48422,
                ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(11.249222,
                ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(0.7650024,
                ChromatogramUtil.getDuration(rtBuffer, numOfDataPoints) / 60,
                0.000001);
        Assert.assertEquals(2609394.5,
                ChromatogramUtil.getMaxHeight(intensityBuffer, numOfDataPoints),
                0.000001);
        Assert.assertEquals(3.4154492E7, ChromatogramUtil.getArea(rtBuffer,
                intensityBuffer, numOfDataPoints), 0.000001);
        Assert.assertEquals(0.21855775, ChromatogramUtil.getFwhm(rtBuffer,
                intensityBuffer, numOfDataPoints) / 60, 0.000001);
        Assert.assertEquals(1.4721208, ChromatogramUtil.getTailingFactor(
                rtBuffer, intensityBuffer, numOfDataPoints), 0.000001);
        Assert.assertEquals(1.8304727, ChromatogramUtil.getAsymmetryFactor(
                rtBuffer, intensityBuffer, numOfDataPoints), 0.0001);

    }

}
