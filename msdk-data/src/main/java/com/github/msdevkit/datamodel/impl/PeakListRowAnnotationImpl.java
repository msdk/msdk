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

import java.net.URL;

import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

import com.github.msdevkit.datamodel.PeakListRowAnnotation;

/**
 * Simple PeakIdentity implementation;
 */
public class PeakListRowAnnotationImpl implements PeakListRowAnnotation {

    @Override
    public IAtomContainer getChemicalStructure() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setChemicalStructure(@Nullable IAtomContainer structure) {
	// TODO Auto-generated method stub

    }

    @Override
    public IMolecularFormula getFormula() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setFormula(@Nullable IMolecularFormula formula) {
	// TODO Auto-generated method stub

    }

    @Override
    public String getDescription() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setDescription(@Nullable String description) {
	// TODO Auto-generated method stub

    }

    @Override
    public String getIdentificationMethod() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setIdentificationMethod(@Nullable String idMethod) {
	// TODO Auto-generated method stub

    }

    @Override
    public String getDataBaseId() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setDataBaseId(@Nullable String dbId) {
	// TODO Auto-generated method stub

    }

    @Override
    public URL getAccessionURL() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setAccessionURL(@Nullable URL dbURL) {
	// TODO Auto-generated method stub

    }

}
