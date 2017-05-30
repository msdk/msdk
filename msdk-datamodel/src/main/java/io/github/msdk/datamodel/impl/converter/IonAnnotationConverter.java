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

package io.github.msdk.datamodel.impl.converter;

import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableDataConverter;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Implementation of {@link FeatureTableDataConverter} for lists of {@link IonAnnotation}
 * </p>
 */
public class IonAnnotationConverter implements FeatureTableDataConverter<List<IonAnnotation>> {

  /** {@inheritDoc} */
  @Override
  public void apply(FeatureTableRow sourceRow,
      FeatureTableColumn<? extends List<IonAnnotation>> sourceColumn, FeatureTableRow targetRow,
      FeatureTableColumn<? extends List<IonAnnotation>> targetColumn) {
    List<IonAnnotation> targetIonAnnotations = targetRow.getData(targetColumn);
    List<IonAnnotation> sourceIonAnnotations = sourceRow.getData(sourceColumn);
    if (targetIonAnnotations == null) {
      targetIonAnnotations = new ArrayList<>();
    }
    if (sourceIonAnnotations != null) {
      for (IonAnnotation ionAnnotation : sourceIonAnnotations) {
        if (!ionAnnotation.isNA()) {
          boolean addIon = true;
          for (IonAnnotation targetIonAnnotation : targetIonAnnotations) {
            if (targetIonAnnotation.compareTo(ionAnnotation) == 0) {
              addIon = false;
            }
          }
          if (addIon) {
            targetIonAnnotations.add(ionAnnotation);
          }
        }
      }
      targetRow.setData(targetColumn, targetIonAnnotations);
    }
  }

}
