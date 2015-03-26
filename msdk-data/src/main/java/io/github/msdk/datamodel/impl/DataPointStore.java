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

package io.github.msdk.datamodel.impl;

import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrum;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This abstract class represents a storage mechanism for arrays of DataPoints.
 * Each RawDataFile and PeakList will use this mechanism to store their data
 * points on the disk, so these do not consume memory.
 * 
 * @see SimpleRawDataFile
 * @see SimplePeakList
 */
abstract class DataPointStore {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final File tmpDataFileName;
    private final RandomAccessFile tmpDataFile;

    private ByteBuffer byteBuffer;
    private final TreeMap<Integer, Long> dataPointsOffsets;
    private final TreeMap<Integer, Integer> dataPointsLengths;
    private int lastStorageId = 0;

    DataPointStore() {

        try {
            tmpDataFileName = File.createTempFile("msdk", ".tmp");
            tmpDataFile = new RandomAccessFile(tmpDataFileName, "rw");

            /*
             * Locks the temporary file.
             */
            FileChannel fileChannel = tmpDataFile.getChannel();
            fileChannel.lock();

            tmpDataFileName.deleteOnExit();

        } catch (IOException e) {
            throw new RuntimeException(
                    "I/O error while creating a new MSDK object", e);
        }

        byteBuffer = ByteBuffer.allocate(20000);
        dataPointsOffsets = new TreeMap<Integer, Long>();
        dataPointsLengths = new TreeMap<Integer, Integer>();

    }

    /**
     * Stores new array of data points.
     * 
     * @return Storage ID for the newly stored data.
     */
    synchronized int storeDataPoints(@Nonnull MassSpectrum dataPoints) {

        long currentOffset;
        try {
            currentOffset = tmpDataFile.length();

            final int numOfDataPoints = dataPoints.size();

            /*
             * Convert the data points into a byte array. Each double takes 8
             * bytes, so we get the current float offset by dividing the size of
             * the file by 8.
             */
            final int numOfBytes = numOfDataPoints * 2 * 8;

            if (byteBuffer.capacity() < numOfBytes) {
                byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
            } else {
                byteBuffer.clear();
            }

            DoubleBuffer dblBuffer = byteBuffer.asDoubleBuffer();
            for (DataPoint dp : dataPoints) {
                dblBuffer.put(dp.getMz());
                dblBuffer.put(dp.getIntensity());
            }

            tmpDataFile.seek(currentOffset);
            tmpDataFile.write(byteBuffer.array(), 0, numOfBytes);

            // Increase the storage ID
            lastStorageId++;

            dataPointsOffsets.put(lastStorageId, currentOffset);
            dataPointsLengths.put(lastStorageId, numOfDataPoints);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return lastStorageId;

    }

    /**
     * Reads the data points associated with given ID.
     */
    synchronized @Nonnull MassSpectrum readDataPoints(int ID) {

        final Long currentOffset = dataPointsOffsets.get(ID);
        final Integer numOfDataPoints = dataPointsLengths.get(ID);

        if ((currentOffset == null) || (numOfDataPoints == null)) {
            throw new RuntimeException("Unknown storage ID " + ID);
        }

        final int numOfBytes = numOfDataPoints * 2 * 8;

        if (byteBuffer.capacity() < numOfBytes) {
            byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
        } else {
            byteBuffer.clear();
        }

        try {
            tmpDataFile.seek(currentOffset);
            tmpDataFile.read(byteBuffer.array(), 0, numOfBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        DoubleBuffer dblBuffer = byteBuffer.asDoubleBuffer();

        DataPoint dataPoints[] = new DataPoint[numOfDataPoints];
        double mz;
        float intensity;

        for (int i = 0; i < numOfDataPoints; i++) {
            mz = dblBuffer.get();
            intensity = (float) dblBuffer.get();
            dataPoints[i] = MSDKObjectBuilder.getDataPoint(mz, intensity);
        }

        return null;

    }

    /**
     * Remove data associated with given storage ID. We do not attempt to remove
     * the data from disk, simply remove the reference to it.
     */
    synchronized void removeStoredDataPoints(long ID) {
        dataPointsOffsets.remove(ID);
        dataPointsLengths.remove(ID);
    }

    public synchronized void dispose() {
        if (!tmpDataFileName.exists())
            return;
        try {
            tmpDataFile.close();
            tmpDataFileName.delete();
        } catch (IOException e) {
            logger.warning("Could not close and remove temporary file "
                    + tmpDataFileName + ": " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * When this object is garbage collected, remove the associated temporary
     * data file from disk.
     */
    @Override
    protected void finalize() {
        dispose();
    }
}
