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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import io.github.msdk.datamodel.features.Feature;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;

/**
 * Implementation of FeatureTableRow. Backed by a non-thread safe Map.
 */
public class SimpleFeatureTableRow implements FeatureTableRow {

  private final @Nonnull FeatureTable featureTable;
  private final @Nonnull Map<Sample, Feature> features = new HashMap<>();

  public SimpleFeatureTableRow(@Nonnull FeatureTable featureTable) {
    Preconditions.checkNotNull(featureTable);
    this.featureTable = featureTable;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull FeatureTable getFeatureTable() {
    return featureTable;
  }

  @Override
  public Double getMz() {
    Collection<Feature> allFeatures = features.values();
    double averageMz = allFeatures.stream().mapToDouble(Feature::getMz).average().getAsDouble();
    return averageMz;
  }

  @Override
  public Float getRT() {
    Collection<Feature> allFeatures = features.values();
    float averageRt =
        (float) allFeatures.stream().mapToDouble(Feature::getRetentionTime).average().getAsDouble();
    return averageRt;
  }

  @Override
  public Feature getFeature(Sample sample) {
    return features.get(sample);
  }

  @Override
  public Feature getFeature(Integer index) {
    assert featureTable != null;
    List<Sample> samples = featureTable.getSamples();
    return getFeature(samples.get(index));
  }


}
