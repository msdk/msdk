package edu.ucdavis.fiehnlab.mona;

import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.identification.Search;

import org.junit.Test;

import java.util.Iterator;

import static junit.framework.TestCase.*;

/**
 */
public class MonaSearchTest {

    /**
     * reference id to test
     */
    private static final long TEST_ID = 3841762;

    @Test
    public void testFindSpectrumById() throws Exception {

        Search search = new MonaSearch();

        MassSpectrum result = search.findSpectrumById(TEST_ID);

        assertTrue(result != null);

    }

    @Test
    public void testFindSpectrumByProperty() throws Exception {
        fail("todo!");
    }

    @Test
    public void testFindSimilarSpectra() throws Exception {

        Search search = new MonaSearch();

        Iterator<MassSpectrum> result = search.findSimilarSpectra(new MonaSpectrum(TEST_ID), 900);

        int count = 0;

        while(result.hasNext()){
            result.next();
            count++;
        }

        assertTrue(count != 0);
    }

    @Test
    public void testFindSimilarSpectraById() throws Exception {

        MonaSearch search = new MonaSearch();

        Iterator<MassSpectrum> result = search.findSimilarSpectra(TEST_ID, 900);

        int count = 0;

        while(result.hasNext()){
            result.next();
            count++;
        }

        assertTrue(count != 0);
    }
}
