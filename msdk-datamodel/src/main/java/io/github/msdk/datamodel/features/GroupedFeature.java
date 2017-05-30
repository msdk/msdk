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

package io.github.msdk.datamodel.features;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A grouped MS feature that consists of several individual features (m/z and RT pairs). A typical
 * example is several isotopes grouped together, or several adducts of the same ion (M+H, M+Na,
 * etc).
 * 
 */
public interface GroupedFeature extends Feature {

  @Nonnull
  List<Feature> getIndividualFeatures();

  void setIndividualFeatures(@Nonnull List<Feature> features);

  @Nullable
  Integer getCharge();

  void setCharge(@Nullable Integer charge);

}
