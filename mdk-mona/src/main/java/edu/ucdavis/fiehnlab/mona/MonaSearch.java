package edu.ucdavis.fiehnlab.mona;

import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.query.Search;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * implementation of the search interface, with mona specific options
 * Created by Gert on 3/23/2015.
 */
public class MonaSearch implements Search {
    @Override
    public MassSpectrum findSpectrumById(long id) throws IOException {
        return new MonaSpectrum(id);
    }

    @Override
    public Iterator<MassSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) {
        return null;
    }

    @Override
    public Iterator<MassSpectrum> findSimilarSpectra(MassSpectrum compare, Double minSimilarity) {
        return null;
    }
}
