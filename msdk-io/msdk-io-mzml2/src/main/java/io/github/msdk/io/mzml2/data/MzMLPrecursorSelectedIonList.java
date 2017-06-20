package io.github.msdk.io.mzml2.data;

import java.util.ArrayList;

public class MzMLPrecursorSelectedIonList {
  private ArrayList<MzMLPrecursorSelectedIon> selectedIonList;

  public MzMLPrecursorSelectedIonList() {
    this.selectedIonList = new ArrayList<>();
  }

  public ArrayList<MzMLPrecursorSelectedIon> getPrecursorList() {
    return selectedIonList;
  }

  public void addSelectedIon(MzMLPrecursorSelectedIon e) {
    selectedIonList.add(e);
  }
}
