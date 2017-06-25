package io.github.msdk.io.mzxml;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.MsSpectrumUtil;

public class MzXMLParserBenchmark {

  private static final String TEST_DATA_PATH = "src/test/resources/";

  public static void main(String[] args) throws MSDKException, NumberFormatException {
    int numberOfRuns = 10;
    Logger logger = LoggerFactory.getLogger(MzXMLParserBenchmark.class);

    if (args.length > 0)
      numberOfRuns = Integer.valueOf(args[0]);

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      testA10A2New();
    long endTime = System.currentTimeMillis();

    long startTime2 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      testR1RG59B41New();
    long endTime2 = System.currentTimeMillis();

    long startTime3 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      testA10A2();
    long endTime3 = System.currentTimeMillis();

    long startTime4 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      testR1RG59B41();
    long endTime4 = System.currentTimeMillis();

    logger.debug("Average run time with MzXMLFileParser File 1: "
        + (endTime - startTime) / numberOfRuns + "ms");
    logger.debug("Average run time with MzXMLFileParser File 2: "
        + (endTime2 - startTime2) / numberOfRuns + "ms");
    logger.debug("Average run time with MzXMLFileImportMethod File 1: "
        + (endTime3 - startTime3) / numberOfRuns + "ms");
    logger.debug("Average run time with MzXMLFileImportMethod File 2: "
        + (endTime4 - startTime4) / numberOfRuns + "ms");

  }

  public static void testA10A2New() throws MSDKException {

    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "A1-0_A2.mzXML");
    Assert.assertTrue(inputFile.canRead());
    MzXMLFileParser parser = new MzXMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();

    // The file has 1 scan
    List<MsScan> scans = rawFile.getScans();

    // 1st scan, #1
    MsScan scan1 = scans.get(0);
    scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Float scan1MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());

    rawFile.dispose();

  }

  public static void testR1RG59B41New() throws MSDKException {

    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "R1_RG59_B4_1.mzXML");
    Assert.assertTrue(inputFile.canRead());
    MzXMLFileParser parser = new MzXMLFileParser(inputFile);
    RawDataFile rawFile = parser.execute();

    // The file has 301 scans
    List<MsScan> scans = rawFile.getScans();

    // 1st scan, #1000
    MsScan scan1 = scans.get(0);
    Assert.assertEquals(new Integer(1000), scan1.getScanNumber());
    Assert.assertEquals(new Integer(2), scan1.getMsLevel());
    Assert.assertEquals(1596.72f, scan1.getRetentionTime(), 0.01f);
    Assert.assertEquals(PolarityType.POSITIVE, scan1.getPolarity());
    scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Float scan1MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());

    // 300th scan, #1299
    MsScan scan299 = scans.get(299);
    scan299.getMzValues();
    intensityBuffer = scan299.getIntensityValues();
    Float scan299MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan299.getNumberOfDataPoints());

    rawFile.dispose();

  }

  public static void testA10A2() throws MSDKException {

    // Create the data structures
    double mzBuffer[];
    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "A1-0_A2.mzXML");
    Assert.assertTrue(inputFile.canRead());
    MzXMLFileImportMethod importer = new MzXMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();

    // The file has 1 scan
    List<MsScan> scans = rawFile.getScans();

    // 1st scan, #1
    MsScan scan1 = scans.get(0);
    mzBuffer = scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Float scan1MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());

    rawFile.dispose();

  }

  public static void testR1RG59B41() throws MSDKException {

    // Create the data structures
    double mzBuffer[];
    float intensityBuffer[];

    // Import the file
    File inputFile = new File(TEST_DATA_PATH + "R1_RG59_B4_1.mzXML");
    Assert.assertTrue(inputFile.canRead());
    MzXMLFileImportMethod importer = new MzXMLFileImportMethod(inputFile);
    RawDataFile rawFile = importer.execute();

    // The file has 301 scans
    List<MsScan> scans = rawFile.getScans();

    // 1st scan, #1000
    MsScan scan1 = scans.get(0);
    mzBuffer = scan1.getMzValues();
    intensityBuffer = scan1.getIntensityValues();
    Float scan1MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan1.getNumberOfDataPoints());

    // 300th scan, #1299
    MsScan scan299 = scans.get(299);
    mzBuffer = scan299.getMzValues();
    intensityBuffer = scan299.getIntensityValues();
    Float scan299MaxInt =
        MsSpectrumUtil.getMaxIntensity(intensityBuffer, scan299.getNumberOfDataPoints());

    rawFile.dispose();

  }

}
