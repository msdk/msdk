/* (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.msp;

import java.util.Hashtable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.impl.AbstractSpectrum;

/**
 * <p>MspSpectrum class.</p>
 *
 */
public class MspSpectrum extends AbstractSpectrum {

    private final @Nonnull Hashtable<String, String> properties = new Hashtable<>();

    MspSpectrum(@Nonnull DataPointStore dataStore) {
        super(dataStore);
    }

    /**
     * <p>getProperty.</p>
     *
     * @param propName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public @Nullable String getProperty(@Nonnull String propName) {
        return properties.get(propName);
    }

    /**
     * <p>setProperty.</p>
     *
     * @param propName a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setProperty(@Nonnull String propName, @Nullable String value) {
        properties.put(propName, value);
    }

}
