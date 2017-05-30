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

package io.github.msdk.datamodel.features;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.MsScan;

/**
 * A feature represents a single three-dimensional peak in an LC-MS or GC-MS dataset. It must have
 * at least an m/z value and a retention time. In addition, it can contain more detailed data about
 * the peak, such as S/N ratio or raw data points that constitute the feature (Chromatogram).
 * 
 */
public interface Feature {

  @Nonnull
  Double getMz();

  @Nonnull
  Float getRetentionTime();

  @Nullable
  Float getArea();

  @Nullable
  Float getHeight();

  /**
   * Returns signal to noise ratio.
   * 
   * @return S/N ratio, null if the method does not estimate it, NaN if noise was estimated to 0
   */
  @Nullable
  Float getSNRatio();

  /**
   * Returns arbitrary, dimension-less quality score for the feature
   * 
   * @return Score
   */
  @Nullable
  Float getScore();

  @Nullable
  Chromatogram getChromatogram();

  @Nullable
  List<MsScan> getMSMSSpectra();

  @Nullable
  IonAnnotation getIonAnnotation();

}
