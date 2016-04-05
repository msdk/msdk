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
package io.github.msdk.features.ransacaligner;

import com.google.common.collect.Range;
import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.util.FeatureTableUtil;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.math.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math.optimization.fitting.PolynomialFitter;
import org.apache.commons.math.optimization.general.GaussNewtonOptimizer;
import org.apache.commons.math.stat.regression.SimpleRegression;

/**
 * This class aligns feature tables using the RANSAC method. 
 */
public class RansacAlignerMethod implements MSDKMethod<FeatureTable> {
    // Variables
    private final @Nonnull MZTolerance mzTolerance;
    private final @Nonnull RTTolerance rtTolerance;
     private final @Nonnull RTTolerance rtToleranceAfterCorrection;    
    private final boolean requireSameCharge;
    private final boolean requireSameAnnotation;
    private final @Nonnull String featureTableName;
    private final @Nonnull DataPointStore dataStore;
    private final @Nonnull List<FeatureTable> featureTables;
    private final @Nonnull FeatureTable result;
    private boolean canceled = false;
    private int processedFeatures = 0, totalFeatures = 0;
    private int k;
    private double t, numRatePoints;
    private boolean linear;

    // ID counter for the new feature table
    private int newRowID = 1;
    
     /**
     * <p>
     * Constructor for RansacAlignerMethod.
     * </p>
     *
     * @param featureTables
     *            a {@link java.util.List} object.
     * @param dataStore
     *            a {@link io.github.msdk.datamodel.datastore.DataPointStore}
     *            object.
     * @param mzTolerance
     *            a {@link io.github.msdk.util.MZTolerance} object.
     * @param requireSameCharge
     *            a {@link java.lang.Boolean} object.  
     * @param requireSameAnnotation
     *            a {@link java.lang.Boolean} object.
     * @param featureTableName
     *            a {@link java.lang.String} object.
     * @param rtTolerance
     *            a {@link io.github.msdk.util.RTTolerance} object.
     * @param rtToleranceAfterCorrection
     *            a {@link io.github.msdk.util.RTTolerance} object.
     * @param t a threshold value for determining when a data point fits
     * a mode
     * @param numRatePoints a double value representing the number of close data values required to assert that a model
     * fits well to data
     * @param linear
     *            a {@link java.lang.Boolean} object.  
     * @param k a integer value representing the maximum number of iterations allowed
     * in the algorithm
     */
    public RansacAlignerMethod(@Nonnull List<FeatureTable> featureTables,
            @Nonnull DataPointStore dataStore, @Nonnull MZTolerance mzTolerance,
            @Nonnull RTTolerance rtTolerance, @Nonnull RTTolerance rtToleranceAfterCorrection,
            boolean requireSameCharge, boolean requireSameAnnotation,
            @Nonnull String featureTableName, @Nonnull double t, @Nonnull double numRatePoints, @Nonnull boolean linear,int k) {
        this.featureTables = featureTables;
        this.dataStore = dataStore;
        this.mzTolerance = mzTolerance;
        this.rtTolerance = rtTolerance;
        this.rtToleranceAfterCorrection = rtToleranceAfterCorrection;
        this.requireSameCharge = requireSameCharge;
        this.requireSameAnnotation = requireSameAnnotation;
        this.featureTableName = featureTableName;

        // Make a new feature table
        result = MSDKObjectBuilder.getFeatureTable(featureTableName, dataStore);
    }
    
     /** {@inheritDoc} */
    @Override
    public FeatureTable execute() throws MSDKException {

        // Calculate number of feature to process. Each feature will be
        // processed twice: first for score calculation and then for actual
        // alignment.
        for (FeatureTable featureTable : featureTables) {
            totalFeatures += featureTable.getRows().size() * 2;
        }

        // Iterate through all feature tables
        Boolean firstFeatureTable = true;
        for (FeatureTable featureTable : featureTables) {

            // Add columns from the original feature table to the result table
            for (FeatureTableColumn<?> column : featureTable.getColumns()) {
                if (firstFeatureTable)
                    result.addColumn(column);
                else if (column.getSample() != null)
                    result.addColumn(column);
            }
            firstFeatureTable = false;

            // Create a sorted array of matching scores between two rows
            List<RowVsRowScore> scoreSet = new ArrayList<RowVsRowScore>();

            // Calculate scores for all possible alignments of this row
            for (FeatureTableRow row : featureTable.getRows()) {

                final Double mz = row.getMz();
                if (mz == null)
                    continue;

                // Calculate the m/z range limit for the current row
                Range<Double> mzRange = mzTolerance.getToleranceRange(mz);

                // Continue if no chromatography info is available
                ChromatographyInfo chromatographyInfo = row
                        .getChromatographyInfo();
                if (chromatographyInfo == null)
                    continue;

                // Calculate the RT range limit for the current row
                Range<Double> rtRange = rtTolerance.getToleranceRange(
                        chromatographyInfo.getRetentionTime());

                


                processedFeatures++;

                if (canceled)
                    return null;
            }

            // Create a table of mappings for best scores
            Hashtable<FeatureTableRow, FeatureTableRow> alignmentMapping = this.getAlignmentMap(featureTable);

          
            // Align all rows using the mapping
            for (FeatureTableRow sourceRow : featureTable.getRows()) {
                FeatureTableRow targetRow = alignmentMapping.get(sourceRow);

                // If we have no mapping for this row, add a new one
                if (targetRow == null) {
                    targetRow = MSDKObjectBuilder.getFeatureTableRow(result,
                            newRowID);
                    result.addRow(targetRow);
                    FeatureTableColumn<Integer> column = result
                            .getColumn(ColumnName.ID, null);
                    targetRow.setData(column, newRowID);
                    newRowID++;
                }

                // Add all features from the original row to the aligned row
                for (Sample sample : sourceRow.getFeatureTable().getSamples()) {
                    FeatureTableUtil.copyFeatureValues(sourceRow, targetRow,
                            sample);
                }

                // Combine common values from the original row with the aligned
                // row
                FeatureTableUtil.copyCommonValues(sourceRow, targetRow, true);

                processedFeatures++;
            }

            // Re-calculate average row averages
            FeatureTableUtil.recalculateAverages(result);

            if (canceled)
                return null;

        }

        // Return the new feature table
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Float getFinishedPercentage() {
        return totalFeatures == 0 ? null
                : (float) processedFeatures / totalFeatures;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public FeatureTable getResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        canceled = true;
    }
    
     private Hashtable<FeatureTableRow, FeatureTableRow> getAlignmentMap(FeatureTable featureTable) {

	// Create a table of mappings for best scores
	Hashtable<FeatureTableRow, FeatureTableRow> alignmentMapping = new Hashtable<FeatureTableRow, FeatureTableRow>();

	
	// Create a sorted set of scores matching
	TreeSet<RowVsRowScore> scoreSet = new TreeSet<RowVsRowScore>();

	// RANSAC algorithm
	List<AlignStructMol> list = ransacPeakLists(result, featureTable);
	PolynomialFunction function = this.getPolynomialFunction(list);

	List<FeatureTableRow> allRows = featureTable.getRows();

	for (FeatureTableRow row : allRows) {
	    // Calculate limits for a row with which the row can be aligned
	    Range<Double> mzRange = mzTolerance.getToleranceRange(row.getMz());

	    double rt;
	    try {
		rt = function.value(row.getChromatographyInfo().getRetentionTime());
	    } catch (NullPointerException e) {
		rt = row.getChromatographyInfo().getRetentionTime();
	    }
	    if (Double.isNaN(rt) || rt == -1) {
		rt = row.getChromatographyInfo().getRetentionTime();
	    }

	    Range<Double> rtRange = rtToleranceAfterCorrection.getToleranceRange(rt);

	    // Get all rows of the aligned feature table within the m/z and
                // RT limits
	    List<FeatureTableRow> candidateRows = result
                        .getRowsInsideRange(rtRange, mzRange);
            
	    for (FeatureTableRow candidateRow : candidateRows) {
		RowVsRowScore score;
		if (requireSameCharge) {
                        FeatureTableColumn<Integer> chargeColumn1 = featureTable
                                .getColumn(ColumnName.CHARGE, null);
                        FeatureTableColumn<Integer> chargeColumn2 = result
                                .getColumn(ColumnName.CHARGE, null);
                        Integer charge1 = row.getData(chargeColumn1);
                        Integer charge2 = candidateRow.getData(chargeColumn2);
                        if (!charge1.equals(charge2))
                            continue;
                    }

                    // Check ion annotation
                    if (requireSameAnnotation) {
                        FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn1 = featureTable
                                .getColumn(ColumnName.IONANNOTATION, null);
                        FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn2 = result
                                .getColumn(ColumnName.IONANNOTATION, null);
                        List<IonAnnotation> ionAnnotations1 = row
                                .getData(ionAnnotationColumn1);
                        List<IonAnnotation> ionAnnotations2 = candidateRow
                                .getData(ionAnnotationColumn2);

                        // Check that all ion annotations in first row are in
                        // the candidate row
                        boolean equalIons = false;
                        if (ionAnnotations1 != null
                                && ionAnnotations2 != null) {
                            for (IonAnnotation ionAnnotation : ionAnnotations1) {
                                for (IonAnnotation targetIonAnnotation : ionAnnotations2) {
                                    if (targetIonAnnotation
                                            .compareTo(ionAnnotation) == 0)
                                        equalIons = true;
                                }
                            }
                        }
                        if (!equalIons)
                            continue;

                    }

		try {
                    double mzLength = mzRange.upperEndpoint()
                            - mzRange.lowerEndpoint();
                    double rtLength = rtRange.upperEndpoint()
                            - rtRange.lowerEndpoint();
		    score = new RowVsRowScore(row, candidateRow,
			    mzLength,
			    rtLength, new Float(rt));

		    scoreSet.add(score);
		  

		} catch (Exception e) {		   
		    return null;
		}
	    }
	}

	// Iterate scores by descending order
	Iterator<RowVsRowScore> scoreIterator = scoreSet.iterator();
	while (scoreIterator.hasNext()) {

	    RowVsRowScore score = scoreIterator.next();

	    // Check if the row is already mapped
	    if (alignmentMapping.containsKey(score.getFeatureTableRow())) {
		continue;
	    }

	    // Check if the aligned row is already filled
	    if (alignmentMapping.containsValue(score.getAlignedRow())) {
		continue;
	    }

	    alignmentMapping.put(score.getFeatureTableRow(), score.getAlignedRow());

	}

	return alignmentMapping;
    }
     
      /**
     * RANSAC
     * 
     * @param alignedPeakList
     * @param peakList
     * @return
     */
    private List<AlignStructMol> ransacPeakLists(FeatureTable alignedPeakList,
	    FeatureTable peakList) {
	List<AlignStructMol> list = this.getVectorAlignment(alignedPeakList,
		peakList);
	RANSAC ransac = new RANSAC(t,numRatePoints,linear,k);
	ransac.alignment(list);
	return list;
    }

    /**
     * Return the corrected RT of the row
     * 
     * @param row
     * @param list
     * @return
     */
    private PolynomialFunction getPolynomialFunction(List<AlignStructMol> list) {
	List<RTs> data = new ArrayList<RTs>();
	for (AlignStructMol m : list) {
	    if (m.Aligned) {
		data.add(new RTs(m.RT2, m.RT));
	    }
	}

	data = this.smooth(data);
	Collections.sort(data, new RTs());

	double[] xval = new double[data.size()];
	double[] yval = new double[data.size()];
	int i = 0;

	for (RTs rt : data) {
	    xval[i] = rt.RT;
	    yval[i++] = rt.RT2;
	}

	PolynomialFitter fitter = new PolynomialFitter(3,
		new GaussNewtonOptimizer(true));
	for (RTs rt : data) {
	    fitter.addObservedPoint(1, rt.RT, rt.RT2);
	}
	try {
	    return fitter.fit();

	} catch (Exception ex) {
	    return null;
	}
    }

    private List<RTs> smooth(List<RTs> list) {
	// Add points to the model in between of the real points to smooth the
	// regression model
	Collections.sort(list, new RTs());

	for (int i = 0; i < list.size() - 1; i++) {
	    RTs point1 = list.get(i);
	    RTs point2 = list.get(i + 1);
	    if (point1.RT < point2.RT - 2) {
		SimpleRegression regression = new SimpleRegression();
		regression.addData(point1.RT, point1.RT2);
		regression.addData(point2.RT, point2.RT2);
		double rt = point1.RT + 1;
		while (rt < point2.RT) {
		    RTs newPoint = new RTs(rt, regression.predict(rt));
		    list.add(newPoint);
		    rt++;
		}

	    }
	}

	return list;
    }

    /**
     * Create the vector which contains all the possible aligned peaks.
     * 
     * @param peakListX
     * @param peakListY
     * @return vector which contains all the possible aligned peaks.
     */
    private List<AlignStructMol> getVectorAlignment(FeatureTable peakListX,
	    FeatureTable peakListY) {

	List<AlignStructMol> alignMol = new ArrayList<AlignStructMol>();
	for (FeatureTableRow row : peakListX.getRows()) {

	    // Calculate limits for a row with which the row can be aligned
	    Range<Double> mzRange = mzTolerance.getToleranceRange(row.getMz());
	    Range<Double> rtRange = rtTolerance.getToleranceRange(row.getChromatographyInfo().getRetentionTime());

	    // Get all rows of the aligned peaklist within parameter limits
	    List<FeatureTableRow> candidateRows = peakListY.getRowsInsideRange(rtRange, mzRange);
		
	    for (FeatureTableRow candidateRow : candidateRows) {
		alignMol.add(new AlignStructMol(row, candidateRow));
	    }
	}

	return alignMol;
    }

}
