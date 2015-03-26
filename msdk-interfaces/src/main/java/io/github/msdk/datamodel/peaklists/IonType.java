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

package io.github.msdk.datamodel.peaklists;

import io.github.msdk.datamodel.rawdata.PolarityType;

import javax.annotation.concurrent.Immutable;

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * 
 *
 */
@Immutable
public interface IonType {

    /**
     * @return Name of ionization type, such as [M+2H]2+
     */
    int getName();

    PolarityType getPolarity();

    int getNumberOfMolecules();

    String getAdductFormula();

    String getCharge();

}
