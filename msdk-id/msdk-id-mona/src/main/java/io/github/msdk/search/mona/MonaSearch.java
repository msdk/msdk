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

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.identification.Search;
import io.github.msdk.search.mona.pojo.Result;
import io.github.msdk.search.mona.pojo.SimilaritySearchQuery;
import io.github.msdk.search.mona.pojo.SimilaritySearchResult;


/**
 * MoNA specific implementation of the Search Interface
 */
public class MonaSearch implements Search, MonaConfiguration {
    /** {@inheritDoc} */
    @Override
    public MsSpectrum findSpectrumById(long id) throws IOException {
        return new MonaSpectrum(id);
    }

    /**
     * {@inheritDoc}
     *
     * returns a mass spectra by it's property name
     */
    @Override
    public @Nonnull Iterator<MsSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) throws IOException {
        return getMsSpectrumIterator(null);
    }

    /**
     * {@inheritDoc}
     *
     * searches for similar spectra based on an existing mona id
     */
    public Iterator<MsSpectrum> findSimilarSpectra(long id, Integer minSimilarity) throws IOException {
        SimilaritySearchQuery query = new SimilaritySearchQuery();
        query.setMinSimilarity(minSimilarity);
        query.setSpectra(String.valueOf(id));
        return getMsSpectrumIterator(query);
    }

    /**
     * {@inheritDoc}
     *
     * searches for similar spectra, based on the provided Spectrum
     */
    @Override
    public @Nonnull Iterator<MsSpectrum> findSimilarSpectra(MsSpectrum compare, Integer minSimilarity) throws IOException  {
        SimilaritySearchQuery query = buildQuery(compare, minSimilarity);
        return getMsSpectrumIterator(query);
    }

    /**
     * runs the actual query against the server and provides us with an iterator to access the results
     * @param query
     * @return
     */
    private @Nonnull Iterator<MsSpectrum> getMsSpectrumIterator(SimilaritySearchQuery query) throws IOException {
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
        final Iterator<MsSpectrum> iterator = new Iterator<MsSpectrum>() {
            @Override
            public boolean hasNext() {
                return resultIterator.hasNext();
            }

            @Override
            public MsSpectrum next() {
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
    private SimilaritySearchQuery buildQuery(MsSpectrum compare, Integer minSimilarity) {
        //builds a standard mona spectra string ion:intensity ion:intensity
        StringBuffer spectraString = new StringBuffer();

        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        compare.getDataPoints(dataPoints);
        
        for (int i = 0; i < dataPoints.getSize(); i++) {
            spectraString.append(dataPoints.getMzBuffer()[i]);
            spectraString.append(":");
            spectraString.append(dataPoints.getIntensityBuffer()[i]);
            spectraString.append(" ");
        }

        //build our query object
        SimilaritySearchQuery query = new SimilaritySearchQuery();
        query.setSpectra(spectraString.toString().trim());
        query.setMinSimilarity(minSimilarity);
        return query;
    }
}
