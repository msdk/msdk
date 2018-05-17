package io.github.msdk.io.mgf;

import com.google.common.collect.Range;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.util.tolerances.MzTolerance;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MgfMsSpectrum implements MsSpectrum {
  private MsSpectrumType type;
  private double[] mz;
  private float[] intensive;
  private String title;
  private int precursorCharge;
  private double precursorMass;
  private int size;

  public MgfMsSpectrum(double[] mz, float[] intensive, int size, String title, int precursorCharge,
      double precursorMass, MsSpectrumType type) {
    this.title = title;
    this.mz = mz;
    this.intensive = intensive;
    this.precursorMass = precursorMass;
    this.precursorCharge = precursorCharge;
    this.size = size;
    this.type = type;
  }

  @Nonnull
  @Override
  public MsSpectrumType getSpectrumType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public double getPrecursorMass() {
    return precursorMass;
  }

  public int getPrecursorCharge() {
    return precursorCharge;
  }

  @Nonnull
  @Override
  public Integer getNumberOfDataPoints() {
    return size;
  }

  @Nonnull
  @Override
  public double[] getMzValues() {
    return mz;
  }

  @Nonnull
  @Override
  public double[] getMzValues(double[] array) {
    return new double[0];
  }

  @Nonnull
  @Override
  public float[] getIntensityValues() {
    return intensive;
  }

  @Nonnull
  @Override
  public float[] getIntensityValues(float[] array) {
    return new float[0];
  }

  @Nonnull
  @Override
  public Float getTIC() {
    return null;
  }

  @Nullable
  @Override
  public Range<Double> getMzRange() {
    return null;
  }

  @Nullable
  @Override
  public MzTolerance getMzTolerance() {
    return null;
  }
}
