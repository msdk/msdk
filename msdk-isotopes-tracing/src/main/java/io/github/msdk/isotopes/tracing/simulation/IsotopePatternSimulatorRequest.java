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

package io.github.msdk.isotopes.tracing.simulation;

import io.github.msdk.isotopes.tracing.data.FragmentList;
import io.github.msdk.isotopes.tracing.data.IncorporationRate;
import io.github.msdk.isotopes.tracing.data.constants.Element;
import io.github.msdk.isotopes.tracing.data.constants.IntensityType;

/**
 * A request class to specify simulation options. A general {@link IsotopePatternSimulatorRequest}
 * uses the following standards: <br>
 * incorporation rate: 0.1 <br>
 * total number of fragments: 100000 <br>
 * minimal relative mass frequencies: 0.003 <br>
 * mass precision: 4 <br>
 * mass frequency precision: 4 <br>
 * analyze mass shifts: false <br>
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class IsotopePatternSimulatorRequest {

  private IncorporationRate incorporationRate = new IncorporationRate(0.1);
  private Double totalNumberOfFragments = 100000.0;
  private Double minimalFrequency = 0.1;
  private Integer roundedMassPrecision = 4;
  private Integer roundedFrequenciesPrecision = 4;
  private FragmentList fragments;
  private Boolean analyzeMassShifts = false;
  private IntensityType targetIntensityType = IntensityType.RELATIVE;
  private int charge = 1;
  /*
   * only for independent tracer incorporation
   */
  private Element tracer1;
  private Element tracer2;
  private IncorporationRate tracer1Inc;
  private IncorporationRate tracer2Inc;
  private IncorporationRate tracerAllInc;

  /**
   * Creates a general {@link IsotopePatternSimulatorRequest} using the following standards: <br>
   * incorporation rate: 1 <br>
   * total number of fragments: 100000 <br>
   * minimal relative mass frequencies: 0.003 <br>
   * mass precision: 4 <br>
   * mass frequency precision: 4 <br>
   * analyze mass shifts: false <br>
   * frequency type: RELATIVE <br>
   */
  public IsotopePatternSimulatorRequest() {

  }

  /**
   * Creates a {@link IsotopePatternSimulatorRequest} using the specified parameters. If a parameter
   * is null the following standards will be used: <br>
   * incorporation rate: 1 <br>
   * total number of fragments: 100000 <br>
   * minimal relative mass frequencies: 0.003 <br>
   * mass precision: 4 <br>
   * mass frequency precision: 4 <br>
   * analyze mass shifts: false <br>
   * target frequency type: RELATIVE <br>
   * <br>
   * 
   * @param incorporationRate
   * @param totalNumberOfFragments
   * @param minimalRelativeFrequency
   * @param roundedMassPrecision
   * @param roundedFrequenciesPrecision
   * @param fragments
   * @param analyzeMassShifts
   */
  public IsotopePatternSimulatorRequest(IncorporationRate incorporationRate,
      Double totalNumberOfFragments, IntensityType targetIntensityType,
      Double minimalRelativeFrequency, Integer roundedMassPrecision,
      Integer roundedFrequenciesPrecision, FragmentList fragments, Boolean analyzeMassShifts) {
    if (incorporationRate != null) {
      this.incorporationRate = incorporationRate;
    }
    if (totalNumberOfFragments != null) {
      this.totalNumberOfFragments = totalNumberOfFragments;
    }
    if (minimalRelativeFrequency != null) {
      this.minimalFrequency = minimalRelativeFrequency;
    }
    if (roundedMassPrecision != null) {
      this.roundedMassPrecision = roundedMassPrecision;
    }
    if (roundedFrequenciesPrecision != null) {
      this.roundedFrequenciesPrecision = roundedFrequenciesPrecision;
    }
    if (analyzeMassShifts != null) {
      this.setAnalyzeMassShifts(analyzeMassShifts);
    }
    if (targetIntensityType != null) {
      this.targetIntensityType = targetIntensityType;
    }
    this.fragments = fragments;
  }

  public IncorporationRate getIncorporationRate() {
    return incorporationRate;
  }

  public void setIncorporationRate(IncorporationRate incorporationRate) {
    this.incorporationRate = incorporationRate;
  }

  public Double getTotalNumberOfFragments() {
    return totalNumberOfFragments;
  }

  public void setTotalNumberOfFragments(Double totalNumberOfFragments) {
    this.totalNumberOfFragments = totalNumberOfFragments;
  }

  public Double getMinimalFrequency() {
    return minimalFrequency;
  }

  public void setMinimalFrequency(Double minimalFrequency) {
    this.minimalFrequency = minimalFrequency;
  }

  public Integer getRoundedMassPrecision() {
    return roundedMassPrecision;
  }

  public void setRoundedMassPrecision(Integer roundedMassPrecision) {
    this.roundedMassPrecision = roundedMassPrecision;
  }

  public Integer getRoundedFrequenciesPrecision() {
    return roundedFrequenciesPrecision;
  }

  public void setRoundedFrequenciesPrecision(Integer roundedFrequenciesPrecision) {
    this.roundedFrequenciesPrecision = roundedFrequenciesPrecision;
  }

  public FragmentList getFragments() {
    return fragments;
  }

  public void setFragments(FragmentList fragments) {
    this.fragments = fragments;
  }

  /**
   * @return the analyzeMassShifts
   */
  public Boolean getAnalyzeMassShifts() {
    return analyzeMassShifts;
  }

  /**
   * @param analyzeMassShifts the analyzeMassShifts to set
   */
  public void setAnalyzeMassShifts(Boolean analyzeMassShifts) {
    this.analyzeMassShifts = analyzeMassShifts;
  }

  public IntensityType getTargetIntensityType() {
    return targetIntensityType;
  }

  public void setTargetIntensityType(IntensityType intensityType) {
    this.targetIntensityType = intensityType;
  }

  public int getCharge() {
    return charge;
  }

  public void setCharge(int charge) {
    this.charge = charge;
  }

  public Element getTracer1() {
    return tracer1;
  }

  public void setTracer1(Element tracer1) {
    this.tracer1 = tracer1;
  }

  public Element getTracer2() {
    return tracer2;
  }

  public void setTracer2(Element tracer2) {
    this.tracer2 = tracer2;
  }

  public IncorporationRate getTracer1Inc() {
    return tracer1Inc;
  }

  public void setTracer1Inc(IncorporationRate tracer1Inc) {
    this.tracer1Inc = tracer1Inc;
  }

  public IncorporationRate getTracer2Inc() {
    return tracer2Inc;
  }

  public void setTracer2Inc(IncorporationRate tracer2Inc) {
    this.tracer2Inc = tracer2Inc;
  }

  public IncorporationRate getTracerAllInc() {
    return tracerAllInc;
  }

  public void setTracerAllInc(IncorporationRate tracerAllInc) {
    this.tracerAllInc = tracerAllInc;
  }

}
