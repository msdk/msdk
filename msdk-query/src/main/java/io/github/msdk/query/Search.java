package io.github.msdk.query;

import io.github.msdk.datamodel.rawdata.MassSpectrum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * This provides the system with easy access to query data from external resources providing MassSpectra. Example ideas for implementation would be a MSP file based library, a third party REST service or other
 * massspectral repositories
 */
public interface Search {

    /**
     * finds a spectrum by it's provided id.
     * @param id
     * @return
     */
    @Nullable
    MassSpectrum findSpectrumById(long id) throws IOException;

    /**
     * query by a specific property name and value
     * @param propertyName
     * @param propertyValue
     * @return
     */
    @Nonnull
    Iterator<MassSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) throws IOException;

    /**
     * searches for similar spectra
     * @param compare
     * @param minSimilarity
     * @return
     */
    @Nonnull
    Iterator<MassSpectrum> findSimilarSpectra(MassSpectrum compare,Integer minSimilarity) throws IOException;
}
