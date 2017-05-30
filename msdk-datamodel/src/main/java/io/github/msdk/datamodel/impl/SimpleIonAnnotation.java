/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.datamodel.impl;

import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;

import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;

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
  private @Nullable Float expectedRT;
  private @Nullable String inchiKey;
  private @Nullable Integer taxId;
  private @Nullable String species;
  private @Nullable String database;
  private @Nullable String databaseVersion;
  private @Nullable String spectraRef;
  private @Nullable String searchEngine;
  private @Nullable Double bestSearchEngineScore;
  private @Nullable String modifications;
  private @Nullable Integer reliability;

  /** {@inheritDoc} */
  @Override
  @Nullable
  public IAtomContainer getChemicalStructure() {
    return chemicalStructure;
  }

  /** {@inheritDoc} */
  @Override
  public void setChemicalStructure(@Nullable IAtomContainer chemicalStructure) {
    this.chemicalStructure = chemicalStructure;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public IMolecularFormula getFormula() {
    return formula;
  }

  /** {@inheritDoc} */
  @Override
  public void setFormula(@Nullable IMolecularFormula formula) {
    this.formula = formula;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public IonType getIonType() {
    return ionType;
  }

  /** {@inheritDoc} */
  @Override
  public void setIonType(@Nullable IonType ionType) {
    this.ionType = ionType;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getExpectedMz() {
    return expectedMz;
  }

  /** {@inheritDoc} */
  @Override
  public void setExpectedMz(@Nullable Double expectedMz) {
    this.expectedMz = expectedMz;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getDescription() {
    return description;
  }

  /** {@inheritDoc} */
  @Override
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getIdentificationMethod() {
    return identificationMethod;
  }

  /** {@inheritDoc} */
  @Override
  public void setIdentificationMethod(@Nullable String identificationMethod) {
    this.identificationMethod = identificationMethod;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getAnnotationId() {
    return annotationId;
  }

  /** {@inheritDoc} */
  @Override
  public void setAnnotationId(@Nullable String annotationId) {
    this.annotationId = annotationId;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public URL getAccessionURL() {
    return accessionURL;
  }

  /** {@inheritDoc} */
  @Override
  public void setAccessionURL(@Nullable URL accessionURL) {
    this.accessionURL = accessionURL;
  }

  /** {@inheritDoc} */
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
      } else if (thisAnnotationId == null && i.getAnnotationId() == null) {
        returnValue = 0;
      } else if (thisAnnotationId == null) {
        returnValue = 1;
      } else {
        returnValue = -1;
      }
    }

    return returnValue;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getExpectedRetentionTime() {
    return expectedRT;
  }

  /** {@inheritDoc} */
  @Override
  public void setExpectedRetentionTime(@Nullable Float expectedRT) {
    this.expectedRT = expectedRT;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Boolean isNA() {
    if (this.chemicalStructure == null & this.formula == null & this.ionType == null
        & this.expectedMz == null & this.description == null
        & (this.annotationId == null || this.annotationId == "") & this.accessionURL == null
        & this.expectedRT == null)
      return true;
    else
      return false;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getInchiKey() {
    return inchiKey;
  }

  /** {@inheritDoc} */
  @Override
  public void setInchiKey(@Nullable String inchiKey) {
    this.inchiKey = inchiKey;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Integer getTaxId() {
    return taxId;
  }

  /** {@inheritDoc} */
  @Override
  public void setTaxId(@Nullable Integer taxId) {
    this.taxId = taxId;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getSpecies() {
    return species;
  }

  /** {@inheritDoc} */
  @Override
  public void setSpecies(@Nullable String species) {
    this.species = species;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getDatabase() {
    return database;
  }

  /** {@inheritDoc} */
  @Override
  public void setDatabase(@Nullable String database) {
    this.database = database;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getDatabaseVersion() {
    return databaseVersion;
  }

  /** {@inheritDoc} */
  @Override
  public void setDatabaseVersion(@Nullable String databaseVersion) {
    this.databaseVersion = databaseVersion;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getSpectraRef() {
    return spectraRef;
  }

  /** {@inheritDoc} */
  @Override
  public void setSpectraRef(@Nullable String spectraRef) {
    this.spectraRef = spectraRef;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getSearchEngine() {
    return searchEngine;
  }

  /** {@inheritDoc} */
  @Override
  public void setSearchEngine(@Nullable String searchEngine) {
    this.searchEngine = searchEngine;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getBestSearchEngineScore() {
    return bestSearchEngineScore;
  }

  /** {@inheritDoc} */
  @Override
  public void setBestSearchEngineScore(@Nullable Double bestSearchEngineScore) {
    this.bestSearchEngineScore = bestSearchEngineScore;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getModifications() {
    return modifications;
  }

  /** {@inheritDoc} */
  @Override
  public void setModifications(@Nullable String modifications) {
    this.modifications = modifications;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Integer getReliability() {
    return reliability;
  }

  /** {@inheritDoc} */
  @Override
  public void setReliability(@Nullable Integer reliability) {
    this.reliability = reliability;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return annotationId + " @ " + expectedMz;
  }

}
