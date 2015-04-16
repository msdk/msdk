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

package io.github.msdk.datamodel;

import io.github.msdk.datamodel.peaklists.PeakListRowAnnotation;

import java.net.URL;

import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

/**
 * Simple PeakIdentity implementation;
 */
class SimplePeakListRowAnnotation implements PeakListRowAnnotation {

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
