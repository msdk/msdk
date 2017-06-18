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

package io.github.msdk.io.mztab;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.common.math.DoubleMath;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.features.Feature;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.MsScan;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.SmallMolecule;

class MzTabFeature implements Feature, IonAnnotation {

  private final SmallMolecule smallMolecule;
  private final Assay sampleAssay;

  MzTabFeature(SmallMolecule smallMolecule, Assay sampleAssay) {
    this.smallMolecule = smallMolecule;
    this.sampleAssay = sampleAssay;
  }

  @Override
  public Double getMz() {
    return smallMolecule.getExpMassToCharge();
  }

  @Override
  public Float getRetentionTime() {
    return (float) DoubleMath.mean(smallMolecule.getRetentionTime());
  }

  @Override
  public Float getArea() {
    return smallMolecule.getAbundanceColumnValue(sampleAssay).floatValue();
  }

  @Override
  public Float getHeight() {
    return null;
  }

  @Override
  public Float getSNRatio() {
    return null;
  }

  @Override
  public Float getScore() {
    return null;
  }

  @Override
  public Chromatogram getChromatogram() {
    return null;
  }

  @Override
  public List<MsScan> getMSMSSpectra() {
    return null;
  }

  @Override
  public IonAnnotation getIonAnnotation() {
    return this;
  }

  @Override
  public IAtomContainer getChemicalStructure() {
    try {
      SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
      String smiles = smallMolecule.getSmiles().toString();
      IAtomContainer chemicalStructure = sp.parseSmiles(smiles);
      return chemicalStructure;
    } catch (InvalidSmilesException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public IMolecularFormula getFormula() {
    smallMolecule.getChemicalFormula();
    return null;
  }

  @Override
  public IonType getIonType() {
    return null;
  }

  @Override
  public Double getExpectedMz() {
    return smallMolecule.getCalcMassToCharge();
  }

  @Override
  public String getDescription() {
    return smallMolecule.getDescription();
  }

  @Override
  public String getIdentificationMethod() {
    return null;
  }


  @Override
  public URL getAccessionURL() {
    try {
      return smallMolecule.getURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public Float getExpectedRetentionTime() {
    return null;
  }

  @Override
  public String getInchiKey() {
    return smallMolecule.getInchiKey().toString();
  }

  @Override
  public String getDatabase() {
    return smallMolecule.getDatabase();
  }

  @Override
  public String getSpectraRef() {
    return null;
  }


  @Override
  public Integer getReliability() {
    if (smallMolecule.getReliability() == null)
      return null;
    else
      return smallMolecule.getReliability().getLevel();
  }

}
