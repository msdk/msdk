package io.github.msdk.io.mgf;

import java.util.Comparator;

/**
 * Created by evger on 17-May-18.
 */
public class MgfPairComparator implements Comparator<Pair> {

  @Override
  public int compare(Pair o1, Pair o2) {
    return (Double)o1.getKey() - (Double)o2.getKey() > 0 ? 1: 0;
  }
}
