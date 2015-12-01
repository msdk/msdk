/* 
 * (C) Copyright 2015 by MSDK Development Team
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

import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Represents a single MS scan in a raw data file. This interface extends
 * IMassSpectrum, therefore the actual data points can be accessed through the
 * inherited methods of IMassSpectrum.
 *
 * If the scan is not added to any file, its data points are stored in memory.
 * However, once the scan is added into a raw data file by calling
 * setRawDataFile(), its data points will be stored in a temporary file that
 * belongs to that RawDataFile. When RawDataFile.dispose() is called, the data
 * points are discarded so the MsScan instance cannot be used anymore.
 */
public interface Chromatogram extends Cloneable {

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
     * @param newRawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
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
     * Loads the data points of this chromatogram into the given DataPointList.
     * If the DataPointList is not empty, it is cleared first. This method
     * allows the internal arrays of the DataPointList to be reused for loading
     * multiple spectra.
     *
     * Note: this method may need to read data from disk, therefore it may be
     * quite slow.
     *
     * @param dataPointList a {@link io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList} object.
     */
    void getDataPoints(@Nonnull ChromatogramDataPointList dataPointList);

    /**
     * Updates the data points of this chromatogram. If this Chromatogram has
     * been added to a raw data file or a feature table, the data points will be
     * immediately stored in a temporary file. Therefore, the DataPointList in
     * the parameter can be reused for other purposes.
     *
     * Note: this method may need to write data to disk, therefore it may be
     * quite slow.
     *
     * @param newDataPoints
     *            new data points
     */
    void setDataPoints(@Nonnull ChromatogramDataPointList newDataPoints);

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
     * @param newMz a {@link java.lang.Double} object.
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
     * Returns a deep clone of this object.
     *
     * @param newStore
     *            data points of the newly created MsScan will be stored in this
     *            store
     * @return A clone of this MsScan.
     */
    @Nonnull
    Chromatogram clone(@Nonnull DataPointStore newStore);

}
