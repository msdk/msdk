/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.features.normalization.compound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.util.FeatureTableUtil;

/**
 * This class creates a normalized feature table based on a feature table and a
 * set of features.
 */
public class FeatureNormalizationByCompoundMethod
        implements MSDKMethod<FeatureTable> {

    // Other variables
    private final @Nonnull FeatureTable featureTable;
    private final @Nonnull String nameSuffix;
    private final @Nonnull DataPointStore dataStore;
    private final @Nonnull FeatureTable result;
    private boolean canceled = false;
    private int processedFeatures = 0, totalFeatures = 0;

    /**
     * <p>
     * Constructor for FeatureFilterMethod.
     * </p>
     *
     * @param featureTable
     *            a {@link io.github.msdk.datamodel.featuretables.FeatureTable}
     *            object.
     * @param dataStore
     *            a {@link io.github.msdk.datamodel.datastore.DataPointStore}
     *            object.
     * @param nameSuffix
     *            a {@link java.lang.String} object.
     */
    public FeatureNormalizationByCompoundMethod(
            @Nonnull FeatureTable featureTable,
            @Nonnull DataPointStore dataStore, @Nonnull String nameSuffix) {
        this.featureTable = featureTable;
        this.dataStore = dataStore;
        this.nameSuffix = nameSuffix;

        // Make a copy of the feature table
        result = FeatureTableUtil.clone(dataStore, featureTable,
                featureTable.getName() + nameSuffix);
    }

    /** {@inheritDoc} */
    @Override
    public FeatureTable execute() throws MSDKException {
        // Total features
        totalFeatures = featureTable.getRows().size()
                * featureTable.getSamples().size();

        /*
         * TODO: Write method
         */

        // Return the new feature table
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalFeatures == 0 ? null
                : (float) processedFeatures / totalFeatures;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public FeatureTable getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
