package io.github.msdk.io.mzml2.data;

import java.util.ArrayList;


public class MzMLPrecursorList {

  private ArrayList<MzMLPrecursorElement> precursorElements;

  public MzMLPrecursorList() {
    this.precursorElements = new ArrayList<>();
  }

  public ArrayList<MzMLPrecursorElement> getPrecursorElements() {
    return precursorElements;
  }

  public void addPrecursor(MzMLPrecursorElement precursor) {
    precursorElements.add(precursor);
  }
}
