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

package io.github.msdk.io.mzdata;

/**
 * Controlled vocabulary (CV) values for mzData files.
 */
class MzDataCV {

  // Retention time
  static final String cvTimeMin = "PSI:1000038";
  static final String cvTimeSec = "PSI:1000039";

  // Polarity
  static final String cvPolarity = "PSI:1000037";

  // Precursor m/z
  static final String cvPrecursorMz = "PSI:1000040";

  // Precursor charge
  static final String cvPrecursorCharge = "PSI:1000041";

}
