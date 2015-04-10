
package edu.ucdavis.fiehnlab.mona;

import edu.ucdavis.fiehnlab.mona.pojo.Result;
import edu.ucdavis.fiehnlab.mona.pojo.SimilaritySearchQuery;
import edu.ucdavis.fiehnlab.mona.pojo.SimilaritySearchResult;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrum;
import io.github.msdk.identification.Search;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;


/**
 * MoNA specific implementation of the Search Interface
 */
public class MonaSearch implements Search, MonaConfiguration {
    @Override
    public MassSpectrum findSpectrumById(long id) throws IOException {
        return new MonaSpectrum(id);
    }

    /**
     * returns a mass spectra by it's property name
     * @param propertyName
     * @param propertyValue
     * @return
     */
    @Override
    public Iterator<MassSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) {
        return null;
    }

    /**
     * searches for similar spectra based on an existing mona id
     * @param id
     * @param minSimilarity
     * @return
     */
    public Iterator<MassSpectrum> findSimilarSpectra(long id, Integer minSimilarity) throws IOException {
        SimilaritySearchQuery query = new SimilaritySearchQuery();
        query.setMinSimilarity(minSimilarity);
        query.setSpectra(String.valueOf(id));
        return getMassSpectrumIterator(query);
    }

    /**
     * searches for similar spectra, based on the provided Spectrum
     * @param compare
     * @param minSimilarity
     * @return
     * @throws IOException
     */
    @Override
    public Iterator<MassSpectrum> findSimilarSpectra(MassSpectrum compare, Integer minSimilarity) throws IOException  {
        SimilaritySearchQuery query = buildQuery(compare, minSimilarity);
        return getMassSpectrumIterator(query);
    }

    /**
     * runs the actual query against the server and provides us with an iterator to access the results
     * @param query
     * @return
     */
    private Iterator<MassSpectrum> getMassSpectrumIterator(SimilaritySearchQuery query) throws IOException {
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
