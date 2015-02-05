/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.io.rawdataimport;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Test;

import com.github.msdevkit.datamodel.RawDataFile;

public class RawDataimportTest {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
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
    
    @Test
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
