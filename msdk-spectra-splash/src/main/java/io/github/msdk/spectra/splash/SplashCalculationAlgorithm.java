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

package io.github.msdk.spectra.splash;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import edu.ucdavis.fiehnlab.spectra.hash.core.Spectrum;
import edu.ucdavis.fiehnlab.spectra.hash.core.Splash;
import edu.ucdavis.fiehnlab.spectra.hash.core.SplashFactory;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.Ion;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectrumImpl;
import io.github.msdk.datamodel.MsSpectrum;

/**
 * the reference implementation of the Spectral Hash Key
 */
public class SplashCalculationAlgorithm {

  private static final Splash splashFactory = SplashFactory.create();

  /**
   * calculates our spectral hash
   *
   * @param spectrum a {@link io.github.msdk.datamodel.MsSpectrum} object.
   * @return a {@link java.lang.String} object.
   */
  public static @Nonnull String calculateSplash(@Nonnull MsSpectrum spectrum) {

    return calculateSplash(spectrum.getMzValues(), spectrum.getIntensityValues(),
        spectrum.getNumberOfDataPoints());
  }

  /**
   * <p>
   * calculateSplash.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   * @param mzValues an array of double.
   * @param intValues an array of float.
   * @param size a {@link java.lang.Integer} object.
   */
  public static @Nonnull String calculateSplash(@Nonnull double mzValues[],
      @Nonnull float intValues[], @Nonnull Integer size) {

    // Parameter check
    Preconditions.checkNotNull(mzValues);
    Preconditions.checkNotNull(intValues);
    Preconditions.checkNotNull(size);

    // Convert the spectrum to a list of Ions
    ArrayList<Ion> ionList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Ion ion = new Ion(mzValues[i], intValues[i]);
      ionList.add(ion);
    }
    Spectrum spectrum = new SpectrumImpl(ionList, SpectraType.MS);

    // Call the Fiehn lab's SPLASH library
    String splash = splashFactory.splashIt(spectrum);
    Preconditions.checkNotNull(splash);

    return splash;
  }

}
