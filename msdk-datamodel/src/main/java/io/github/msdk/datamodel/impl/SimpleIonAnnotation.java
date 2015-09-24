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

import java.net.URL;

import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * Simple IonAnnotation implementation;
 */
class SimpleIonAnnotation implements IonAnnotation {

    private @Nullable IAtomContainer chemicalStructure;
    private @Nullable IMolecularFormula formula;
    private @Nullable IonType ionType;
    private @Nullable Double expectedMz;
    private @Nullable String description;
    private @Nullable String identificationMethod;
    private @Nullable String annotationId;
    private @Nullable URL accessionURL;
    private @Nullable ChromatographyInfo chromatographyInfo;

    @Override
    @Nullable
    public IAtomContainer getChemicalStructure() {
        return chemicalStructure;
    }

    @Override
    public void setChemicalStructure(
            @Nullable IAtomContainer chemicalStructure) {
        this.chemicalStructure = chemicalStructure;
    }

    @Override
    @Nullable
    public IMolecularFormula getFormula() {
        return formula;
    }

    @Override
    public void setFormula(@Nullable IMolecularFormula formula) {
        this.formula = formula;
    }

    @Override
    @Nullable
    public IonType getIonType() {
        return ionType;
    }

    @Override
    public void setIonType(@Nullable IonType ionType) {
        this.ionType = ionType;
    }

    @Override
    @Nullable
    public Double getExpectedMz() {
        return expectedMz;
    }

    @Override
    public void setExpectedMz(@Nullable Double expectedMz) {
        this.expectedMz = expectedMz;
    }

    @Override
    @Nullable
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Override
    @Nullable
    public String getIdentificationMethod() {
        return identificationMethod;
    }

    @Override
    public void setIdentificationMethod(@Nullable String identificationMethod) {
        this.identificationMethod = identificationMethod;
    }

    @Override
    @Nullable
    public String getAnnotationId() {
        return annotationId;
    }

    @Override
    public void setAnnotationId(@Nullable String annotationId) {
        this.annotationId = annotationId;
    }

    @Override
    @Nullable
    public URL getAccessionURL() {
        return accessionURL;
    }

    @Override
    public void setAccessionURL(@Nullable URL accessionURL) {
        this.accessionURL = accessionURL;
    }

    @Override
    public int compareTo(IonAnnotation i) {
        int returnValue;

        final String thisDescription = this.description;
        final String thisAnnotationId = this.annotationId;

        // 1. Compare description
        if (thisDescription != null && i.getDescription() != null) {
            returnValue = thisDescription.compareTo(i.getDescription());
        } else if (thisDescription == null && i.getDescription() == null) {
            returnValue = 0;
        } else if (thisDescription == null) {
            returnValue = 1;
        } else {
            returnValue = -1;
        }

        // 2. Compare annotation id
        if (returnValue == 0) {
            if (thisAnnotationId != null && i.getAnnotationId() != null) {
                returnValue = thisAnnotationId.compareTo(i.getAnnotationId());
            } else
                if (thisAnnotationId == null && i.getAnnotationId() == null) {
                returnValue = 0;
            } else if (thisAnnotationId == null) {
                returnValue = 1;
            } else {
                returnValue = -1;
            }
        }

        return returnValue;
    }

    @Override
    @Nullable
    public ChromatographyInfo getChromatographyInfo() {
        return chromatographyInfo;
    }

    @Override
    public void setChromatographyInfo(
            @Nullable ChromatographyInfo chromatographyInfo) {
        this.chromatographyInfo = chromatographyInfo;
    }
}
