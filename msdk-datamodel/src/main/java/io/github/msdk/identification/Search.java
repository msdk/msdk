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

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.msspectra.MsSpectrum;

/**
 * This provides the system with easy access to query data from external resources providing MassSpectra. Example ideas for implementation would be a MSP file based library, a third party REST service or other
 * massspectral repositories
 */
public interface Search {

    /**
     * finds a spectrum by it's provided id.
     *
     * @param id a long.
     * @return a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     * @throws java.io.IOException if any.
     */
    @Nullable
    MsSpectrum findSpectrumById(long id) throws IOException;

    /**
     * query by a specific property name and value
     *
     * @param propertyName a {@link java.lang.String} object.
     * @param propertyValue a {@link java.io.Serializable} object.
     * @return a {@link java.util.Iterator} object.
     * @throws java.io.IOException if any.
     */
    @Nonnull
    Iterator<MsSpectrum> findSpectrumByProperty(String propertyName, Serializable propertyValue) throws IOException;

    /**
     * searches for similar spectra
     *
     * @param compare a {@link io.github.msdk.datamodel.msspectra.MsSpectrum} object.
     * @param minSimilarity a {@link java.lang.Integer} object.
     * @return a {@link java.util.Iterator} object.
     * @throws java.io.IOException if any.
     */
    @Nonnull
    Iterator<MsSpectrum> findSimilarSpectra(MsSpectrum compare,Integer minSimilarity) throws IOException;
}
