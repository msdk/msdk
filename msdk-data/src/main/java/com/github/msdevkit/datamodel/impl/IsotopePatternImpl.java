/* 
 * Copyright 2015 MSDK Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.github.msdevkit.datamodel.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IMolecularFormula;

import com.github.msdevkit.datamodel.IsotopePattern;
import com.github.msdevkit.datamodel.IsotopePatternType;

/**
 * Simple implementation of IsotopePattern interface
 */
public class IsotopePatternImpl extends SpectrumImpl implements IsotopePattern {

    private @Nonnull IsotopePatternType status = IsotopePatternType.UNKNOWN;
    private @Nonnull String description = "";

    IsotopePatternImpl(@Nonnull DataPointStoreImpl dataPointStore) {
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

}