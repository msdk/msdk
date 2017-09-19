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

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import com.google.common.base.Strings;
import com.google.common.math.DoubleMath;

import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.Feature;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.IonType;
import io.github.msdk.datamodel.MsScan;
import uk.ac.ebi.pride.jmztab.model.Assay;
import uk.ac.ebi.pride.jmztab.model.SmallMolecule;

class MzTabFeature implements Feature, IonAnnotation {

  private final SmallMolecule smallMolecule;
  private final Assay sampleAssay;

  MzTabFeature(SmallMolecule smallMolecule, Assay sampleAssay) {
    this.smallMolecule = smallMolecule;
    this.sampleAssay = sampleAssay;
  }

  /** {@inheritDoc} */
  @Override
  public Double getMz() {
    String sampleMzString = smallMolecule.getOptionColumnValue(sampleAssay, "mz");
    if (Strings.isNullOrEmpty(sampleMzString))
      return smallMolecule.getExpMassToCharge();
    else
      return Double.parseDouble(sampleMzString);
  }

  /** {@inheritDoc} */
  @Override
  public Float getRetentionTime() {
    return (float) DoubleMath.mean(smallMolecule.getRetentionTime());
  }

  /** {@inheritDoc} */
  @Override
  public Float getArea() {
    Double abundance = smallMolecule.getAbundanceColumnValue(sampleAssay);
    if (abundance == null)
      return null;
    else
      return abundance.floatValue();
  }

  /** {@inheritDoc} */
  @Override
  public Float getHeight() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Float getSNRatio() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Float getScore() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Chromatogram getChromatogram() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public List<MsScan> getMSMSSpectra() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public IonAnnotation getIonAnnotation() {
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public IAtomContainer getChemicalStructure() {
    try {
      SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
      String smiles = smallMolecule.getSmiles().toString();
      IAtomContainer chemicalStructure = sp.parseSmiles(smiles);
      return chemicalStructure;
    } catch (InvalidSmilesException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public IMolecularFormula getFormula() {
    String formula = smallMolecule.getChemicalFormula();
    IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula(formula,
        SilentChemObjectBuilder.getInstance());
    return cdkFormula;
  }

  /** {@inheritDoc} */
  @Override
  public IonType getIonType() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Double getExpectedMz() {
    return smallMolecule.getCalcMassToCharge();
  }

  /** {@inheritDoc} */
  @Override
  public String getDescription() {
    if (smallMolecule.getIdentifier() != null) {
      String smID = smallMolecule.getIdentifier().toString();
      if (!Strings.isNullOrEmpty(smID))
        return smID;
    }
    return smallMolecule.getDescription();
  }

  /** {@inheritDoc} */
  @Override
  public String getIdentificationMethod() {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public URL getAccessionURL() {
    try {
      return smallMolecule.getURI().toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public Float getExpectedRetentionTime() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public String getInchiKey() {
    return smallMolecule.getInchiKey().toString();
  }

  /** {@inheritDoc} */
  @Override
  public String getDatabase() {
    return smallMolecule.getDatabase();
  }

  /** {@inheritDoc} */
  @Override
  public String getSpectraRef() {
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public Integer getReliability() {
    if (smallMolecule.getReliability() == null)
      return null;
    else
      return smallMolecule.getReliability().getLevel();
  }

  /** {@inheritDoc} */
  @Override
  public String getAnnotationId() {
    return smallMolecule.getIdentifier().toString();
  }

}
