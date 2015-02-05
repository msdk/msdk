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

package com.github.msdevkit.io.spectrumtypedetection;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import com.github.msdevkit.datamodel.MassSpectrumType;
import com.github.msdevkit.datamodel.MsScan;
import com.github.msdevkit.datamodel.RawDataFile;
import com.github.msdevkit.io.rawdataimport.RawDataFileImportAlgorithm;

public class SpectrumTypeDetectionAlgorithmTest {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Test the SpectrumTypeDetectionAlgorithm
     */
    @Test
    public void testSpectrumTypeDetectionAlgorithm() throws Exception {

	File inputFiles[] = new File("src/test/resources/spectrumtypedetection")
		.listFiles();

	Assert.assertNotNull(inputFiles);
	Assert.assertNotEquals(0, inputFiles.length);

	int filesTested = 0;

	for (File inputFile : inputFiles) {

	    MassSpectrumType trueType;
	    if (inputFile.getName().startsWith("centroided"))
		trueType = MassSpectrumType.CENTROIDED;
	    else if (inputFile.getName().startsWith("thresholded"))
		trueType = MassSpectrumType.THRESHOLDED;
	    else if (inputFile.getName().startsWith("profile"))
		trueType = MassSpectrumType.PROFILE;
	    else
		continue;

	    logger.info("Checking autodetection of centroided/thresholded/profile scans on file "
		    + inputFile.getName());

	    RawDataFileImportAlgorithm importer = new RawDataFileImportAlgorithm(
		    inputFile);
	    importer.execute();
	    RawDataFile rawFile = importer.getResult();

	    Assert.assertNotNull(rawFile);

	    for (MsScan scan : rawFile.getScans()) {
		SpectrumTypeDetectionAlgorithm detector = new SpectrumTypeDetectionAlgorithm(
			scan);
		detector.execute();
		MassSpectrumType detectedType = detector.getResult();
		Assert.assertNotNull(detectedType);

		Assert.assertEquals("Scan type wrongly detected for scan "
			+ scan.getScanNumber() + " in " + rawFile.getName(),
			trueType, detectedType);
	    }
	    filesTested++;
	}

	// make sure we tested 10+ files
	Assert.assertTrue(filesTested > 10);
    }

}
