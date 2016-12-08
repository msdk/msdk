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

package io.github.msdk.datamodel.chromatograms;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Represents a single chromatogram.
 */
public interface Chromatogram {

    /**
     * Returns the raw data file that contains this chromatogram. This might
     * return null when the chromatogram is created, but once the chromatogram
     * is added to the raw data file by calling RawDataFile.addChromatogram(),
     * the RawDataFile automatically calls the Chromatogram.setRawDataFile()
     * method to update this reference.
     *
     * @return RawDataFile containing this chromatogram, or null.
     * @see RawDataFile
     */
    @Nullable
    RawDataFile getRawDataFile();

    /**
     * Updates the raw data file reference. This method can be called only once.
     * Any subsequent calls will throw the IllegalOperationException.
     *
     * @param newRawDataFile
     *            a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
     */
    void setRawDataFile(@Nonnull RawDataFile newRawDataFile);

    /**
     * Returns the number of this chromatogram, represented by an integer,
     * typically positive. Typically, the chromatogram number will be unique
     * within the file. However, the data model does not guarantee that, and in
     * some cases multiple chromatogram with the same number may be present in
     * the file.
     *
     * @return Chromatogram number
     */
    @Nonnull
    Integer getChromatogramNumber();

    /**
     * Updates the chromatogram number.
     *
     * @param chromatogramNumber
     *            New chromatogram number.
     */
    void setChromatogramNumber(@Nonnull Integer chromatogramNumber);

    /**
     * Returns the type of the chromatogram. If unknown,
     * ChromatogramType.UNKNOWN is returned.
     *
     * @return Chromatogram type
     */
    @Nonnull
    ChromatogramType getChromatogramType();

    /**
     * Updates the chromatogram type.
     *
     * @param newChromatogramType
     *            New chromatogram type.
     */
    void setChromatogramType(@Nonnull ChromatogramType newChromatogramType);

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
     * Returns the info of this chromatogram. Generally, this method should pass
     * null to the method that takes an array as a parameter.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     *
     * @return an array of
     *         {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *         objects.
     * @see getRetentionTimes(ChromatographyInfo array[])
     */
    @Nonnull
    ChromatographyInfo[] getRetentionTimes();

    /**
     * <p>
     * Loads the info of this chromatogram into the given array of
     * ChromtagraphyInfo. If the given array is null or is not large enough to
     * hold all values, a new array is created.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     * </p>
     *
     * @param array
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @return an array of
     *         {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *         objects.
     */
    @Nonnull
    ChromatographyInfo[] getRetentionTimes(
            @Nullable ChromatographyInfo array[]);

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
     * @param array
     *            an array of float.
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
     * <p>
     * getMzValues.
     * </p>
     *
     * @param array
     *            an array of double.
     * @return an array of double.
     */
    @Nullable
    double[] getMzValues(@Nullable double array[]);

    /**
     * <p>
     * setDataPoints.
     * </p>
     *
     * @param rtValues
     *            an array of
     *            {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo}
     *            objects.
     * @param mzValues
     *            an array of double.
     * @param intensityValues
     *            an array of float.
     * @param size
     *            a {@link java.lang.Integer} object.
     */
    void setDataPoints(@Nonnull ChromatographyInfo rtValues[],
            @Nullable double mzValues[], @Nonnull float intensityValues[],
            @Nonnull Integer size);

    /**
     * Returns the m/z value of this chromatogram, or null if no m/z value is
     * set for the chromatogram.
     *
     * @return a {@link java.lang.Double} object.
     */
    @Nullable
    Double getMz();

    /**
     * Sets the m/z value of the chromatogram
     *
     * @param newMz
     *            a {@link java.lang.Double} object.
     */
    void setMz(@Nullable Double newMz);

    /**
     * Returns a list of isolations performed for this chromatogram. These
     * isolations may also include fragmentations (tandem MS).
     *
     * @return A mutable list of isolations. New isolation items can be added to
     *         this list.
     */
    @Nonnull
    List<IsolationInfo> getIsolations();

    /**
     * Returns the separation type used for separation of molecules.
     *
     * @return the seperation type. Returns {@link SeparationType#UNKNOWN} for
     *         unknown separations.
     */
    @Nonnull
    SeparationType getSeparationType();

    /**
     * Sets the ion annotation for this chromatogram.
     *
     * @param ionAnnotation
     *            New ion annotation.
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
     * @param separationType
     *            New seperation type.
     */
    void setSeparationType(@Nonnull SeparationType separationType);

    /**
     * Returns the range of retention times. This can return null if the
     * chromatogram has no data points.
     *
     * @return RT range
     */
    @Nullable
    Range<ChromatographyInfo> getRtRange();

}
