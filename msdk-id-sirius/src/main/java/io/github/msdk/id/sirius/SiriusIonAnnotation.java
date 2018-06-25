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
import de.unijena.bioinf.chemdb.DBLink;
import io.github.msdk.datamodel.SimpleIonAnnotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SiriusIonAnnotation extends SimpleIonAnnotation {
  private FTree ftree;
  private String smilesString;
  private DBLink[] dblinks;

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
    target.setFTree(master.getFTree());
    target.setDBLinks(master.getDBLinks());
    target.setSMILES(master.getSMILES());
  }

  public void setFTree(FTree ftree) {
    this.ftree = ftree;
  }

  public FTree getFTree() {
    return ftree;
  }

  //TODO: check SMILES string somehow!
  public void setSMILES(String SMILES) {
    smilesString = SMILES;
  }

  public String getSMILES() {
    return smilesString;
  }

  public void setDBLinks(DBLink[] links) {
    Set<DBLink> linksSet = new HashSet<>();
    for (DBLink link: links)
      linksSet.add(link);
    this.dblinks = new DBLink[linksSet.size()];

    // So dirty bcs problems with
    int i = 0;
    for (Iterator<DBLink> it = linksSet.iterator(); it.hasNext(); ) {
      dblinks[i++] = it.next();
    }
  }

  public DBLink[] getDBLinks() {
    return dblinks;
  }
}
