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

package io.github.msdk.datamodel.rawdata;

/**
 * Represents the type of MS/MS experiment.
 */
public enum MsMsExperimentType {

    /**
     * Collision-induced dissociation
     */
    CID,

    /**
     * Higher-energy C-trap dissociation
     */
    HCD,

    /**
     * Electron-capture dissociation
     */
    ECD,

    /**
     * Electron transfer dissociation
     */
    ETD,

    /**
     * Unknown MS/MS experiment type
     */
    UNKNOWN;

}
