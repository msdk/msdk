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

package io.github.msdk.io.mzml2.data;

/**
 * Controlled vocabulary (CV) values for mzML files.
 *
 * @see <a href=
 *      "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo">
 *      Official CV specification</a>
 */
public class MzMLCV {

  // Scan start time
  public static final String MS_RT_SCAN_START = "MS:1000016"; // "scan start time"
  public static final String MS_RT_RETENTION_TIME = "MS:1000894"; // "retention time"
  public static final String MS_RT_RETENTION_TIME_LOCAL = "MS:1000895"; // "local retention time"
  public static final String MS_RT_RETENTION_TIME_NORMALIZED = "MS:1000896"; // "normalized
                                                                             // retention time"

  // MS level
  public static final String cvMSLevel = "MS:1000511";
  public static final String cvMS1Spectrum = "MS:1000579";

  // m/z and charge state
  public static final String cvMz = "MS:1000040";
  public static final String cvChargeState = "MS:1000041";

  // Minutes unit. MS:1000038 is used in mzML 1.0, while UO:000003 is used in
  // mzML 1.1.0
  public static final String cvUnitsMin1 = "MS:1000038";
  public static final String cvUnitsMin2 = "UO:0000031";
  public static final String cvUnitsSec = "UO:0000010";

  // Scan filter string
  public static final String cvScanFilterString = "MS:1000512";

  // Precursor m/z.
  public static final String cvPrecursorMz = "MS:1000744";

  // Polarity
  public static final String cvPolarityPositive = "MS:1000130";
  public static final String cvPolarityNegative = "MS:1000129";
  public static final MzMLCVParam polarityPositiveCvParam =
      new MzMLCVParam(cvPolarityPositive, "", "positive scan", null);
  public static final MzMLCVParam polarityNegativeCvParam =
      new MzMLCVParam(cvPolarityNegative, "", "negative scan", null);

  // Centroid vs profile
  public static final String cvCentroidSpectrum = "MS:1000127";
  public static final String cvProfileSpectrum = "MS:1000128";
  public static final MzMLCVParam centroidCvParam =
      new MzMLCVParam(cvCentroidSpectrum, "", "centroid mass spectrum", null);
  public static final MzMLCVParam profileCvParam =
      new MzMLCVParam(cvProfileSpectrum, "", "profile spectrum", null);


  // Total Ion Current
  public static final String cvTIC = "MS:1000285";

  // m/z range
  public static final String cvLowestMz = "MS:1000528";
  public static final String cvHighestMz = "MS:1000527";

  // Scan window range

  public static final String cvScanWindowUpperLimit = "MS:1000500";
  public static final String cvScanWindowLowerLimit = "MS:1000501";


  // Chromatograms
  public static final String cvChromatogramTIC = "MS:1000235";
  public static final String cvChromatogramMRM_SRM = "MS:1001473";
  public static final String cvChromatogramSIC = "MS:1000627";
  public static final String cvChromatogramBPC = "MS:1000628";

  // Activation
  public static final String cvActivationEnergy = "MS:1000045";
  public static final String cvActivationCID = "MS:1000133";

  // Isolation
  public static final String cvIsolationWindowTarget = "MS:1000827";
  public static final String cvIsolationWindowLowerOffset = "MS:1000828";
  public static final String cvIsolationWindowUpperOffset = "MS:1000829";

  // Data arrays
  public static final String cvMzArray = "MS:1000514";
  public static final String cvIntensityArray = "MS:1000515";
  public static final String cvRetentionTimeArray = "MS:1000595";

  // UV spectrum, actually "electromagnetic radiation spectrum"
  public static final String cvUVSpectrum = "MS:1000804";

  // Intensity array unit
  public static final String cvUnitsIntensity1 = "MS:1000131";

}
