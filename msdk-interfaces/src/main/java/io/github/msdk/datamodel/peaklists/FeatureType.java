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

/* 
 * WARNING: the interfaces in this package are still under construction
 */

/**
 * 
 */
public enum FeatureType {

    /**
     * Peak was found in primary peak picking
     */
    DETECTED,

    /**
     * Peak was estimated in secondary peak picking (gap filling)
     */
    ESTIMATED,

    /**
     * Peak was defined manually
     */
    MANUAL,

    /**
     * Unknown type
     */
    UNKNOWN;

}
