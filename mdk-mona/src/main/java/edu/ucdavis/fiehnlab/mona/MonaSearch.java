package edu.ucdavis.fiehnlab.mona;

import edu.ucdavis.fiehnlab.mona.pojo.Result;
import edu.ucdavis.fiehnlab.mona.pojo.SimilaritySearch;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.query.Search;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * implementation of the search interface, with mona specific options
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

        StringBuffer spectraString = new StringBuffer();

        for(DataPoint p :compare.getDataPoints()){
            spectraString.append(p.getMz());
            spectraString.append(":");
            spectraString.append(p.getIntensity());
            spectraString.append(" ");
        }

        spectraString.trimToSize();


        SimilaritySearch similaritySearch = new SimilaritySearch();

        List<Result> results = similaritySearch.getResult();

        final Iterator<Result> resultIterator = results.iterator();

        final Iterator<MassSpectrum> iterator = new Iterator<MassSpectrum>() {
            @Override
            public boolean hasNext() {
                return resultIterator.hasNext();
            }

            @Override
            public MassSpectrum next() {
                try {
                    return findSpectrumById(resultIterator.next().getId());
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            @Override
            public void remove() {
                resultIterator.remove();
            }
        };

        return iterator;
    }
}
