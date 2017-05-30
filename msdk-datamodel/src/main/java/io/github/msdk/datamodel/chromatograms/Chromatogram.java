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

package io.github.msdk.datamodel.chromatograms;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Represents a single chromatogram.
 */
public interface Chromatogram {

  /**
   * Returns the raw data file that contains this chromatogram. This might return null when the
   * chromatogram is created, but once the chromatogram is added to the raw data file by calling
   * RawDataFile.addChromatogram(), the RawDataFile automatically calls the
   * Chromatogram.setRawDataFile() method to update this reference.
   *
   * @return RawDataFile containing this chromatogram, or null.
   */
  @Nullable
  RawDataFile getRawDataFile();

  /**
   * Returns the number of this chromatogram, represented by an integer, typically positive.
   * Typically, the chromatogram number will be unique within the file. However, the data model does
   * not guarantee that, and in some cases multiple chromatogram with the same number may be present
   * in the file.
   *
   * @return Chromatogram number
   */
  @Nonnull
  Integer getChromatogramNumber();

  /**
   * Returns the type of the chromatogram. If unknown, ChromatogramType.UNKNOWN is returned.
   *
   * @return Chromatogram type
   */
  @Nonnull
  ChromatogramType getChromatogramType();

  /**
   * <p>
   * getNumberOfDataPoints.
   * </p>
   *
   * @return a {@link java.lang.Integer} object.
   */
  @Nonnull
  Integer getNumberOfDataPoints();

  /**
   * Returns the info of this chromatogram. Generally, this method should pass null to the method
   * that takes an array as a parameter.
   *
   * Note: this method may need to read data from disk, therefore it may be quite slow.
   *
   * @return an array of {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo} objects.
   */
  @Nonnull
  float[] getRetentionTimes();

  /**
   * <p>
   * getIntensityValues.
   * </p>
   *
   * @return an array of float.
   */
  @Nonnull
  float[] getIntensityValues();

  /**
   * <p>
   * getIntensityValues.
   * </p>
   *
   * @param array an array of float.
   * @return an array of float.
   */
  @Nonnull
  float[] getIntensityValues(@Nullable float array[]);

  /**
   * <p>
   * getMzValues.
   * </p>
   *
   * @return an array of double.
   */
  @Nullable
  double[] getMzValues();

  /**
   * Returns the m/z value of this chromatogram, or null if no m/z value is set for the
   * chromatogram.
   *
   * @return a {@link java.lang.Double} object.
   */
  @Nullable
  Double getMz();

  /**
   * Sets the m/z value of the chromatogram
   *
   * @param newMz a {@link java.lang.Double} object.
   */
  void setMz(@Nullable Double newMz);

  /**
   * Returns a list of isolations performed for this chromatogram. These isolations may also include
   * fragmentations (tandem MS).
   *
   * @return A mutable list of isolations. New isolation items can be added to this list.
   */
  @Nonnull
  List<IsolationInfo> getIsolations();

  /**
   * Returns the separation type used for separation of molecules.
   *
   * @return the seperation type. Returns {@link SeparationType#UNKNOWN} for unknown separations.
   */
  @Nonnull
  SeparationType getSeparationType();

  /**
   * Sets the ion annotation for this chromatogram.
   *
   * @param ionAnnotation New ion annotation.
   */
  void setIonAnnotation(@Nonnull IonAnnotation ionAnnotation);

  /**
   * Returns the ion annotation for this chromatogram.
   *
   * @return the ion annotation.
   */
  IonAnnotation getIonAnnotation();

  /**
   * Sets the separation type used for separation of molecules.
   *
   * @param separationType New seperation type.
   */
  void setSeparationType(@Nonnull SeparationType separationType);

  /**
   * Returns the range of retention times. This can return null if the chromatogram has no data
   * points.
   *
   * @return RT range
   */
  @Nullable
  Range<Float> getRtRange();

}
