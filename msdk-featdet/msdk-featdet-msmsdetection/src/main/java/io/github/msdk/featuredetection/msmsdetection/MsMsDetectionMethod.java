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

package io.github.msdk.featuredetection.msmsdetection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

/**
 * This class creates a list of IonAnnotations for a RawDataFile based the
 * MS2 scans.
 */
public class MsMsDetectionMethod implements MSDKMethod<List<IonAnnotation>> {

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull DataPointStore dataPointStore;
    private final @Nonnull MZTolerance mzTolerance;
    private final @Nonnull RTTolerance rtTolerance;
    private final @Nonnull Double intensityTolerance;

    private List<IonAnnotation> result;
    private boolean canceled = false;
    private int processedScans = 0, totalScans = 0;

    /**
     * <p>Constructor for MsMsDetectionMethod.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     * @param dataPointStore a {@link io.github.msdk.datamodel.datapointstore.DataPointStore} object.
     * @param mzTolerance a {@link io.github.msdk.util.MZTolerance} object.
     * @param rtTolerance a {@link io.github.msdk.util.RTTolerance} object.
     * @param intensityTolerance a {@link java.lang.Double} object.
     */
    public MsMsDetectionMethod(	@Nonnull RawDataFile rawDataFile,
            @Nonnull DataPointStore dataPointStore,
            @Nonnull MZTolerance mzTolerance, @Nonnull RTTolerance rtTolerance,
            @Nonnull Double intensityTolerance) {
        this.rawDataFile = rawDataFile;
        this.dataPointStore = dataPointStore;
        this.mzTolerance = mzTolerance;
        this.rtTolerance = rtTolerance;
        this.intensityTolerance = intensityTolerance;
    }

    /** {@inheritDoc} */
    @Override
    public List<IonAnnotation> execute() throws MSDKException {

        result = new ArrayList<IonAnnotation>();

        /*
         * TODO
         */

        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) processedScans / totalScans;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public List<IonAnnotation> getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
