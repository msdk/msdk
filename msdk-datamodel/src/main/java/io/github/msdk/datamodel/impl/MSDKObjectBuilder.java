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

package io.github.msdk.datamodel.impl;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.chromatograms.ChromatogramType;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.ActivationInfo;
import io.github.msdk.datamodel.rawdata.ActivationType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * Object builder
 */
public class MSDKObjectBuilder {

    /**
     * Common columns for feature tables
     */
    private static final @Nonnull SimpleFeatureTableColumn<Integer> IdFeatureTableColumn = new SimpleFeatureTableColumn<Integer>(
            "Id", Integer.class, null);
    private static final @Nonnull SimpleFeatureTableColumn<Double> MzFeatureTableColumn = new SimpleFeatureTableColumn<Double>(
            ColumnName.MZ.getName(), Double.class, null);
    private static final @Nonnull SimpleFeatureTableColumn<ChromatographyInfo> ChromatographyInfoFeatureTableColumn = new SimpleFeatureTableColumn<ChromatographyInfo>(
            "Chromatography Info", ChromatographyInfo.class, null);
    private static final @Nonnull SimpleFeatureTableColumn<IonAnnotation> IonAnnotationFeatureTableColumn = new SimpleFeatureTableColumn<IonAnnotation>(
            "Ion Annotation", IonAnnotation.class, null);
    private static final @Nonnull SimpleFeatureTableColumn<Integer> ChargeFeatureTableColumn = new SimpleFeatureTableColumn<Integer>(
            ColumnName.CHARGE.getName(), Integer.class, null);

    
    /**
     * The number of MS functions used in a project is typically small, but each
     * scan has to be annotated with its MS function. So we take advantage of
     * the immutable nature of MsFunction and recycle the instances using this
     * List.
     */
    private static final List<WeakReference<MsFunction>> msFunctions = new LinkedList<>();

    /**
     * Creates a new MsFunction reference.
     * 
     * @param name
     * @param msLevel
     * @return
     */
    public static final @Nonnull MsFunction getMsFunction(@Nonnull String name,
            @Nullable Integer msLevel) {

        synchronized (msFunctions) {
            Iterator<WeakReference<MsFunction>> iter = msFunctions.iterator();
            while (iter.hasNext()) {
                WeakReference<MsFunction> ref = iter.next();
                MsFunction func = ref.get();
                if (func == null) {
                    iter.remove();
                    continue;
                }
                if (!func.getName().equals(name))
                    continue;
                if ((func.getMsLevel() == null) && (msLevel == null))
                    return func;
                if ((func.getMsLevel() != null)
                        && (func.getMsLevel().equals(msLevel)))
                    return func;
            }
            MsFunction newFunc = new SimpleMsFunction(name, msLevel);
            WeakReference<MsFunction> ref = new WeakReference<>(newFunc);
            msFunctions.add(ref);
            return newFunc;

        }
    }

    /**
     * Creates a new MsFunction reference.
     * 
     * @param name
     * @return
     */
    public static final @Nonnull MsFunction getMsFunction(
            @Nonnull String name) {
        return getMsFunction(name, null);
    }

    /**
     * Creates a new MsFunction reference.
     * 
     * @param msLevel
     * @return
     */
    @SuppressWarnings("null")
    public static final @Nonnull MsFunction getMsFunction(
            @Nullable Integer msLevel) {
        return getMsFunction(MsFunction.DEFAULT_MS_FUNCTION_NAME, msLevel);
    }

    public static final @Nonnull RawDataFile getRawDataFile(
            @Nonnull String rawDataFileName, @Nullable File originalRawDataFile,
            @Nonnull FileType rawDataFileType,
            @Nonnull DataPointStore dataPointStore) {
        return new SimpleRawDataFile(rawDataFileName, originalRawDataFile,
                rawDataFileType, dataPointStore);
    }

    public static final @Nonnull FeatureTable getFeatureTable(
            @Nonnull String featureTableName,
            @Nonnull DataPointStore dataPointStore) {
        return new SimpleFeatureTable(featureTableName, dataPointStore);
    }

    public static final @Nonnull MsScan getMsScan(
            @Nonnull DataPointStore dataPointStore, @Nonnull Integer scanNumber,
            @Nonnull MsFunction msFunction) {
        return new SimpleMsScan(dataPointStore, scanNumber, msFunction);
    }

    public static final @Nonnull Chromatogram getChromatogram(
            @Nonnull DataPointStore dataPointStore,
            @Nonnull Integer chromatogramNumber,
            @Nonnull ChromatogramType chromatogramType,
            @Nonnull SeparationType separationType) {
        return new SimpleChromatogram(dataPointStore, chromatogramNumber,
                chromatogramType, separationType);
    }

    public static final @Nonnull ChromatographyInfo getChromatographyInfo1D(
            SeparationType separationType, Float rt1) {
        return new SimpleChromatographyInfo(rt1, null, null, separationType);
    }

    public static final @Nonnull ChromatographyInfo getChromatographyInfo2D(
            SeparationType separationType, Float rt1, Float rt2) {
        if (separationType.getFeatureDimensions() < 2) {
            throw new IllegalArgumentException(
                    "2D ChromatographyInfo requires at least 2 feature dimensions. Provided separation type "
                            + separationType + " has "
                            + separationType.getFeatureDimensions());
        }
        return new SimpleChromatographyInfo(rt1, rt2, null, separationType);
    }

    public static final @Nonnull ChromatographyInfo getImsInfo(
            SeparationType separationType, Float rt1, Float ionDriftTime) {
        if (separationType.getFeatureDimensions() < 2) {
            throw new IllegalArgumentException(
                    "2D ChromatographyInfo requires at least 2 feature dimensions. Provided separation type "
                            + separationType + " has "
                            + separationType.getFeatureDimensions());
        }
        if (separationType != SeparationType.IMS) {
            throw new IllegalArgumentException(
                    "2D ChromatographyInfo for IMS separation requires IMS separation type. Provided separation type "
                            + separationType);
        }
        return new SimpleChromatographyInfo(rt1, null, ionDriftTime,
                separationType);
    }

    /**
     * Creates a new SpectrumDataPointList instance with no data points and
     * initial capacity of 100.
     * 
     * @return new DataPointList
     */
    public static final @Nonnull MsSpectrumDataPointList getMsSpectrumDataPointList() {
        return new SimpleMSSpectrumDataPointList();
    }

    /**
     * Creates a new ChromatogramDataPointList instance with no data points and
     * initial capacity of 100.
     * 
     * @return new DataPointList
     */
    public static final @Nonnull ChromatogramDataPointList getChromatogramDataPointList() {
        return new SimpleChromatogramDataPointList();
    }

    /**
     * Creates a new FeatureTableColumn instance.
     * 
     * @return new SimpleFeatureTableColumn
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static @Nonnull <DataType> FeatureTableColumn<DataType> getFeatureTableColumn(
            @Nonnull ColumnName columnName, @Nullable Sample sample) {
        return new SimpleFeatureTableColumn<DataType>(columnName.getName(),
                (Class) columnName.getDataTypeClass(), sample);
    }

    public static @Nonnull <DataType> FeatureTableColumn<DataType> getFeatureTableColumn(
            @Nonnull String name, @Nonnull Class<DataType> dataTypeClass,
            @Nullable Sample sample) {
        return new SimpleFeatureTableColumn<DataType>(name, dataTypeClass,
                sample);
    }

    /**
     * Creates a new FeatureTableRow instance.
     * 
     * @return new SimpleFeatureTableRow
     */
    public static @Nonnull SimpleFeatureTableRow getFeatureTableRow(
            @Nonnull FeatureTable featureTable, int rowId) {
        return new SimpleFeatureTableRow(featureTable, rowId);
    }

    public static @Nonnull FeatureTableColumn<Double> getMzFeatureTableColumn() {
        return MzFeatureTableColumn;
    }

    public static @Nonnull FeatureTableColumn<ChromatographyInfo> getChromatographyInfoFeatureTableColumn() {
        return ChromatographyInfoFeatureTableColumn;
    }

    public static @Nonnull FeatureTableColumn<Integer> getIdFeatureTableColumn() {
        return IdFeatureTableColumn;
    }

    public static @Nonnull FeatureTableColumn<IonAnnotation> getIonAnnotationFeatureTableColumn() {
        return IonAnnotationFeatureTableColumn;
    }

    public static @Nonnull FeatureTableColumn<Integer> getChargeFeatureTableColumn() {
        return ChargeFeatureTableColumn;
    }

    /**
     * Creates a new ActivationInfo reference.
     * 
     * @param activationEnergy
     * @param fragmentationType
     * @return new SimpleActivationInfo
     */
    public static final @Nonnull ActivationInfo getActivationInfo(
            @Nullable Double activationEnergy,
            @Nonnull ActivationType fragmentationType) {
        ActivationInfo newFunc = new SimpleActivationInfo(activationEnergy,
                fragmentationType);
        return newFunc;
    }

    /**
     * Creates a new IsolationInfo reference.
     * 
     * @param isolationMzRange
     * @param ionInjectTime
     * @param precursorMz
     * @param precursorCharge
     * @param activationInfo
     * @return new SimpleIsolationInfo
     */
    public static final @Nonnull IsolationInfo getIsolationInfo(
            @Nonnull Range<Double> isolationMzRange,
            @Nullable Float ionInjectTime, @Nullable Double precursorMz,
            @Nullable Integer precursorCharge,
            @Nullable ActivationInfo activationInfo) {
        IsolationInfo newFunc = new SimpleIsolationInfo(isolationMzRange,
                ionInjectTime, precursorMz, precursorCharge, activationInfo);
        return newFunc;
    }

    /**
     * Creates a new SimpleSample reference.
     * 
     * @param sampleName
     * @return new SimpleSample
     */
    public static final @Nonnull SimpleSample getSimpleSample(
            @Nonnull String sampleName) {
        SimpleSample newSample = new SimpleSample(sampleName);
        return newSample;
    }

    /**
     * Creates a new SimpleSample reference.
     * 
     * @param sampleName
     * @return new SimpleSample
     */
    public static final @Nonnull SimpleIonAnnotation getSimpleIonAnnotation() {
        SimpleIonAnnotation ionAnnotation = new SimpleIonAnnotation();
        return ionAnnotation;
    }

}
