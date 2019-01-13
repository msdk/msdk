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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class PartitionTest extends TestCase {
  private static final Logger LOG = LoggerFactory.getLogger(PartitionTest.class);

  public void testEqualsUpToPermutation() {
    Partition partition1 = new Partition(1, 2, 3, 0, 0, 0);
    Partition partition2 = new Partition(1, 2, 3, 0, 0);
    Partition partition3 = new Partition(1, 2, 4, 0, 0);
    Partition partition4 = new Partition(1, 0, 3, 0, 0, 0);
    Partition partition5 = new Partition(1, 0, 3, 0, 0, 2);

    assertTrue(partition1.equalsUpToPermutation(partition5));
    assertTrue(partition5.equalsUpToPermutation(partition1));
    assertTrue(!partition1.equalsUpToPermutation(partition2));
    assertTrue(!partition2.equalsUpToPermutation(partition1));
    assertTrue(!partition2.equalsUpToPermutation(partition3));
    assertTrue(!partition3.equalsUpToPermutation(partition2));
    assertTrue(!partition1.equalsUpToPermutation(partition4));
    assertTrue(!partition4.equalsUpToPermutation(partition1));
  }

  public void testAllPermutations() {
    Partition partition = new Partition(1, 2, 0);
    LOG.info("partition" + partition);
    PermutationSet expected =
        new PermutationSet(new Partition(0, 1, 2), new Partition(1, 0, 2), new Partition(1, 2, 0),
            new Partition(0, 2, 1), new Partition(2, 0, 1), new Partition(2, 1, 0));
    PermutationSet actual = partition.allPermutations();
    LOG.info("allPermutations" + actual);
    assertEquals(expected, actual);
  }

  public void testAllPermutations2() {
    Partition partition = new Partition(1, 1, 0);
    LOG.info("partition" + partition);
    PermutationSet expected =
        new PermutationSet(new Partition(0, 1, 1), new Partition(1, 0, 1), new Partition(1, 1, 0));
    PermutationSet actual = partition.allPermutations();
    LOG.info("allPermutations" + actual);
    assertEquals(expected, actual);
  }

  public void testAllPermutations3() {
    Partition partition = new Partition(2, 2, 2);
    LOG.info("partition" + partition);
    PermutationSet expected = new PermutationSet(new Partition(2, 2, 2));
    PermutationSet actual = partition.allPermutations();
    LOG.info("allPermutations" + actual);
    assertEquals(expected, actual);
  }

}
