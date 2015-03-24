package edu.ucdavis.fiehnlab.mona;

import io.github.msdk.datamodel.rawdata.IMassSpectrum;
import io.github.msdk.query.ISearch;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * implementation of the search interface, with mona specific options
 * Created by Gert on 3/23/2015.
 */
public class MonaSearch implements ISearch {
    @Override
    public IMassSpectrum findSpectrumById(long id) throws IOException {
        return new MonaSpectrum(id);
    }

    @Override
    public Iterator<IMassSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) {
        return null;
    }

    @Override
    public Iterator<IMassSpectrum> findSimilarSpectra(IMassSpectrum compare, Double minSimilarity) {
        return null;
    }
}
