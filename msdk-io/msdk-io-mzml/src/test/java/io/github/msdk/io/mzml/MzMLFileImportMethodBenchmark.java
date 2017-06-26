package io.github.msdk.io.mzml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;

public class MzMLFileImportMethodBenchmark {

  public static void main(String[] args) throws MSDKException, NumberFormatException {

    int numberOfRuns = 10;
    Logger logger = LoggerFactory.getLogger(MzMLFileImportMethod.class);

    if (args.length > 0)
      numberOfRuns = Integer.valueOf(args[0]);

    MzMLFileImportMethodTest importerTest = new MzMLFileImportMethodTest();

    long startTime1 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.test5peptideFT();
    long endTime1 = System.currentTimeMillis();

    long startTime2 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testCompressedAndUncompressed();
    long endTime2 = System.currentTimeMillis();

    long startTime3 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testEmptyScan();
    long endTime3 = System.currentTimeMillis();

    long startTime4 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testFileWithUV();
    long endTime4 = System.currentTimeMillis();

    long startTime5 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testParamGroup();
    long endTime5 = System.currentTimeMillis();

    long startTime6 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testPwizTiny();
    long endTime6 = System.currentTimeMillis();

    long startTime7 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testSRM();
    long endTime7 = System.currentTimeMillis();

    long startTime8 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importerTest.testZlibAndNumpressCompression();;
    long endTime8 = System.currentTimeMillis();

    logger.info("Average run time with MzMLFileParser File 1: "
        + (endTime1 - startTime1) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 2: "
        + (endTime2 - startTime2) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 3: "
        + (endTime3 - startTime3) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 4: "
        + (endTime4 - startTime4) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 5: "
        + (endTime5 - startTime5) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 6: "
        + (endTime6 - startTime6) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 7: "
        + (endTime7 - startTime7) / numberOfRuns + "ms");
    logger.info("Average run time with MzMLFileParser File 8: "
        + (endTime8 - startTime8) / numberOfRuns + "ms");

  }

}
