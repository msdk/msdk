package edu.ucdavis.fiehnlab.mona;

import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.query.Search;
import org.junit.Test;

import java.util.Iterator;

import static junit.framework.TestCase.*;

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/31/15
 * Time: 12:15 PM
 */
public class MonaSearchTest {

    private static final int TEST_ID = 3841762;

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
            count++;
        }

        assertTrue(count != 0);
    }
}
