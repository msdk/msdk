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

package io.github.msdk.featdet.targeteddetection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.mzml.MzMLFileImportMethod;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.tolerances.MaximumMzTolerance;
import io.github.msdk.util.tolerances.MzTolerance;
import io.github.msdk.util.tolerances.RTTolerance;

public class TargetedDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @SuppressWarnings("null")
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
        IonAnnotation ion1 = MSDKObjectBuilder.getIonAnnotation();
        ion1.setExpectedMz(332.56);
        ion1.setAnnotationId("Feature 332.56");
        ion1.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 772.8));
        ionAnnotations.add(ion1);

        // Ion 2
        IonAnnotation ion2 = MSDKObjectBuilder.getIonAnnotation();
        ion2.setExpectedMz(508.004);
        ion2.setAnnotationId("Feature 508.004");
        ion2.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 868.8));
        ionAnnotations.add(ion2);

        // Ion 3
        IonAnnotation ion3 = MSDKObjectBuilder.getIonAnnotation();
        ion3.setExpectedMz(362.102);
        ion3.setAnnotationId("Feature 362.102");
        ion3.setChromatographyInfo(MSDKObjectBuilder
                .getChromatographyInfo1D(SeparationType.LC, (float) 643.2));
        ionAnnotations.add(ion3);

        // Variables
        final MzTolerance mzTolerance = new MaximumMzTolerance(0.003, 5.0);
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

        // ************
        // Verify ion 1
        // ************
        Chromatogram chromatogram1 = chromatograms.get(0);
        Assert.assertEquals(1,
                chromatogram1.getChromatogramNumber().intValue());
        rtBuffer = chromatogram1.getRetentionTimes(rtBuffer);
        mzBuffer = chromatogram1.getMzValues(mzBuffer);
        intensityBuffer = chromatogram1.getIntensityValues(intensityBuffer);
        numOfDataPoints = chromatogram1.getNumberOfDataPoints();
        Assert.assertEquals(20, numOfDataPoints);
        Assert.assertEquals(5513891.5, intensityBuffer[8], 0.000001);

        // m/z
        Double mz = chromatogram1.getMz();
        Assert.assertNotNull(mz);
        if (mz != null)
            Assert.assertEquals(332.5622270372179, mz, 0.000001);

        // RT
        Float rt = ChromatogramUtil.getRt(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(rt);
        if (rt != null)
            Assert.assertEquals(12.8835535, rt / 60, 0.000001);

        // RT start
        Float rtStart = ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(rtStart);
        if (rtStart != null)
            Assert.assertEquals(12.538729, rtStart / 60, 0.000001);

        // RT end
        Float rtEnd = ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(rtEnd);
        if (rtEnd != null)
            Assert.assertEquals(13.3210535, rtEnd / 60, 0.000001);

        // Duration
        Float duration = ChromatogramUtil.getDuration(rtBuffer,
                numOfDataPoints);
        Assert.assertNotNull(duration);
        if (duration != null)
            Assert.assertEquals(0.7823242, duration / 60, 0.000001);

        // Height
        Float height = ChromatogramUtil.getMaxHeight(intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(height);
        if (height != null)
            Assert.assertEquals(5513891.5, height, 0.000001);

        // Area
        Double area = ChromatogramUtil.getArea(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(area);
        if (area != null)
            Assert.assertEquals(7.519958076518613E7, area, 0.000001);

        // FWHM
        Double fwhm = ChromatogramUtil.getFwhm(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(fwhm);
        if (fwhm != null)
            Assert.assertEquals(0.20610961, fwhm / 60, 0.000001);

        // Tailing factor
        Double tf = ChromatogramUtil.getTailingFactor(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(tf);
        if (tf != null)
            Assert.assertEquals(1.1181464, tf, 0.001);

        // Asymmetry factor
        Double af = ChromatogramUtil.getAsymmetryFactor(rtBuffer,
                intensityBuffer, numOfDataPoints);
        Assert.assertNotNull(af);
        if (af != null)
            Assert.assertEquals(1.1135141, af, 0.000001);

        // ************
        // Verify ion 2
        // ************
        Chromatogram chromatogram2 = chromatograms.get(1);
        Assert.assertEquals(2,
                chromatogram2.getChromatogramNumber().intValue());
        rtBuffer = chromatogram2.getRetentionTimes(rtBuffer);
        mzBuffer = chromatogram2.getMzValues(mzBuffer);
        intensityBuffer = chromatogram2.getIntensityValues(intensityBuffer);
        numOfDataPoints = chromatogram2.getNumberOfDataPoints();
        Assert.assertEquals(18, numOfDataPoints);
        Assert.assertEquals(6317753.0, intensityBuffer[7], 0.000001);

        // m/z
        mz = chromatogram2.getMz();
        Assert.assertNotNull(mz);
        if (mz != null)
            Assert.assertEquals(508.0034287396599, mz, 0.000001);

        // RT
        rt = ChromatogramUtil.getRt(rtBuffer, intensityBuffer, numOfDataPoints);
        Assert.assertNotNull(rt);
        if (rt != null)
            Assert.assertEquals(14.481896, rt / 60, 0.000001);

        // RT start
        rtStart = ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(rtStart);
        if (rtStart != null)
            Assert.assertEquals(14.16822, rtStart / 60, 0.000001);

        // RT end
        rtEnd = ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(rtEnd);
        if (rtEnd != null)
            Assert.assertEquals(14.9408865, rtEnd / 60, 0.000001);

        // Duration
        duration = ChromatogramUtil.getDuration(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(duration);
        if (duration != null)
            Assert.assertEquals(0.7726664, duration / 60, 0.000001);

        // Height
        height = ChromatogramUtil.getMaxHeight(intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(height);
        if (height != null)
            Assert.assertEquals(6317753.0, height, 0.000001);

        // Area
        area = ChromatogramUtil.getArea(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(area);
        if (area != null)
            Assert.assertEquals(8.448660371074352E7, area, 0.000001);

        // FWHM
        fwhm = ChromatogramUtil.getFwhm(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(fwhm);
        if (fwhm != null)
            Assert.assertEquals(0.19513144, fwhm / 60, 0.000001);

        // Tailing factor
        tf = ChromatogramUtil.getTailingFactor(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(tf);
        if (tf != null)
            Assert.assertEquals(1.3311311, tf, 0.001);

        // Asymmetry factor
        af = ChromatogramUtil.getAsymmetryFactor(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(af);
        if (af != null)
            Assert.assertEquals(1.7337219745465573, af, 0.000001);

        // ************
        // Verify ion 3
        // ************
        Chromatogram chromatogram3 = chromatograms.get(2);
        Assert.assertEquals(3,
                chromatogram3.getChromatogramNumber().intValue());
        rtBuffer = chromatogram3.getRetentionTimes(rtBuffer);
        mzBuffer = chromatogram3.getMzValues(mzBuffer);
        intensityBuffer = chromatogram3.getIntensityValues(intensityBuffer);
        numOfDataPoints = chromatogram3.getNumberOfDataPoints();
        Assert.assertEquals(17, numOfDataPoints);
        Assert.assertEquals(2609394.5, intensityBuffer[5], 0.000001);

        // m/z
        mz = chromatogram3.getMz();
        Assert.assertNotNull(mz);
        if (mz != null)
            Assert.assertEquals(362.1021836224724, mz, 0.000001);

        // RT
        rt = ChromatogramUtil.getRt(rtBuffer, intensityBuffer, numOfDataPoints);
        Assert.assertNotNull(rt);
        if (rt != null)
            Assert.assertEquals(10.722553, rt / 60, 0.000001);

        // RT start
        rtStart = ChromatogramUtil.getRtStart(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(rtStart);
        if (rtStart != null)
            Assert.assertEquals(10.48422, rtStart / 60, 0.000001);

        // RT end
        rtEnd = ChromatogramUtil.getRtEnd(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(rtEnd);
        if (rtEnd != null)
            Assert.assertEquals(11.249222, rtEnd / 60, 0.000001);

        // Duration
        duration = ChromatogramUtil.getDuration(rtBuffer, numOfDataPoints);
        Assert.assertNotNull(duration);
        if (duration != null)
            Assert.assertEquals(0.7650024, duration / 60, 0.000001);

        // Height
        height = ChromatogramUtil.getMaxHeight(intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(height);
        if (height != null)
            Assert.assertEquals(2609394.5, height, 0.000001);

        // Area
        area = ChromatogramUtil.getArea(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(area);
        if (area != null)
            Assert.assertEquals(3.415448809043382E7, area, 0.000001);

        // FWHM
        fwhm = ChromatogramUtil.getFwhm(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(fwhm);
        if (fwhm != null)
            Assert.assertEquals(0.21855775, fwhm / 60, 0.000001);

        // Tailing factor
        tf = ChromatogramUtil.getTailingFactor(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(tf);
        if (tf != null)
            Assert.assertEquals(1.4721208, tf, 0.001);

        // Asymmetry factor
        af = ChromatogramUtil.getAsymmetryFactor(rtBuffer, intensityBuffer,
                numOfDataPoints);
        Assert.assertNotNull(af);
        if (af != null)
            Assert.assertEquals(1.8304700453905858, af, 0.000001);

    }

}
