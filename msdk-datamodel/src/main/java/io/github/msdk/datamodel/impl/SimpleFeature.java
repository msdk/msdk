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

package io.github.msdk.datamodel.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.features.Feature;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.MsScan;

/**
 * Implementation of the Feature interface.
 */
public class SimpleFeature implements Feature {

  private @Nonnull final Double mz;
  private @Nonnull final Float retentionTime;
  private @Nullable Float area, height, snRatio, score;
  private @Nullable Chromatogram chromatogram;
  private @Nullable List<MsScan> msmsSpectra;
  private @Nullable IonAnnotation ionAnnotation;

  public SimpleFeature(@Nonnull Double mz, @Nonnull Float retentionTime) {
    this.mz = mz;
    this.retentionTime = retentionTime;
  }


  @Override
  public Double getMz() {
    return mz;
  }

  @Override
  public Float getRetentionTime() {
    return retentionTime;
  }

  @Override
  public Float getArea() {
    return area;
  }

  @Override
  public Float getHeight() {
    return height;
  }

  @Override
  public Float getSNRatio() {
    return snRatio;
  }

  @Override
  public Float getScore() {
    return score;
  }

  @Override
  public Chromatogram getChromatogram() {
    return chromatogram;
  }

  @Override
  public List<MsScan> getMSMSSpectra() {
    return msmsSpectra;
  }

  @Override
  public IonAnnotation getIonAnnotation() {
    return ionAnnotation;
  }


  public void setArea(Float area) {
    this.area = area;
  }

  public void setHeight(Float height) {
    this.height = height;
  }

  public void setSNRatio(Float snRatio) {
    this.snRatio = snRatio;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public void setChromatogram(Chromatogram chromatogram) {
    this.chromatogram = chromatogram;
  }

  public void setMSMSSpectra(List<MsScan> msmsSpectra) {
    this.msmsSpectra = msmsSpectra;
  }

  public void setIonAnnotation(IonAnnotation ionAnnotation) {
    this.ionAnnotation = ionAnnotation;
  }
  
}
