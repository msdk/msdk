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

package io.github.msdk.featdet.gridmass;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * <p>GridMassMethod class.</p>
 */
public class GridMassMethod implements MSDKMethod<List<Chromatogram>> {

    private final @Nonnull RawDataFile rawDataFile;

    private List<Chromatogram> result;
    private boolean canceled = false;
    private int processedScans = 0, totalScans = 0;

    /**
     * <p>Constructor for GridMassMethod.</p>
     *
     * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     */
    public GridMassMethod(@Nonnull RawDataFile rawDataFile) {
        this.rawDataFile = rawDataFile;

    }

    /** {@inheritDoc} */
    @Override
    public List<Chromatogram> execute() throws MSDKException {

        result = new ArrayList<Chromatogram>();

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
    public List<Chromatogram> getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }

}
