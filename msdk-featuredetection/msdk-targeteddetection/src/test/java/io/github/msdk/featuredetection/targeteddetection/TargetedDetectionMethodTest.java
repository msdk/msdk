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
import io.github.msdk.io.rawdataimport.mzml.MzMLFileImportMethod;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

public class TargetedDetectionMethodTest {

    private static final String TEST_DATA_PATH = "src/test/resources/";

    @Test
    public void testOrbitrap() throws MSDKException {

        // Create the data structures
        final DataPointStore dataStore = DataPointStoreFactory.getMemoryDataStore();
        final ChromatogramDataPointList dataPoints = MSDKObjectBuilder.getChromatogramDataPointList();

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
        final RTTolerance rtTolerance = new RTTolerance(6, false);
        final Double intensityTolerance = 10d;
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
        Assert.assertEquals(1, chromatogram1.getChromatogramNumber().intValue());
        chromatogram1.getDataPoints(dataPoints);
        System.out.println(dataPoints.getSize());

        // Verify ion 2
        Chromatogram chromatogram2 = chromatograms.get(1);
        Assert.assertEquals(2, chromatogram2.getChromatogramNumber().intValue());
        chromatogram2.getDataPoints(dataPoints);
        System.out.println(dataPoints.getSize());

        // Verify ion 3
        Chromatogram chromatogram3 = chromatograms.get(2);
        Assert.assertEquals(3, chromatogram3.getChromatogramNumber().intValue());
        chromatogram3.getDataPoints(dataPoints);
        System.out.println(dataPoints.getSize());
    }

}
