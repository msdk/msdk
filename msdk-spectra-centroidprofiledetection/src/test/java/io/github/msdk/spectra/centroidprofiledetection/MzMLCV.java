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

package io.github.msdk.spectra.centroidprofiledetection;

import uk.ac.ebi.jmzml.model.mzml.CVParam;

/**
 * Controlled vocabulary (CV) values for mzML files.
 * 
 * @see <a href=
 *      "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo">
 *      Official CV specification</a>
 * 
 */
class MzMLCV {

  // Scan start time
  static final String cvScanStartTime = "MS:1000016";

  // MS level
  static final String cvMSLevel = "MS:1000511";
  static final String cvMS1Spectrum = "MS:1000579";

  // m/z and charge state
  static final String cvMz = "MS:1000040";
  static final String cvChargeState = "MS:1000041";

  // Minutes unit. MS:1000038 is used in mzML 1.0, while UO:000003 is used in
  // mzML 1.1.0
  static final String cvUnitsMin1 = "MS:1000038";
  static final String cvUnitsMin2 = "UO:0000031";
  static final String cvUnitsSec = "UO:0000010";

  // Scan filter string
  static final String cvScanFilterString = "MS:1000512";

  // Precursor m/z.
  static final String cvPrecursorMz = "MS:1000744";

  // Polarity
  static final String cvPolarityPositive = "MS:1000130";
  static final String cvPolarityNegative = "MS:1000129";
  static final CVParam polarityPositiveCvParam = new CVParam();
  static final CVParam polarityNegativeCvParam = new CVParam();

  // Centroid vs profile
  static final String cvCentroidSpectrum = "MS:1000127";
  static final String cvProfileSpectrum = "MS:1000128";
  static final CVParam centroidCvParam = new CVParam();
  static final CVParam profileCvParam = new CVParam();

  // Fill in the CVParam details
  static {
    polarityPositiveCvParam.setAccession(cvPolarityPositive);
    polarityPositiveCvParam.setName("positive scan");
    polarityNegativeCvParam.setAccession(cvPolarityNegative);
    polarityNegativeCvParam.setName("negative scan");
    centroidCvParam.setAccession(cvCentroidSpectrum);
    centroidCvParam.setName("centroid mass spectrum");
    profileCvParam.setAccession(cvProfileSpectrum);
    profileCvParam.setName("profile spectrum");
  }

  // Chromatograms
  static final String cvChromatogramTIC = "MS:1000235";
  static final String cvChromatogramMRM_SRM = "MS:1001473";
  static final String cvChromatogramSIC = "MS:1000627";
  static final String cvChromatogramBPC = "MS:1000628";

  // Activation
  static final String cvActivationEnergy = "MS:1000045";
  static final String cvActivationCID = "MS:1000133";

  // Isolation
  static final String cvIsolationWindowTarget = "MS:1000827";
  static final String cvIsolationWindowLowerOffset = "MS:1000828";
  static final String cvIsolationWindowUpperOffset = "MS:1000829";

  // Data arrays
  static final String cvMzArray = "MS:1000514";
  static final String cvIntensityArray = "MS:1000515";

  // UV spectrum, actually "electromagnetic radiation spectrum"
  static final String cvUVSpectrum = "MS:1000804";

}
