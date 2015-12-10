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

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;

/**
 * This class creates a filtered feature table based on a feature table and a
 * set of row filters.
 */
public class RowFilterMethod implements MSDKMethod<FeatureTable> {

	// Other variables
	private final @Nonnull FeatureTable featureTable;
	private final @Nonnull String nameSuffix;
	private final @Nonnull DataPointStore dataStore;
	private final @Nonnull FeatureTable result;
	private boolean canceled = false;
	private int processedRows = 0, totalRows = 0;

	public RowFilterMethod(@Nonnull FeatureTable featureTable, @Nonnull DataPointStore dataStore,
			@Nonnull String nameSuffix) {
		this.featureTable = featureTable;
		this.dataStore = dataStore;
		this.nameSuffix = nameSuffix;

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
