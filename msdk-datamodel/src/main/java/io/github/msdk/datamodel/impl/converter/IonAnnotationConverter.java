/*
 * Copyright (C) 2015 nilshoffmann.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.msdk.datamodel.impl.converter;

import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableDataConverter;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nilshoffmann
 */
public class IonAnnotationConverter implements
        FeatureTableDataConverter<List<IonAnnotation>> {

    @Override
    public void apply(FeatureTableRow sourceRow,
            FeatureTableColumn<? extends List<IonAnnotation>> sourceColumn,
            FeatureTableRow targetRow,
            FeatureTableColumn<? extends List<IonAnnotation>> targetColumn) {
        List<IonAnnotation> targetIonAnnotations = targetRow
                .getData(targetColumn);
        List<IonAnnotation> sourceIonAnnotations = sourceRow
                .getData(sourceColumn);
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
