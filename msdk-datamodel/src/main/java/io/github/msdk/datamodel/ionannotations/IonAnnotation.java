/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.datamodel.ionannotations;

import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

/**
 * Annotation of a detected feature with a chemical structure, formula, or textual description.
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
   * <p>
   * Chemical formula annotation should include charge, e.g. C6H13O6+ or [C34H58N5O35P3]2-.
   * </p>
   *
   * @return Chemical formula of this annotation.
   * @see #setFormula(IMolecularFormula)
   */
  @Nullable
  IMolecularFormula getFormula();

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
   * <p>
   * getExpectedMz.
   * </p>
   *
   * @return Expected m/z value of this annotation.
   */
  @Nullable
  Double getExpectedMz();

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
   * <p>
   * getIdentificationMethod.
   * </p>
   *
   * @return Identification method (e.g. database name) of this annotation.
   */
  @Nullable
  String getIdentificationMethod();

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
   * <p>
   * getAccessionURL.
   * </p>
   *
   * @return Accession URL for a database, if this annotation comes from a database.
   */
  @Nullable
  URL getAccessionURL();

  /**
   * <p>
   * getRetentionTime.
   * </p>
   *
   * @return RT
   */
  @Nullable
  Float getExpectedRetentionTime();

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
   * <p>
   * getTaxId
   * </p>
   *
   * @return Taxonomy id for the species associated to this annotation.
   */
  @Nullable
  Integer getTaxId();

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
   * <p>
   * getDatabase
   * </p>
   *
   * @return Textual database description of this annotation.
   */
  @Nullable
  String getDatabase();

  /**
   * <p>
   * database version description to this annotation. Either the version of the used
   * database if available or otherwise the date of creation. Additionally, the number of entries in
   * the database MAY be reported in round brackets after the version in the format: {version}
   * ({#entries} entries), for example "2011-11 (1234 entries)".
   * </p>
   *
   * @return The version or date of creation of the used database.
   */
  @Nullable
  String getDatabaseVersion();

  /**
   * <p>
   * getSpectraRef
   * </p>
   *
   * @return Textual reference to a spectrum in a spectrum file for this annotation.
   */
  @Nullable
  String getSpectraRef();

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
   * <p>
   * getBestSearchEngineScore
   * </p>
   *
   * @return The best search engine score for this annotation.
   */
  @Nullable
  Double getBestSearchEngineScore();

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
   * Returns the reliability of the identification. This must be reported as an integer between 1-4:
   * 1: Identified metabolites 2: Putatively annotated compounds 3: Putatively characterized
   * compound classes 4: Unknown compounds
   *
   * @return The reliability of the ion annotation identification.
   */
  @Nullable
  Integer getReliability();

}
