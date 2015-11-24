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

package io.github.msdk.search.mona;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;

import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.identification.Search;

/**
 */
public class MonaSearchTest {

    /**
     * reference id to test
     */
    private static final long TEST_ID = 3841762;

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testFindSpectrumById() throws Exception {

        Search search = new MonaSearch();

        MsSpectrum result = search.findSpectrumById(TEST_ID);

        assertTrue(result != null);

    }

    @Ignore
    @Test
    public void testFindSpectrumByProperty() throws Exception {
        fail("todo!");
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testFindSimilarSpectra() throws Exception {

        Search search = new MonaSearch();

        Iterator<MsSpectrum> result = search
                .findSimilarSpectra(new MonaSpectrum(TEST_ID), 900);

        int count = 0;

        while (result.hasNext()) {
            result.next();
            count++;
        }

        assertTrue(count != 0);
    }

    @Ignore("Ignored because MoNA API is throwing HTTP 500 error")
    @Test
    public void testFindSimilarSpectraById() throws Exception {

        MonaSearch search = new MonaSearch();

        Iterator<MsSpectrum> result = search.findSimilarSpectra(TEST_ID, 900);

        int count = 0;

        while (result.hasNext()) {
            result.next();
            count++;
        }

        assertTrue(count != 0);
    }
}
