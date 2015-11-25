/* 
 * (C) Copyright 2015 by MSDK Development Team
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

package io.github.msdk.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.msdk.datamodel.rawdata.ChromatographyInfo;

/**
 * <p>DataPointSorter class.</p>
 *
 */
public class DataPointSorter {

    public enum SortingProperty {
        MZ, INTENSITY
    }

    public enum SortingDirection {
        ASCENDING, DESCENDING
    }

    private static class DataPointComparator implements Comparator<Integer> {

        private final double mzBuffer[];
        private final float intensityBuffer[];
        private final SortingProperty prop;
        private final SortingDirection dir;

        DataPointComparator(final double mzBuffer[],
                final float intensityBuffer[], SortingProperty prop,
                SortingDirection dir) {
            this.mzBuffer = mzBuffer;
            this.intensityBuffer = intensityBuffer;
            this.prop = prop;
            this.dir = dir;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            switch (prop) {
            case INTENSITY:
                if (dir == SortingDirection.ASCENDING)
                    return Float.compare(intensityBuffer[i1],
                            intensityBuffer[i2]);
                else
                    return Float.compare(intensityBuffer[i2],
                            intensityBuffer[i1]);
            case MZ:
                if (dir == SortingDirection.ASCENDING)
                    return Double.compare(mzBuffer[i1], mzBuffer[i2]);
                else
                    return Double.compare(mzBuffer[i2], mzBuffer[i1]);
            }
            return 0;
        }

    }

    /**
     * Sort the given data points by m/z order
     *
     * @param mzBuffer an array of double.
     * @param intensityBuffer an array of float.
     * @param size a int.
     * @param prop a {@link io.github.msdk.util.DataPointSorter.SortingProperty} object.
     * @param dir a {@link io.github.msdk.util.DataPointSorter.SortingDirection} object.
     */
    public static void sortDataPoints(final double mzBuffer[],
            final float intensityBuffer[], final int size, SortingProperty prop,
            SortingDirection dir) {

        // Use Collections.sort to obtain index mapping from old arrays to the
        // new sorted array
        final List<Integer> idx = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            idx.add(i);
        Comparator<Integer> comp = new DataPointComparator(mzBuffer,
                intensityBuffer, prop, dir);
        Collections.sort(idx, comp);

        // Remap the values according to the index map idx
        for (int i = 0; i < size; i++) {
            final int newIndex = idx.get(i);
            if (newIndex == i)
                continue;

            final double tmpMz = mzBuffer[i];
            final float tmpInt = intensityBuffer[i];

            mzBuffer[i] = mzBuffer[newIndex];
            intensityBuffer[i] = intensityBuffer[newIndex];

            mzBuffer[newIndex] = tmpMz;
            intensityBuffer[newIndex] = tmpInt;

            final int swapIndex = idx.indexOf(i);
            idx.set(swapIndex, newIndex);

        }

    }

    /**
     * Sort the given data points by RT order
     *
     * @param rtBuffer an array of {@link io.github.msdk.datamodel.rawdata.ChromatographyInfo} objects.
     * @param intensityBuffer an array of float.
     * @param size a int.
     */
    public static void sortDataPoints(final ChromatographyInfo rtBuffer[],
            final float intensityBuffer[], final int size) {

        // Use Collections.sort to obtain index mapping from old arrays to the
        // new sorted array
        final List<Integer> idx = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            idx.add(i);
        Collections.sort(idx, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                return rtBuffer[i1].compareTo(rtBuffer[i2]);
            }
        });

        // Remap the values according to the index map idx
        for (int i = 0; i < size; i++) {
            final int newIndex = idx.get(i);
            if (newIndex == i)
                continue;

            final ChromatographyInfo tmpRt = rtBuffer[i];
            final float tmpInt = intensityBuffer[i];

            rtBuffer[i] = rtBuffer[newIndex];
            intensityBuffer[i] = intensityBuffer[newIndex];

            rtBuffer[newIndex] = tmpRt;
            intensityBuffer[newIndex] = tmpInt;

            final int swapIndex = idx.indexOf(i);
            idx.set(swapIndex, newIndex);

        }
    }

}
