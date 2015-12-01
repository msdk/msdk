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

package io.github.msdk.filtering.featurefilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;

/**
 * This class creates a filtered feature table based on a feature table and a
 * set of feature filters.
 */
public class FeatureFilterMethod implements MSDKMethod<FeatureTable> {

	// Boolean values
	private final @Nonnull boolean filterByDuration;
	private final @Nonnull boolean filterByArea;
	private final @Nonnull boolean filterByHeight;
	private final @Nonnull boolean filterByDataPoints;
	private final @Nonnull boolean filterByFWHM;
	private final @Nonnull boolean filterByTailingFactor;
	private final @Nonnull boolean filterByAsymmetryFactor;

	// Ranges values
	private final @Nullable Range<Double> durationRange;
	private final @Nullable Range<Double> areaRange;
	private final @Nullable Range<Double> heightRange;
	private final @Nullable Range<Integer> dataPointsRange;
	private final @Nullable Range<Double> fwhmRange;
	private final @Nullable Range<Double> tailingFactorRange;
	private final @Nullable Range<Double> asymmetryFactorRange;

	// Other variables
	private final @Nonnull FeatureTable featureTable;
	private final @Nonnull String nameSuffix;
	private final @Nonnull DataPointStore dataStore;
	private FeatureTable result;
	private boolean canceled = false;
	private int processedFeatures = 0, totalFeatures = 0;

	/**
	 * @param ionAnnotations,
	 *            rawDataFile, dataPointStore, mzTolerance, rtTolerance,
	 *            intensityTolerance, noiseLevel
	 */
	public FeatureFilterMethod(@Nonnull FeatureTable featureTable, @Nonnull DataPointStore dataStore,
			@Nonnull boolean filterByDuration, @Nonnull boolean filterByArea, @Nonnull boolean filterByHeight,
			@Nonnull boolean filterByDataPoints, @Nonnull boolean filterByFWHM, @Nonnull boolean filterByTailingFactor,
			@Nonnull boolean filterByAsymmetryFactor, @Nullable Range<Double> durationRange,
			@Nullable Range<Double> areaRange, @Nullable Range<Double> heightRange,
			@Nullable Range<Integer> dataPointsRange, @Nullable Range<Double> fwhmRange,
			@Nullable Range<Double> tailingFactorRange, @Nullable Range<Double> asymmetryFactorRange,
			@Nonnull String nameSuffix) {
		this.featureTable = featureTable;
		this.dataStore = dataStore;
		this.filterByDuration = filterByDuration;
		this.filterByArea = filterByArea;
		this.filterByHeight = filterByHeight;
		this.filterByDataPoints = filterByDataPoints;
		this.filterByFWHM = filterByFWHM;
		this.filterByTailingFactor = filterByTailingFactor;
		this.filterByAsymmetryFactor = filterByAsymmetryFactor;
		this.durationRange = durationRange;
		this.areaRange = areaRange;
		this.heightRange = heightRange;
		this.dataPointsRange = dataPointsRange;
		this.fwhmRange = fwhmRange;
		this.tailingFactorRange = tailingFactorRange;
		this.asymmetryFactorRange = asymmetryFactorRange;
		this.nameSuffix = nameSuffix;
	}

	/**
	 * @throws MSDKException
	 */
	@Override
	public FeatureTable execute() throws MSDKException {
		// Total features
		totalFeatures = featureTable.getRows().size() * featureTable.getSamples().size();

		// Make a new feature table
		result = MSDKObjectBuilder.getFeatureTable(featureTable.getName() + nameSuffix, dataStore);

		// Add columns
		for (FeatureTableColumn column : featureTable.getColumns()) {
			result.addColumn(column);
		}

		// Loop through all features
		for (FeatureTableRow row : featureTable.getRows()) {

			// Loop through all samples for the feature
			for (Sample sample : featureTable.getSamples()) {

				/*
				 * TODO: Add sample specific data only if the filter criteria
				 * are fulfilled.
				 */

				processedFeatures++;

				if (canceled)
					return null;
			}

			// Remove features with no data for any samples
			/*
			 * TODO!
			 */
		}

		return result;
	}

	@Override
	@Nullable
	public Float getFinishedPercentage() {
		return totalFeatures == 0 ? null : (float) processedFeatures / totalFeatures;
	}

	@Override
	@Nullable
	public FeatureTable getResult() {
		return result;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

}
