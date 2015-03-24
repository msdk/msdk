package io.github.msdk.query;

import io.github.msdk.datamodel.rawdata.IMassSpectrum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * simple query interface
 * Created by Gert on 3/23/2015.
 */
public interface ISearch {

    /**
     * finds a spectrum by it's provided id.
     * @param id
     * @return
     */
    @Nullable
    IMassSpectrum findSpectrumById(long id) throws IOException;

    /**
     * query by a specific property name and value
     * @param propertyName
     * @param propertyValue
     * @return
     */
    @Nonnull
    Iterator<IMassSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue);

    /**
     * searches for similar spectra
     * @param compare
     * @param minSimilarity
     * @return
     */
    @Nonnull
    Iterator<IMassSpectrum> findSimilarSpectra(IMassSpectrum compare,Double minSimilarity);
}
