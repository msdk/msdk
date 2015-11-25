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

package io.github.msdk.datamodel.datapointstore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.chromatograms.ChromatogramDataPointList;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

/**
 * A DataPointStore implementation that stores the data points in a temporary
 * file. This is a simple and efficient method, but has one disadvantage -
 * removing the data points is an expensive operation, so this implementation
 * actually only removes the reference but the data still remain in the
 * temporary file. If a single instance is continuously used to add and remove
 * data points, the file will grow indefinitely.
 * 
 * Since this class stores data on disk, there is a risk that IOException may
 * occur. If that happens, the IOException is wrapped in a MSDKRuntimeException
 * and thrown.
 * 
 * The methods of this class are synchronized, therefore it can be safely used
 * by multiple threads.
 */
class TmpFileDataPointStore implements DataPointStore {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final File tmpDataFileName;
    private final RandomAccessFile tmpDataFile;

    // Start with a ~20 KB byte buffer, that will be expanded based on needs
    private ByteBuffer byteBuffer = ByteBuffer.allocate(20000);

    private final HashMap<Object, Long> dataPointsOffsets = new HashMap<>();
    private final HashMap<Object, Integer> dataPointsLengths = new HashMap<>();

    private int lastStorageId = 0;

    TmpFileDataPointStore() throws MSDKException {

        try {
            tmpDataFileName = File.createTempFile("msdk", ".tmp");

            logger.debug("Initializing a new tmp-file data store in "
                    + tmpDataFileName);

            tmpDataFile = new RandomAccessFile(tmpDataFileName, "rw");

            /*
             * Lock the temporary file.
             */
            FileChannel fileChannel = tmpDataFile.getChannel();
            fileChannel.lock();

            tmpDataFileName.deleteOnExit();

        } catch (IOException e) {
            throw new MSDKException(e);
        }

    }

    /**
     * {@inheritDoc}
     *
     * Stores new array of data points.
     */
    @Override
    synchronized public @Nonnull Integer storeDataPoints(
            @Nonnull MsSpectrumDataPointList dataPoints) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        try {
            final long currentOffset = tmpDataFile.length();

            final int numOfDataPoints = dataPoints.getSize();

            // Calculate minimum necessary size of the byte buffer
            int numOfBytes = numOfDataPoints * (Double.SIZE / 8);

            // Make sure we have enough space in the byte buffer
            if (byteBuffer.capacity() < numOfBytes) {
                byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
            } else {
                byteBuffer.clear();
            }

            // Write the m/z values into the file
            final DoubleBuffer dblBuffer = byteBuffer.asDoubleBuffer();
            dblBuffer.put(dataPoints.getMzBuffer(), 0, numOfDataPoints);
            tmpDataFile.seek(currentOffset);
            tmpDataFile.write(byteBuffer.array(), 0, numOfBytes);

            // Write the intensity values into the file
            numOfBytes = numOfDataPoints * (Float.SIZE / 8);
            final FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
            fltBuffer.put(dataPoints.getIntensityBuffer(), 0, numOfDataPoints);
            tmpDataFile.write(byteBuffer.array(), 0, numOfBytes);

            // Increase the storage ID
            lastStorageId++;

            // Save the reference to the new items
            dataPointsOffsets.put(lastStorageId, currentOffset);
            dataPointsLengths.put(lastStorageId, numOfDataPoints);

        } catch (IOException e) {
            throw new MSDKRuntimeException(e);
        }

        return lastStorageId;

    }

    /** {@inheritDoc} */
    @Override
    synchronized public @Nonnull Integer storeDataPoints(
            @Nonnull ChromatogramDataPointList dataPoints) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        try {
            final long currentOffset = tmpDataFile.length();

            final int numOfDataPoints = dataPoints.getSize();

            // Calculate minimum necessary size of the byte buffer
            int numOfBytes = numOfDataPoints * (Float.SIZE / 8) * 3;

            // Make sure we have enough space in the byte buffer
            if (byteBuffer.capacity() < numOfBytes) {
                byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
            } else {
                byteBuffer.clear();
            }

            final ChromatographyInfo retentionTimes[] = dataPoints
                    .getRtBuffer();

            FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
            Float f;
            for (ChromatographyInfo ch : retentionTimes) {
                f = ch.getRetentionTime();
                if (f == null)
                    f = Float.NaN;
                fltBuffer.put(f);
                f = ch.getSecondaryRetentionTime();
                if (f == null)
                    f = Float.NaN;
                fltBuffer.put(f);
                f = ch.getIonDriftTime();
                if (f == null)
                    f = Float.NaN;
                fltBuffer.put(f);
            }
            tmpDataFile.write(byteBuffer.array(), 0, numOfBytes);

            fltBuffer = byteBuffer.asFloatBuffer();
            fltBuffer.put(dataPoints.getIntensityBuffer(), 0, numOfDataPoints);
            tmpDataFile.write(byteBuffer.array(), 0, numOfBytes);

            // Increase the storage ID
            lastStorageId++;

            // Save the reference to the new items
            dataPointsOffsets.put(lastStorageId, currentOffset);
            dataPointsLengths.put(lastStorageId, numOfDataPoints);

        } catch (IOException e) {
            throw new MSDKRuntimeException(e);
        }

        return lastStorageId;
    }

    /**
     * {@inheritDoc}
     *
     * Reads the data points associated with given ID.
     */
    @Override
    synchronized public void readDataPoints(@Nonnull Object ID,
            @Nonnull MsSpectrumDataPointList list) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        if (!dataPointsLengths.containsKey(ID))
            throw new IllegalArgumentException("ID " + ID
                    + " not found in storage file " + tmpDataFileName);

        // Get file offset and number of data points
        final long offset = dataPointsOffsets.get(ID);
        final int numOfDataPoints = dataPointsLengths.get(ID);

        // Calculate minimum necessary size of the byte buffer
        int numOfBytes = numOfDataPoints * (Double.SIZE / 8);

        // Make sure we have enough space in the byte buffer
        if (byteBuffer.capacity() < numOfBytes) {
            byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
        } else {
            byteBuffer.clear();
        }

        try {

            // Read m/z values
            tmpDataFile.seek(offset);
            tmpDataFile.read(byteBuffer.array(), 0, numOfBytes);

            DoubleBuffer dblBuffer = byteBuffer.asDoubleBuffer();
            double mzValues[] = list.getMzBuffer();
            if (mzValues.length < numOfDataPoints)
                mzValues = new double[numOfDataPoints];
            dblBuffer.get(mzValues, 0, numOfDataPoints);

            // Read intensity values
            numOfBytes = numOfDataPoints * (Float.SIZE / 8);
            tmpDataFile.read(byteBuffer.array(), 0, numOfBytes);
            FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
            float intensityValues[] = list.getIntensityBuffer();
            if (intensityValues.length < numOfDataPoints)
                intensityValues = new float[numOfDataPoints];
            fltBuffer.get(intensityValues, 0, numOfDataPoints);

            // Update list
            list.setBuffers(mzValues, intensityValues, numOfDataPoints);

        } catch (IOException e) {
            throw new MSDKRuntimeException(e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public synchronized void readDataPoints(@Nonnull Object ID,
            @Nonnull ChromatogramDataPointList list) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        if (!dataPointsLengths.containsKey(ID))
            throw new IllegalArgumentException("ID " + ID
                    + " not found in storage file " + tmpDataFileName);

        // Get file offset and number of data points
        final long offset = dataPointsOffsets.get(ID);
        final int numOfDataPoints = dataPointsLengths.get(ID);

        // Calculate minimum necessary size of the byte buffer
        int numOfBytes = numOfDataPoints * (Float.SIZE / 8) * 3;

        // Make sure we have enough space in the byte buffer
        if (byteBuffer.capacity() < numOfBytes) {
            byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
        } else {
            byteBuffer.clear();
        }

        try {

            // Read m/z values
            tmpDataFile.seek(offset);
            tmpDataFile.read(byteBuffer.array(), 0, numOfBytes);

            FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
            ChromatographyInfo rtValues[] = list.getRtBuffer();
            if (rtValues.length < numOfDataPoints)
                rtValues = new ChromatographyInfo[numOfDataPoints];

            for (int i = 0; i < numOfDataPoints; i++) {
                Float rt = fltBuffer.get();
                if (rt == Float.NaN)
                    rt = null;
                Float srt = fltBuffer.get();
                if (srt == Float.NaN)
                    srt = null;
                Float idt = fltBuffer.get();
                if (idt == Float.NaN)
                    idt = null;
                rtValues[i] = MSDKObjectBuilder.getChromatographyInfo2D(
                        SeparationType.UNKNOWN, rt, srt);
            }

            // Read intensity values
            numOfBytes = numOfDataPoints * (Float.SIZE / 8);
            tmpDataFile.read(byteBuffer.array(), 0, numOfBytes);
            fltBuffer = byteBuffer.asFloatBuffer();
            float intensityValues[] = list.getIntensityBuffer();
            if (intensityValues.length < numOfDataPoints)
                intensityValues = new float[numOfDataPoints];
            fltBuffer.get(intensityValues, 0, numOfDataPoints);

            // Update list
            list.setBuffers(rtValues, intensityValues, numOfDataPoints);

        } catch (IOException e) {
            throw new MSDKRuntimeException(e);
        }

    }

    /**
     * {@inheritDoc}
     *
     * Remove data associated with given storage ID. We do not attempt to remove
     * the data from disk, simply remove the reference to it.
     */
    @Override
    synchronized public void removeDataPoints(@Nonnull Object ID) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        dataPointsOffsets.remove(ID);
        dataPointsLengths.remove(ID);
    }

    /** {@inheritDoc} */
    @Override
    synchronized public void dispose() {

        // Discard the hash maps and byte buffer
        dataPointsOffsets.clear();
        dataPointsLengths.clear();
        byteBuffer = null;

        // Remove the temporary file
        if (tmpDataFileName.exists()) {
            logger.debug("Removing tmp-file " + tmpDataFileName);

            try {
                tmpDataFile.close();
                tmpDataFileName.delete();
            } catch (IOException e) {
                logger.warn("Could not close and remove temporary file "
                        + tmpDataFileName + ": " + e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * When this object is garbage collected, remove the associated temporary
     * data file from disk.
     */
    @Override
    protected void finalize() {
        dispose();
    }
}
