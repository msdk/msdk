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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;

/**
 * Implementation of the FeatureTable interface.
 */
public class SimpleFeatureTable implements FeatureTable {

  private @Nonnull DataPointStore dataPointStore;
  private final @Nonnull ArrayList<FeatureTableRow> featureTableRows = new ArrayList<>();
  private final @Nonnull ArrayList<Sample> featureTableSamples = new ArrayList<>();

  @Override
  public @Nonnull List<FeatureTableRow> getRows() {
    return ImmutableList.copyOf(featureTableRows);
  }

  public void addRow(@Nonnull FeatureTableRow row) {
    Preconditions.checkNotNull(row);
    synchronized (featureTableRows) {
      featureTableRows.add(row);
    }
  }

  public void removeRow(@Nonnull FeatureTableRow row) {
    Preconditions.checkNotNull(row);
    synchronized (featureTableRows) {
      featureTableRows.remove(row);
    }
  }


  /** {@inheritDoc} */
  @Override
  public @Nonnull List<Sample> getSamples() {
    return ImmutableList.copyOf(featureTableSamples);
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    dataPointStore.dispose();
  }


}
