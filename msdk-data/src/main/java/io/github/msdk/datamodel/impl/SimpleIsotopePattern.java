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

package io.github.msdk.datamodel.impl;

import io.github.msdk.datamodel.peaklists.IsotopePattern;
import io.github.msdk.datamodel.peaklists.IsotopePatternType;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.DataPointList;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IMolecularFormula;

import com.google.common.collect.Range;

/**
 * Simple implementation of IsotopePattern interface
 */
public class SimpleIsotopePattern extends AbstractSpectrum implements
        IsotopePattern {

    private @Nonnull IsotopePatternType status = IsotopePatternType.UNKNOWN;
    private @Nonnull String description = "";

    SimpleIsotopePattern(@Nonnull DataPointStore dataPointStore) {
        super(dataPointStore);
    }

    @Override
    public @Nonnull String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Isotope pattern: " + description;
    }

    @Override
    public void setDescription(@Nonnull String description) {
        this.description = description;
    }

    @Override
    public @Nonnull IsotopePatternType getType() {
        // TODO Auto-generated method stub
        return IsotopePatternType.DETECTED;
    }

    @Override
    public IMolecularFormula getChemicalFormula() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setChemicalFormula(@Nullable IMolecularFormula formula) {
        // TODO Auto-generated method stub

    }

    @Override
    // @Nonnull
    public DataPointList getDataPoints() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getDataPoints(DataPointList list) {
        // TODO Auto-generated method stub

    }

    @Override
    // @Nonnull
    public DataPointList getDataPointsByMass(@Nonnull Range<Double> mzRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    // @Nonnull
    public List<DataPoint> getDataPointsByIntensity(
            @Nonnull Range<Float> intensityRange) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDataPoints(@Nonnull DataPointList newDataPoints) {
        // TODO Auto-generated method stub

    }

    @Override
    @Nonnull
    public Float getTIC() {
        // TODO Auto-generated method stub
        return null;
    }

}