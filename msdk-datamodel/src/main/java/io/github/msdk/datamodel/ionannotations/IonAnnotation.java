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

package io.github.msdk.datamodel.ionannotations;

import java.net.URL;

import javax.annotation.Nonnull;
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
     * <p>
     * getChemicalStructure.
     * </p>
     *
     * @return Chemical structure of this annotation.
     */
    @Nullable
    IAtomContainer getChemicalStructure();

    /**
     * Sets a new chemical structure to this annotation.
     *
     * @param chemicalStructure
     *            a {@link org.openscience.cdk.interfaces.IAtomContainer}
     *            object.
     */
    void setChemicalStructure(@Nullable IAtomContainer chemicalStructure);

    /**
     * <p>
     * getFormula.
     * </p>
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
     * @param formula
     *            a {@link org.openscience.cdk.interfaces.IMolecularFormula}
     *            object.
     */
    void setFormula(@Nullable IMolecularFormula formula);

    /**
     * <p>
     * getIonType.
     * </p>
     *
     * @return Ionization type for this annotation.
     */
    @Nullable
    IonType getIonType();

    /**
     * Sets a new ionization type to this annotation.
     *
     * @param ionType
     *            a {@link io.github.msdk.datamodel.ionannotations.IonType}
     *            object.
     */
    void setIonType(@Nullable IonType ionType);

    /**
     * <p>
     * getExpectedMz.
     * </p>
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
     * @param expectedMz
     *            a {@link java.lang.Double} object.
     */
    void setExpectedMz(@Nullable Double expectedMz);

    /**
     * <p>
     * getDescription.
     * </p>
     *
     * @return Textual description of this annotation.
     */
    @Nullable
    String getDescription();

    /**
     * Sets a new description to this annotation.
     *
     * @param description
     *            a {@link java.lang.String} object.
     */
    void setDescription(@Nullable String description);

    /**
     * <p>
     * getIdentificationMethod.
     * </p>
     *
     * @return Identification method (e.g. database name) of this annotation.
     */
    @Nullable
    String getIdentificationMethod();

    /**
     * Sets a new identification method to this annotation.
     *
     * @param identificationMethod
     *            a {@link java.lang.String} object.
     */
    void setIdentificationMethod(@Nullable String identificationMethod);

    /**
     * <p>
     * getAnnotationId.
     * </p>
     *
     * @return The id of this annotation..
     */
    @Nullable
    String getAnnotationId();

    /**
     * Sets a new annotation id this annotation.
     *
     * @param annotationId
     *            a {@link java.lang.String} object.
     */
    void setAnnotationId(@Nullable String annotationId);

    /**
     * <p>
     * getAccessionURL.
     * </p>
     *
     * @return Accession URL for a database, if this annotation comes from a
     *         database.
     */
    @Nullable
    URL getAccessionURL();

    /**
     * Sets a new accession URL to this annotation.
     *
     * @param dbURL
     *            a {@link java.net.URL} object.
     */
    void setAccessionURL(@Nullable URL dbURL);

    /**
     * <p>
     * getChromatographyInfo.
     * </p>
     *
     * @return Chromatography info.
     */
    @Nullable
    ChromatographyInfo getChromatographyInfo();

    /**
     * Sets a new chromatography info to this annotation.
     *
     * @param chromatographyInfo
     *            a {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            object.
     */
    void setChromatographyInfo(@Nullable ChromatographyInfo chromatographyInfo);

    /**
     * <p>
     * isNA.
     * </p>
     *
     * @return True if no data is associated to this annotation.
     */
    @Nonnull
    Boolean isNA();

    /**
     * <p>
     * getInchiKey
     * </p>
     *
     * @return InChI key of this annotation.
     */
    @Nullable
    String getInchiKey();

    /**
     * Sets a new InChI key to this annotation.
     *
     * @param inchiKey
     *            a {@link java.lang.String} object.
     */
    void setInchiKey(@Nullable String inchiKey);

    /**
     * <p>
     * getTaxId
     * </p>
     *
     * @return Taxonomy id for the species associated to this annotation.
     */
    @Nullable
    Integer getTaxId();

    /**
     * Sets a new taxonomy id from the NEWT taxonomy for the species associated
     * to this annotation.
     *
     * @param taxId
     *            a {@link java.lang.Integer} object.
     */
    void setTaxId(@Nullable Integer taxId);

    /**
     * <p>
     * getSpecies
     * </p>
     *
     * @return Textual species description of this annotation.
     */
    @Nullable
    String getSpecies();

    /**
     * Sets a new species description to this annotation
     *
     * @param species
     *            a {@link java.lang.String} object.
     */
    void setSpecies(@Nullable String species);

    /**
     * <p>
     * getDatabase
     * </p>
     *
     * @return Textual database description of this annotation.
     */
    @Nullable
    String getDatabase();

    /**
     * Sets a new database description to this annotation. Generally references
     * the used identification or spectral library.
     *
     * @param database
     *            a {@link java.lang.String} object.
     */
    void setDatabase(@Nullable String database);

    /**
     * <p>
     * getDatabaseVersion
     * </p>
     *
     * @return The version or date of creation of the used database.
     */
    @Nullable
    String getDatabaseVersion();

    /**
     * Sets a new database version description to this annotation. Either the
     * version of the used database if available or otherwise the date of
     * creation. Additionally, the number of entries in the database MAY be
     * reported in round brackets after the version in the format: {version}
     * ({#entries} entries), for example "2011-11 (1234 entries)".
     *
     * @param databaseVersion
     *            a {@link java.lang.String} object.
     */
    void setDatabaseVersion(@Nullable String databaseVersion);

    /**
     * <p>
     * getSpectraRef
     * </p>
     *
     * @return Textual reference to a spectrum in a spectrum file for this
     *         annotation.
     */
    @Nullable
    String getSpectraRef();

    /**
     * Sets a new textual reference to a spectrum in a spectrum file for this
     * annotation. The reference must be in the format
     * ms_run[1-n]:{SPECTRA_REF}. Multiple spectra can be referenced using a "|"
     * delimited list.
     *
     * @param spectraRef
     *            a {@link java.lang.String} object.
     */
    void setSpectraRef(@Nullable String spectraRef);

    /**
     * <p>
     * getSearchEngine
     * </p>
     *
     * @return Textual search engine description of this annotation.
     */
    @Nullable
    String getSearchEngine();

    /**
     * Sets a "|" delimited list of search engine(s) that identified this
     * annotation.
     *
     * @param searchEngine
     *            a {@link java.lang.String} object.
     */
    void setSearchEngine(@Nullable String searchEngine);

    /**
     * <p>
     * getBestSearchEngineScore
     * </p>
     *
     * @return The best search engine score for this annotation.
     */
    @Nullable
    Double getBestSearchEngineScore();

    /**
     * Sets the best search engine score for this annotation.
     *
     * @param bestSearchEngineScore
     *            a {@link java.lang.Double} object.
     */
    void setBestSearchEngineScore(@Nullable Double bestSearchEngineScore);

    /**
     * <p>
     * getModifications
     * </p>
     *
     * @return Textual description of the annotation's modifications.
     */
    @Nullable
    String getModifications();

    /**
     * Sets the annotation's modifications.
     *
     * @param modifications
     *            a {@link java.lang.String} object.
     */
    void setModifications(@Nullable String modifications);

    /**
     * <p>
     * getReliability
     * </p>
     *
     * @return The reliability of the ion annotation identification.
     */
    @Nullable
    Integer getReliability();

    /**
     * Sets the reliability of the identification. This must be reported as an
     * integer between 1-4:
     * 1: Identified metabolites
     * 2: Putatively annotated compounds
     * 3: Putatively characterized compound classes
     * 4: Unknown compounds
     *
     * @param reliability
     *            a {@link java.lang.Integer} object.
     */
    void setReliability(@Nullable Integer reliability);
}
