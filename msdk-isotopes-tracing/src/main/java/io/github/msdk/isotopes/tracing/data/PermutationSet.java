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
import java.util.Comparator;

/**
 * A {@link PermutationSet} is a list of {@link Partition}s that are equal up to permutations
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class PermutationSet extends ArrayList<Partition> {

  public PermutationSet() {

  }

  public PermutationSet(Partition... partitions) {
    for (Partition partition : partitions) {
      this.add(partition);
    }
  }

  public PermutationSet copy() {
    PermutationSet newPermutationSet = new PermutationSet();
    for (Partition partition : this) {
      newPermutationSet.add(partition);
    }
    return newPermutationSet;
  }


  /**
   * creates a list of {@link PermutationSet}s where one {@link PermutationSet} corresponds to a
   * {@link Partition} of the numberOfElements and all its permutations. Only {@link Partition}s
   * with length equals to the numberOfIsotopes are considered.
   * 
   * @param numberOfIsotopes
   * @param numberOfElements
   * @return a list of {@link PermutationSet}s. One set corresponds to a {@link Partition} of the
   *         numberOfElements and all its permutations with length equals to the numberOfIsotopes.
   */
  public static ArrayList<PermutationSet> allIsotopeCombinations(int numberOfIsotopes,
      int numberOfElements) {
    ArrayList<PermutationSet> allPermutations = new ArrayList<>();
    if (numberOfIsotopes == 1) {
      PermutationSet permutationSet = new PermutationSet(new Partition(numberOfElements));
      allPermutations.add(permutationSet);
      return allPermutations;
    }
    if (numberOfIsotopes == 2) {
      for (int summand = numberOfElements; summand >= 0; summand--) {
        PermutationSet permutationSet =
            new PermutationSet(new Partition(summand, numberOfElements - summand));
        allPermutations.add(permutationSet);
      }
      return allPermutations;
    } else {
      Integer[] summands = new Integer[numberOfIsotopes];
      summands[0] = 0;
      int k = 1;
      int y = numberOfElements - 1;
      while (k != 0) {
        k--;
        int x = summands[k] + 1;
        while (2 * x <= y) {
          summands[k] = x;
          y = y - x;
          k++;
        }
        while (x <= y) {
          if (k + 1 < numberOfIsotopes) {
            summands[k] = x;
            summands[k + 1] = y;
            Partition partition = new Partition(summands);
            for (int index = k + 2; index <= numberOfIsotopes - 1; index++) {
              if (index < partition.size() && partition.get(index) != null) {
                partition.remove(index);
              }
              partition.add(index, 0);
            }
            partition.sort(new Comparator<Integer>() {

              @Override
              public int compare(Integer o1, Integer o2) {
                return -o1.compareTo(o2);
              }
            });
            allPermutations.add(partition.allPermutations());

          }
          x++;
          y--;
        }
        y = y + x - 1;
        if (k < numberOfIsotopes) {
          summands[k] = y + 1;
          Partition partition = new Partition(summands);
          for (int index = k + 1; index <= numberOfIsotopes - 1; index++) {
            if (index < partition.size() && partition.get(index) != null) {
              partition.remove(index);
            }
            partition.add(index, 0);
          }
          partition.sort(new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
              return -o1.compareTo(o2);
            }
          });
          allPermutations.add(partition.allPermutations());
        }
      }
      return allPermutations;
    }
  }

}
