/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.util.tolerances;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Range;

public class MaximumMzToleranceTest {

    @Test
    public void testGetToleranceRange() {
        // simple case
        MzTolerance tolerance = new MaximumMzTolerance(new ConstantDaTolerance(
                0.005), new ConstantDaTolerance(0.01));
        Range<Double> range = tolerance.getToleranceRange(400.0);
        assertEquals(range.lowerEndpoint(), 399.99, 1e-8);
        assertEquals(range.upperEndpoint(), 400.01, 1e-8);

        // test varargs in constructor
        tolerance = new MaximumMzTolerance(new ConstantDaTolerance(0.005),
                new ConstantDaTolerance(0.01), new ConstantDaTolerance(0.1));
        range = tolerance.getToleranceRange(400.0);
        assertEquals(range.lowerEndpoint(), 399.9, 1e-8);
        assertEquals(range.upperEndpoint(), 400.1, 1e-8);

        // test da and ppm
        tolerance = new MaximumMzTolerance(new ConstantDaTolerance(0.001),
                new ConstantPpmTolerance(5.0));
        range = tolerance.getToleranceRange(400.0);
        assertEquals(range.lowerEndpoint(), 399.998, 1e-8);
        assertEquals(range.upperEndpoint(), 400.002, 1e-8);

        range = tolerance.getToleranceRange(200.0);
        assertEquals(range.lowerEndpoint(), 199.999, 1e-8);
        assertEquals(range.upperEndpoint(), 200.001, 1e-8);
    }

}
