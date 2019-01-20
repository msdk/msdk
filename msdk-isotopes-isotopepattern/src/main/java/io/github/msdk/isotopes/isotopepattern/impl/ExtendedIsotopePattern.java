package io.github.msdk.isotopes.isotopepattern.impl;

import javax.annotation.Nonnull;

import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;

public class ExtendedIsotopePattern extends SimpleMsSpectrum {

  private String description;
  private String[] isotopeComposition;

  public ExtendedIsotopePattern(@Nonnull double mzValues[], @Nonnull float intensityValues[],
      @Nonnull Integer size, @Nonnull MsSpectrumType spectrumType, String description,
      String[] isotopeComposition) {
    super(mzValues, intensityValues, size, spectrumType);
    this.description = description;
    this.isotopeComposition = isotopeComposition;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getIsotopeComposition() {
    return isotopeComposition;
  }

  public void setIsotopeComposition(String[] isotopeCompostion) {
    this.isotopeComposition = isotopeCompostion;
  }
}
