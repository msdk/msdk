/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.id.sirius;

import de.unijena.bioinf.ChemistryBase.ms.ft.FTree;
import io.github.msdk.datamodel.SimpleIonAnnotation;

public class SiriusIonAnnotation extends SimpleIonAnnotation {
  private FTree ftree;
  public SiriusIonAnnotation(SiriusIonAnnotation master) {
    super();
    copyInternal(this, master);
  }

  public SiriusIonAnnotation() {
    super();
  }

  public SiriusIonAnnotation copy() {
    SiriusIonAnnotation target = new SiriusIonAnnotation();
    copyInternal(target, this);
    return target;
  }

  private void copyInternal(SiriusIonAnnotation target,SiriusIonAnnotation master) {
    target.setAccessionURL(master.getAccessionURL());
    target.setAnnotationId(master.getAnnotationId());
    target.setChemicalStructure(master.getChemicalStructure());
    target.setDatabase(master.getDatabase());
    target.setDescription(master.getDescription());
    target.setExpectedMz(master.getExpectedMz());
    target.setExpectedRetentionTime(master.getExpectedRetentionTime());
    target.setSpectraRef(master.getSpectraRef());
    target.setReliability(master.getReliability());
    target.setIonType(master.getIonType());
    target.setInchiKey(master.getInchiKey());
    target.setIdentificationMethod(master.getIdentificationMethod());
    target.setFormula(master.getFormula());
  }

  public void setFTree(FTree ftree) {
    this.ftree = ftree;
  }

  public FTree getFTree() {
    return ftree;
  }
}
