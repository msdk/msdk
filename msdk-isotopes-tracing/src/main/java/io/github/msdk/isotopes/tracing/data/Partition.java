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

package io.github.msdk.isotopes.tracing.data;

import java.util.ArrayList;

/**
 * If we represent a natural number n as sum of non negative integers n_0,...,n_{k-1} for some
 * natural k < n, then the collection (n_0,...,n_{k-1}) is called a {@link Partition} of n. We will
 * define the length/size of a {@link Partition} (n_0,...,n_{k-1}) as the natural number k in this
 * representation. Two {@link Partition}s are considered to be equal up to permutation if they have
 * the same size and the same set of non zero entries.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class Partition extends ArrayList<Integer> {

  public Partition() {

  }

  public Partition(Integer... summands) {
    for (Integer summand : summands) {
      if (summand != null) {
        this.add(summand);
      }
    }
  }

  /**
   * Two {@link Partition}s are considered to be equal up to permutation if they have the same size
   * and the same set of non zero entries.
   * 
   * @param other
   * @return
   */
  public boolean equalsUpToPermutation(Partition other) {
    if (other == null) {
      return false;
    }
    if (this.size() != other.size()) {
      return false;
    }
    for (int index = 0; index < this.size(); index++) {
      if (!other.contains(this.get(index)) || !this.contains(other.get(index))) {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @return a set of all permutations ( {@link PermutationSet} ) of this {@link Partition}
   */
  public PermutationSet allPermutations() {
    PermutationSet permutationSet = new PermutationSet();
    Partition start1 = new Partition(this.get(0), this.get(1));
    permutationSet.add(start1);
    Partition start2 = new Partition(this.get(1), this.get(0));
    permutationSet.add(start2);
    for (int index = 2; index < this.size(); index++) {
      Integer summand = this.get(index);
      PermutationSet temPermutationSet = new PermutationSet();
      for (int index2 = 0; index2 < permutationSet.size(); index2++) {
        Partition currentPartition = permutationSet.get(index2);
        for (int insertionPoint = 0; insertionPoint < index + 1; insertionPoint++) {
          Partition newPartition = currentPartition.copy();
          newPartition.add(insertionPoint, summand);
          if (!(permutationSet.contains(newPartition)
              || temPermutationSet.contains(newPartition))) {
            temPermutationSet.add(newPartition);
          }
        }
      }
      permutationSet = temPermutationSet.copy();
      temPermutationSet.clear();
    }
    return permutationSet;
  }

  /**
   * 
   * @return a copy of this {@link Partition}
   */
  public Partition copy() {
    Partition newPartition = new Partition();
    for (Integer summand : this) {
      newPartition.add(summand);
    }
    return newPartition;
  }

}
