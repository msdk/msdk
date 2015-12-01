package io.github.msdk.featuredetection.targeteddetection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.datapointstore.DataPointStoreFactory;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
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
        final ChromatogramDataPointList dataPoints = MSDKObjectBuilder
                .getChromatogramDataPointList();

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
        final List<Chromatogram> detectedChromatograms = chromBuilder.execute();
        Assert.assertEquals(1.0, chromBuilder.getFinishedPercentage(), 0.0001);
        List<Chromatogram> chromatograms = chromBuilder.getResult();
        Assert.assertNotNull(chromatograms);
        Assert.assertEquals(3, chromatograms.size());

        // Verify ion 1
        Chromatogram chromatogram1 = chromatograms.get(0);
        Assert.assertEquals(1,
                chromatogram1.getChromatogramNumber().intValue());
        chromatogram1.getDataPoints(dataPoints);
        Assert.assertEquals(20, dataPoints.getSize());
        Assert.assertEquals(5513891.5, dataPoints.getIntensityBuffer()[8],
                0.000001);
        Assert.assertEquals(332.5622270372179, chromatogram1.getMz(), 0.000001);
        Assert.assertEquals(12.8835535,
                ChromatogramUtil.getRt(chromatogram1) / 60, 0.000001);
        Assert.assertEquals(12.538729,
                ChromatogramUtil.getRtStart(chromatogram1) / 60, 0.000001);
        Assert.assertEquals(13.3210535,
                ChromatogramUtil.getRtEnd(chromatogram1) / 60, 0.000001);
        Assert.assertEquals(0.7823242,
                ChromatogramUtil.getDuration(chromatogram1) / 60, 0.000001);
        Assert.assertEquals(5513891.5,
                ChromatogramUtil.getMaxHeight(chromatogram1), 0.000001);
        Assert.assertEquals(7.5199576E7,
                ChromatogramUtil.getArea(chromatogram1), 0.000001);
        Assert.assertEquals(20,
                ChromatogramUtil.getNumberOfDataPoints(chromatogram1),
                0.000001);
        Assert.assertEquals(0.20610961,
                ChromatogramUtil.getFwhm(chromatogram1) / 60, 0.000001);
        Assert.assertEquals(1.1181464,
                ChromatogramUtil.getTailingFactor(chromatogram1), 0.001);
        Assert.assertEquals(1.1135141,
                ChromatogramUtil.getAsymmetryFactor(chromatogram1), 0.000001);

        // Verify ion 2
        Chromatogram chromatogram2 = chromatograms.get(1);
        Assert.assertEquals(2,
                chromatogram2.getChromatogramNumber().intValue());
        chromatogram2.getDataPoints(dataPoints);
        Assert.assertEquals(18, dataPoints.getSize());
        Assert.assertEquals(6317753.0, dataPoints.getIntensityBuffer()[7],
                0.000001);
        Assert.assertEquals(508.0034287396599, chromatogram2.getMz(), 0.000001);
        Assert.assertEquals(14.481896,
                ChromatogramUtil.getRt(chromatogram2) / 60, 0.000001);
        Assert.assertEquals(14.16822,
                ChromatogramUtil.getRtStart(chromatogram2) / 60, 0.000001);
        Assert.assertEquals(14.9408865,
                ChromatogramUtil.getRtEnd(chromatogram2) / 60, 0.000001);
        Assert.assertEquals(0.7726664,
                ChromatogramUtil.getDuration(chromatogram2) / 60, 0.000001);
        Assert.assertEquals(6317753.0,
                ChromatogramUtil.getMaxHeight(chromatogram2), 0.000001);
        Assert.assertEquals(8.4486608E7,
                ChromatogramUtil.getArea(chromatogram2), 0.000001);
        Assert.assertEquals(18,
                ChromatogramUtil.getNumberOfDataPoints(chromatogram2),
                0.000001);
        Assert.assertEquals(0.19513144,
                ChromatogramUtil.getFwhm(chromatogram2) / 60, 0.000001);
        Assert.assertEquals(1.3311311,
                ChromatogramUtil.getTailingFactor(chromatogram2), 0.0001);
        Assert.assertEquals(1.7337204,
                ChromatogramUtil.getAsymmetryFactor(chromatogram2), 0.0001);

        // Verify ion 3
        Chromatogram chromatogram3 = chromatograms.get(2);
        Assert.assertEquals(3,
                chromatogram3.getChromatogramNumber().intValue());
        chromatogram3.getDataPoints(dataPoints);
        Assert.assertEquals(17, dataPoints.getSize());
        Assert.assertEquals(2609394.5, dataPoints.getIntensityBuffer()[5],
                0.000001);
        Assert.assertEquals(362.1021836224724, chromatogram3.getMz(), 0.000001);
        Assert.assertEquals(10.722553,
                ChromatogramUtil.getRt(chromatogram3) / 60, 0.000001);
        Assert.assertEquals(10.48422,
                ChromatogramUtil.getRtStart(chromatogram3) / 60, 0.000001);
        Assert.assertEquals(11.249222,
                ChromatogramUtil.getRtEnd(chromatogram3) / 60, 0.000001);
        Assert.assertEquals(0.7650024,
                ChromatogramUtil.getDuration(chromatogram3) / 60, 0.000001);
        Assert.assertEquals(2609394.5,
                ChromatogramUtil.getMaxHeight(chromatogram3), 0.000001);
        Assert.assertEquals(3.4154492E7,
                ChromatogramUtil.getArea(chromatogram3), 0.000001);
        Assert.assertEquals(17,
                ChromatogramUtil.getNumberOfDataPoints(chromatogram3),
                0.000001);
        Assert.assertEquals(0.21855775,
                ChromatogramUtil.getFwhm(chromatogram3) / 60, 0.000001);
        Assert.assertEquals(1.4721208,
                ChromatogramUtil.getTailingFactor(chromatogram3), 0.000001);
        Assert.assertEquals(1.8304727,
                ChromatogramUtil.getAsymmetryFactor(chromatogram3), 0.0001);

    }

}
