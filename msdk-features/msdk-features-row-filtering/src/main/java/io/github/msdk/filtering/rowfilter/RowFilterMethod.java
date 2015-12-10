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

package io.github.msdk.filtering.rowfilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Range;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.util.MZTolerance;
import io.github.msdk.util.RTTolerance;

/**
 * This class creates a filtered feature table based on a feature table and a
 * set of row filters.
 */
public class RowFilterMethod implements MSDKMethod<FeatureTable> {

	// Boolean values
	private final @Nonnull boolean filterByMz;
	private final @Nonnull boolean filterByRt;
	private final @Nonnull boolean filterByDuration;
	private final @Nonnull boolean filterByCount;
	private final @Nonnull boolean filterByIsotopes;
	private final @Nonnull boolean filterByIonAnnotation;
	private final @Nonnull boolean requireAnnotation;
	private final @Nonnull boolean removeDuplicates;
	private final @Nonnull boolean duplicateRequireSameID;

	// Ranges values
	private final @Nullable Range<Double> mzRange;
	private final @Nullable Range<Double> rtRange;
	private final @Nullable Range<Double> durationRange;

	// Tolerance values
	private final @Nullable MZTolerance duplicateMzTolerance;
	private final @Nullable RTTolerance duplicateRtTolerance;

	// Other variables
	private final @Nullable Integer minCount;
	private final @Nullable Integer minIsotopes;
	private final @Nullable String ionAnnotation;
	private final @Nonnull FeatureTable featureTable;
	private final @Nonnull String nameSuffix;
	private final @Nonnull DataPointStore dataStore;
	private final @Nonnull FeatureTable result;
	private boolean canceled = false;
	private int processedRows = 0, totalRows = 0;

	public RowFilterMethod(@Nonnull FeatureTable featureTable, @Nonnull DataPointStore dataStore,
			@Nonnull String nameSuffix, @Nonnull boolean filterByMz, @Nonnull boolean filterByRt,
			@Nonnull boolean filterByDuration, @Nonnull boolean filterByCount, @Nonnull boolean filterByIsotopes,
			@Nonnull boolean filterByIonAnnotation, @Nonnull boolean requireAnnotation, @Nullable Range<Double> mzRange,
			@Nullable Range<Double> rtRange, @Nullable Range<Double> durationRange, @Nullable Integer minCount,
			@Nullable Integer minIsotopes, @Nullable String ionAnnotation, @Nonnull boolean removeDuplicates,
			@Nullable MZTolerance duplicateMzTolerance, @Nullable RTTolerance duplicateRtTolerance,
			@Nonnull boolean duplicateRequireSameID) {

		this.featureTable = featureTable;
		this.dataStore = dataStore;
		this.nameSuffix = nameSuffix;
		this.filterByMz = filterByMz;
		this.filterByRt = filterByRt;
		this.filterByDuration = filterByDuration;
		this.filterByCount = filterByCount;
		this.filterByIsotopes = filterByIsotopes;
		this.filterByIonAnnotation = filterByIonAnnotation;
		this.requireAnnotation = requireAnnotation;
		this.mzRange = mzRange;
		this.rtRange = rtRange;
		this.durationRange = durationRange;
		this.minCount = minCount;
		this.minIsotopes = minIsotopes;
		this.ionAnnotation = ionAnnotation;
		this.removeDuplicates = removeDuplicates;
		this.duplicateMzTolerance = duplicateMzTolerance;
		this.duplicateRtTolerance = duplicateRtTolerance;
		this.duplicateRequireSameID = duplicateRequireSameID;

		// Make a new feature table
		result = MSDKObjectBuilder.getFeatureTable(featureTable.getName() + nameSuffix, dataStore);
	}

	/**
	 * @throws MSDKException
	 */
	@Override
	public FeatureTable execute() throws MSDKException {
		// Total features
		totalRows = featureTable.getRows().size();

		// Return the new feature table
		return result;
	}

	@Override
	@Nullable
	public Float getFinishedPercentage() {
		return totalRows == 0 ? null : (float) processedRows / totalRows;
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
