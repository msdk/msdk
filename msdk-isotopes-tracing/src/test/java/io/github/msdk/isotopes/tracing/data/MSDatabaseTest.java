/*
 * (C) Copyright 2015-2017 by MSDK Development Team
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

package io.github.msdk.isotopes.tracing.data;

import java.io.IOException;

import io.github.msdk.isotopes.tracing.data.constants.PathConstants;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class MSDatabaseTest extends TestCase {

  public void testParseCsv() {
    String filePath = PathConstants.TEST_RESOURCES.toAbsolutePath("MSDatabaseTest01.csv");
    MSDatabase msDatabase = new MSDatabase(filePath);
    assertEquals(0.8, msDatabase.getIncorporationRate());
    assertEquals("C2N", msDatabase.getIncorporatedTracers());
    assertEquals("C4H8NO2Si", msDatabase.getCompoundFormula());

    MassSpectrum naturalSpectrum = msDatabase.getNaturalSpectrum();
    assertEquals(3, naturalSpectrum.size());
    assertEquals(naturalSpectrum.get(130.0324), 0.917);
    assertEquals(naturalSpectrum.get(131.032), 0.0395);
    assertEquals(naturalSpectrum.get(132.0324), 0.025);
    assertEquals(naturalSpectrum.get(0.0), null);

    MassSpectrum markedSpectrum = msDatabase.getMarkedSpectrum();
    assertEquals(7, markedSpectrum.size());
    assertEquals(markedSpectrum.get(133.0362), 0.4765);
    assertEquals(markedSpectrum.get(134.0357), 0.013);
    assertEquals(markedSpectrum.get(134.0395), 0.2909);
    assertEquals(markedSpectrum.get(135.0391), 0.0216);
    assertEquals(markedSpectrum.get(135.0429), 0.1534);
    assertEquals(markedSpectrum.get(136.0395), 0.0139);
    assertEquals(markedSpectrum.get(136.0425), 0.0109);

    MassSpectrum mixedSpectrum = msDatabase.getMixedSpectrum();
    assertEquals(7, mixedSpectrum.size());
    assertEquals(mixedSpectrum.get(130.0324), 0.1833);
    assertEquals(mixedSpectrum.get(133.0362), 0.3812);
    assertEquals(mixedSpectrum.get(134.0395), 0.2327);
    assertEquals(mixedSpectrum.get(135.0391), 0.0173);
    assertEquals(mixedSpectrum.get(135.0429), 0.1227);
    assertEquals(mixedSpectrum.get(136.0395), 0.0111);

  }

  public void testWriteCsv() throws IOException {
    String filePath = PathConstants.TEST_RESOURCES.toAbsolutePath("MSDatabaseTest01.csv");
    MSDatabase msDatabase = new MSDatabase(filePath);
    msDatabase.writeCsv(PathConstants.TMP_FOLDER.toAbsolutePath());
  }

}
