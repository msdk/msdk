package io.github.msdk.io.mgf;

import java.util.Comparator;

/**
 * Created by evger on 17-May-18.
 */
public class MgfPairComparator implements Comparator<Pair> {

  @Override
  public int compare(Pair o1, Pair o2) {
    double k1 = (Double)o1.getKey();
    double k2 = (Double)o2.getKey();
    return k1 < k2 ? -1 : k1 == k2 ? 0 : 1;
  }
}
