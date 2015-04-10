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

package io.github.msdk.identification;

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
