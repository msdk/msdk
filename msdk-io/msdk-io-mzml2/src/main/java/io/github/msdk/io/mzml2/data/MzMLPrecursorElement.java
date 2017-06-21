/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

import java.util.Optional;

public class MzMLPrecursorElement {

  private final Optional<String> spectrumRef;
  private Optional<MzMLPrecursorIsolationWindow> isolationWindow;
  private Optional<MzMLPrecursorSelectedIonList> selectedIonList;
  private MzMLPrecursorActivation activation;


  public MzMLPrecursorElement(String spectrumRef) {
    this.spectrumRef = Optional.ofNullable(spectrumRef);
    this.isolationWindow = Optional.ofNullable(null);
    this.selectedIonList = Optional.ofNullable(null);
  }

  public Optional<String> getSpectrumRef() {
    return spectrumRef;
  }

  public Optional<MzMLPrecursorIsolationWindow> getIsolationWindow() {
    return isolationWindow;
  }

  public Optional<MzMLPrecursorSelectedIonList> getSelectedIonList() {
    return selectedIonList;
  }

  public MzMLPrecursorActivation getActivation() {
    return activation;
  }

  public void setIsolationWindow(MzMLPrecursorIsolationWindow isolationWindow) {
    this.isolationWindow = Optional.ofNullable(isolationWindow);
  }

  public void setSelectedIonList(MzMLPrecursorSelectedIonList selectedIonList) {
    this.selectedIonList = Optional.ofNullable(selectedIonList);
  }

  public void setActivation(MzMLPrecursorActivation activation) {
    this.activation = activation;
  }

}
