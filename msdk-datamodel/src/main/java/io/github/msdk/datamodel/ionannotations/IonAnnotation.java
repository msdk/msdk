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

package io.github.msdk.datamodel.ionannotations;

import java.net.URL;

import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * Annotation of a detected feature with a chemical structure, formula, or
 * textual description.
 */
public interface IonAnnotation extends Comparable<IonAnnotation> {

    /**
     * @return Chemical structure of this annotation.
     */
    @Nullable
    IAtomContainer getChemicalStructure();

    /**
     * Sets a new chemical structure to this annotation.
     */
    void setChemicalStructure(@Nullable IAtomContainer chemicalStructure);

    /**
     * @return Chemical formula of this annotation.
     * @see #setFormula(String)
     */
    @Nullable
    IMolecularFormula getFormula();

    /**
     * Sets a new chemical structure annotation to this annotation. Chemical
     * formula annotation should include charge, e.g. C6H13O6+ or
     * [C34H58N5O35P3]2-.
     */
    void setFormula(@Nullable IMolecularFormula formula);

    /**
     * @return Ionization type for this annotation.
     */
    @Nullable
    IonType getIonType();

    /**
     * Sets a new ionization type to this annotation.
     */
    void setIonType(@Nullable IonType ionType);

    /**
     * @return Expected m/z value of this annotation.
     */
    @Nullable
    Double getExpectedMz();

    /**
     * Sets the expected m/z value to this annotation. The expected m/z value
     * can be the calculated value based on the chemical formula or a value from
     * a database.
     */
    void setExpectedMz(@Nullable Double expectedMz);

    /**
     * @return Textual description of this annotation.
     */
    @Nullable
    String getDescription();

    /**
     * Sets a new description to this annotation.
     */
    void setDescription(@Nullable String description);

    /**
     * @return Identification method (e.g. database name) of this annotation.
     */
    @Nullable
    String getIdentificationMethod();

    /**
     * Sets a new identification method to this annotation.
     */
    void setIdentificationMethod(@Nullable String identificationMethod);

    /**
     * @return The id of this annotation..
     */
    @Nullable
    String getAnnotationId();

    /**
     * Sets a new annotation id this annotation.
     */
    void setAnnotationId(@Nullable String annotationId);

    /**
     * @return Accession URL for a database, if this annotation comes from a
     *         database.
     */
    @Nullable
    URL getAccessionURL();

    /**
     * Sets a new accession URL to this annotation.
     */
    void setAccessionURL(@Nullable URL dbURL);

    /**
     * @return Chromatography info.
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Sets a new chromatography info to this annotation.
     */
    void setChromatographyInfo(@Nullable ChromatographyInfo chromatographyInfo);

}
