
package edu.ucdavis.fiehnlab.mona;

import edu.ucdavis.fiehnlab.mona.pojo.Result;
import edu.ucdavis.fiehnlab.mona.pojo.SimilaritySearchQuery;
import edu.ucdavis.fiehnlab.mona.pojo.SimilaritySearchResult;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.query.Search;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.filter.LoggingFilter;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;

import java.io.Serializable;
import java.util.*;


/**
 * implementation of the search interface, with mona specific options
 */
public class MonaSearch implements Search, MonaConfiguration {
    @Override
    public MassSpectrum findSpectrumById(long id) throws IOException {
        return new MonaSpectrum(id);
    }

    @Override
    public Iterator<MassSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) {
        return null;
    }

    @Override
    public Iterator<MassSpectrum> findSimilarSpectra(MassSpectrum compare, Integer minSimilarity) {
        SimilaritySearchQuery query = buildQuery(compare, minSimilarity);


        //create a client and send data to the server
        final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).register(new LoggingFilter()).build();

        WebTarget target = client.target(MONA_URL).path("/rest/spectra/similarity");
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(query, MediaType.APPLICATION_JSON));

        SimilaritySearchResult results = response.readEntity(SimilaritySearchResult.class);

        //fetch results
        final Iterator<Result> resultIterator = results.getResult().iterator();

        /**
         * wrapper to provide us with an iterator
         */
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

    /**
     * builds our similarity query search object
     * @param compare
     * @param minSimilarity
     * @return
     */
    private SimilaritySearchQuery buildQuery(MassSpectrum compare, Integer minSimilarity) {
        //builds a standard mona spectra string ion:intensity ion:intensity
        StringBuffer spectraString = new StringBuffer();

        for (DataPoint p : compare.getDataPoints()) {
            spectraString.append(p.getMz());
            spectraString.append(":");
            spectraString.append(p.getIntensity());
            spectraString.append(" ");
        }

        //build our query object
        SimilaritySearchQuery query = new SimilaritySearchQuery();
        query.setSpectra(spectraString.toString().trim());
        query.setMinSimilarity(minSimilarity);
        return query;
    }
}
