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
     * <p>getChemicalStructure.</p>
     *
     * @return Chemical structure of this annotation.
     */
    @Nullable
    IAtomContainer getChemicalStructure();

    /**
     * Sets a new chemical structure to this annotation.
     *
     * @param chemicalStructure a {@link org.openscience.cdk.interfaces.IAtomContainer} object.
     */
    void setChemicalStructure(@Nullable IAtomContainer chemicalStructure);

    /**
     * <p>getFormula.</p>
     *
     * @return Chemical formula of this annotation.
     * @see #setFormula(IMolecularFormula)
     */
    @Nullable
    IMolecularFormula getFormula();

    /**
     * Sets a new chemical structure annotation to this annotation. Chemical
     * formula annotation should include charge, e.g. C6H13O6+ or
     * [C34H58N5O35P3]2-.
     *
     * @param formula a {@link org.openscience.cdk.interfaces.IMolecularFormula} object.
     */
    void setFormula(@Nullable IMolecularFormula formula);

    /**
     * <p>getIonType.</p>
     *
     * @return Ionization type for this annotation.
     */
    @Nullable
    IonType getIonType();

    /**
     * Sets a new ionization type to this annotation.
     *
     * @param ionType a {@link io.github.msdk.datamodel.ionannotations.IonType} object.
     */
    void setIonType(@Nullable IonType ionType);

    /**
     * <p>getExpectedMz.</p>
     *
     * @return Expected m/z value of this annotation.
     */
    @Nullable
    Double getExpectedMz();

    /**
     * Sets the expected m/z value to this annotation. The expected m/z value
     * can be the calculated value based on the chemical formula or a value from
     * a database.
     *
     * @param expectedMz a {@link java.lang.Double} object.
     */
    void setExpectedMz(@Nullable Double expectedMz);

    /**
     * <p>getDescription.</p>
     *
     * @return Textual description of this annotation.
     */
    @Nullable
    String getDescription();

    /**
     * Sets a new description to this annotation.
     *
     * @param description a {@link java.lang.String} object.
     */
    void setDescription(@Nullable String description);

    /**
     * <p>getIdentificationMethod.</p>
     *
     * @return Identification method (e.g. database name) of this annotation.
     */
    @Nullable
    String getIdentificationMethod();

    /**
     * Sets a new identification method to this annotation.
     *
     * @param identificationMethod a {@link java.lang.String} object.
     */
    void setIdentificationMethod(@Nullable String identificationMethod);

    /**
     * <p>getAnnotationId.</p>
     *
     * @return The id of this annotation..
     */
    @Nullable
    String getAnnotationId();

    /**
     * Sets a new annotation id this annotation.
     *
     * @param annotationId a {@link java.lang.String} object.
     */
    void setAnnotationId(@Nullable String annotationId);

    /**
     * <p>getAccessionURL.</p>
     *
     * @return Accession URL for a database, if this annotation comes from a
     *         database.
     */
    @Nullable
    URL getAccessionURL();

    /**
     * Sets a new accession URL to this annotation.
     *
     * @param dbURL a {@link java.net.URL} object.
     */
    void setAccessionURL(@Nullable URL dbURL);

    /**
     * <p>getChromatographyInfo.</p>
     *
     * @return Chromatography info.
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Sets a new chromatography info to this annotation.
     *
     * @param chromatographyInfo a {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo} object.
     */
    void setChromatographyInfo(@Nullable ChromatographyInfo chromatographyInfo);

}
