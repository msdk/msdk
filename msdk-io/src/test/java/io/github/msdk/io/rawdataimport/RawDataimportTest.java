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

package io.github.msdk.io.rawdataimport;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import io.github.msdk.datamodel.rawdata.RawDataFile;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Ignore;
import org.junit.Test;

public class RawDataimportTest {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Ignore("not ready yet") @Test
    public void testXMLFileImport() throws Exception {

	File inputFiles[] = new File("src/test/resources/rawdataimport/xml")
		.listFiles();

	assertNotNull(inputFiles);
	assertNotEquals(0, inputFiles.length);

	int filesTested = 0;
	for (File inputFile : inputFiles) {
	    importFile(inputFile);
	    filesTested++;
	}

	// make sure we tested some files
	assertTrue(filesTested > 0);
    }
    
    @Ignore("not ready yet") @Test
    public void testCDFFileImport() throws Exception {

	File inputFiles[] = new File("src/test/resources/rawdataimport/netcdf")
		.listFiles();

	assertNotNull(inputFiles);
	assertNotEquals(0, inputFiles.length);

	int filesTested = 0;
	for (File inputFile : inputFiles) {
	    importFile(inputFile);
	    filesTested++;
	}

	// make sure we tested some files
	assertTrue(filesTested > 0);
    }

    @Test
    public void testThermoRawImport() throws Exception {
	
	// Run this test only on Windows
	assumeTrue(System.getProperty("os.name").startsWith("Windows"));

	File inputFiles[] = new File("src/test/resources/rawdataimport/thermo")
		.listFiles();

	assertNotNull(inputFiles);
	assertNotEquals(0, inputFiles.length);

	int filesTested = 0;
	for (File inputFile : inputFiles) {
	    importFile(inputFile);
	    filesTested++;
	}

	// make sure we tested some files
	assertTrue(filesTested > 0);
    }
    
    @Test
    public void testWatersRawImport() throws Exception {

	// Run this test only on Windows
	assumeTrue(System.getProperty("os.name").startsWith("Windows"));

	File inputFiles[] = new File("src/test/resources/rawdataimport/waters")
		.listFiles();

	assertNotNull(inputFiles);
	assertNotEquals(0, inputFiles.length);

	int filesTested = 0;
	for (File inputFile : inputFiles) {
	    importFile(inputFile);
	    filesTested++;
	}

	// make sure we tested some files
	// assertTrue(filesTested > 0);
    }

    private void importFile(File inputFile) throws Exception {
	
	logger.info("Checking import of file " + inputFile.getName());

	RawDataFileImportAlgorithm importer = new RawDataFileImportAlgorithm(
		inputFile);
	importer.execute();
	RawDataFile rawFile = importer.getResult();

	assertNotNull(rawFile);
	assertNotEquals(rawFile.getScans().size(), 0);

    }
}
