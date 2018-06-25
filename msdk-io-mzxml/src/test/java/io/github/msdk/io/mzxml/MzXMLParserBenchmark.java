/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.io.mzxml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;

public class MzXMLParserBenchmark {

  public static void main(String[] args) throws MSDKException, NumberFormatException {
    int numberOfRuns = 10;
    Logger logger = LoggerFactory.getLogger(MzXMLParserBenchmark.class);

    if (args.length > 0)
      numberOfRuns = Integer.valueOf(args[0]);

    MzXMLParserTest parserTest = new MzXMLParserTest();
    MzXMLFileImportMethodTest importTest = new MzXMLFileImportMethodTest();

    long startTime1 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      parserTest.testA10A2();
    long endTime1 = System.currentTimeMillis();

    long startTime2 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      parserTest.testR1RG59B41();
    long endTime2 = System.currentTimeMillis();

    long startTime3 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importTest.testA10A2();
    long endTime3 = System.currentTimeMillis();

    long startTime4 = System.currentTimeMillis();
    for (int i = 0; i < numberOfRuns; i++)
      importTest.testR1RG59B41();
    long endTime4 = System.currentTimeMillis();

    logger.debug("Average run time with MzXMLFileParser File 1: "
        + (endTime1 - startTime1) / numberOfRuns + "ms");
    logger.debug("Average run time with MzXMLFileParser File 2: "
        + (endTime2 - startTime2) / numberOfRuns + "ms");
    logger.debug("Average run time with MzXMLFileImportMethod File 1: "
        + (endTime3 - startTime3) / numberOfRuns + "ms");
    logger.debug("Average run time with MzXMLFileImportMethod File 2: "
        + (endTime4 - startTime4) / numberOfRuns + "ms");

  }

}
