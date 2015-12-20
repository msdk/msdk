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
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
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
class TmpFileDataStore implements DataPointStore {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final File tmpDataFileName;
    private final RandomAccessFile tmpDataFile;

    // Start with a ~20 KB byte buffer, that will be expanded based on needs
    private ByteBuffer byteBuffer = ByteBuffer.allocate(20000);

    private final HashMap<Integer, Long> dataPointsOffsets = new HashMap<>();
    private final HashMap<Integer, Integer> dataPointsLengths = new HashMap<>();

    private int lastStorageId = 0;

    TmpFileDataStore() {

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
            throw new MSDKRuntimeException(e);
        }

    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    synchronized public Object storeData(@Nonnull Object array,
            @Nonnull Integer size) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        try {
            final long currentOffset = tmpDataFile.length();

            int objectSize;
            if (array.getClass().getComponentType().equals(Double.TYPE))
                objectSize = Double.SIZE / 8;
            else if (array.getClass().getComponentType().equals(Float.TYPE))
                objectSize = Float.SIZE / 8;
            else if (ChromatographyInfo.class
                    .isAssignableFrom(array.getClass().getComponentType()))
                objectSize = (Float.SIZE / 8) * 3;
            else
                throw new IllegalArgumentException("Unsupported array type");

            // Calculate minimum necessary size of the byte buffer
            final int numOfBytes = size * objectSize;

            // Make sure we have enough space in the byte buffer
            if (byteBuffer.capacity() < numOfBytes) {
                byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
            } else {
                byteBuffer.clear();
            }

            if (array.getClass().getComponentType().equals(Double.TYPE))
                convertArrayToByteBuffer((double[]) array, size);
            else if (array.getClass().getComponentType().equals(Float.TYPE))
                convertArrayToByteBuffer((float[]) array, size);
            else if (ChromatographyInfo.class
                    .isAssignableFrom(array.getClass().getComponentType()))
                convertArrayToByteBuffer((ChromatographyInfo[]) array, size);

            tmpDataFile.write(byteBuffer.array(), 0, numOfBytes);

            // Increase the storage ID
            lastStorageId++;

            // Save the reference to the new items
            dataPointsOffsets.put(lastStorageId, currentOffset);
            dataPointsLengths.put(lastStorageId, size);

        } catch (IOException e) {
            throw new MSDKRuntimeException(e);
        }

        return lastStorageId;
    }

    /** {@inheritDoc} */
    @Override
    synchronized public void loadData(@Nonnull Object id, @Nonnull Object array) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        if (!dataPointsLengths.containsKey(id))
            throw new IllegalArgumentException("ID " + id
                    + " not found in storage file " + tmpDataFileName);

        // Get file offset and number of data points
        final long offset = dataPointsOffsets.get(id);
        final int numOfDataPoints = dataPointsLengths.get(id);

        if (!array.getClass().isArray())
            throw new IllegalArgumentException(
                    "The provided argument is not an array");

        if (Array.getLength(array) < numOfDataPoints)
            throw new IllegalArgumentException(
                    "The provided array does not fit all loaded objects");

        int objectSize;
        if (array.getClass().getComponentType().equals(Double.TYPE))
            objectSize = Double.SIZE / 8;
        else if (array.getClass().getComponentType().equals(Float.TYPE))
            objectSize = Float.SIZE / 8;
        else if (ChromatographyInfo.class
                .isAssignableFrom(array.getClass().getComponentType()))
            objectSize = (Float.SIZE / 8) * 3;
        else
            throw new IllegalArgumentException("Unsupported array type");

        // Calculate minimum necessary size of the byte buffer
        final int numOfBytes = numOfDataPoints * objectSize;

        // Make sure we have enough space in the byte buffer
        if (byteBuffer.capacity() < numOfBytes) {
            byteBuffer = ByteBuffer.allocate(numOfBytes * 2);
        } else {
            byteBuffer.clear();
        }

        try {

            // Read values
            tmpDataFile.seek(offset);
            tmpDataFile.read(byteBuffer.array(), 0, numOfBytes);

            if (array.getClass().getComponentType().equals(Double.TYPE))
                convertByteBufferToArray((double[]) array, numOfDataPoints);
            else if (array.getClass().getComponentType().equals(Float.TYPE))
                convertByteBufferToArray((float[]) array, numOfDataPoints);
            else if (ChromatographyInfo.class
                    .isAssignableFrom(array.getClass().getComponentType()))
                convertByteBufferToArray((ChromatographyInfo[]) array,
                        numOfDataPoints);

        } catch (IOException e) {
            throw new MSDKRuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    synchronized public void removeData(@Nonnull Object id) {

        if (byteBuffer == null)
            throw new IllegalStateException("This object has been disposed");

        dataPointsOffsets.remove(id);
        dataPointsLengths.remove(id);

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

    private void convertArrayToByteBuffer(float[] data, int size) {
        FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
        for (int i = 0; i < size; i++)
            fltBuffer.put(data[i]);
    }

    private void convertArrayToByteBuffer(double[] data, int size) {
        DoubleBuffer dblBuffer = byteBuffer.asDoubleBuffer();
        for (int i = 0; i < size; i++)
            dblBuffer.put(data[i]);
    }

    private void convertArrayToByteBuffer(ChromatographyInfo[] data, int size) {

        FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
        Float f;
        for (int i = 0; i < size; i++) {
            f = data[i].getRetentionTime();
            fltBuffer.put(f);
            f = data[i].getSecondaryRetentionTime();
            if (f == null)
                f = Float.NaN;
            fltBuffer.put(f);
            f = data[i].getIonDriftTime();
            if (f == null)
                f = Float.NaN;
            fltBuffer.put(f);
        }
    }

    private void convertByteBufferToArray(float[] array, Integer size) {
        FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();
        for (int i = 0; i < size; i++) {
            array[i] = fltBuffer.get();
        }
    }

    private void convertByteBufferToArray(double[] array, Integer size) {
        DoubleBuffer dblBuffer = byteBuffer.asDoubleBuffer();
        for (int i = 0; i < size; i++) {
            array[i] = dblBuffer.get();
        }
    }

    private void convertByteBufferToArray(ChromatographyInfo[] array,
            Integer size) {
        FloatBuffer fltBuffer = byteBuffer.asFloatBuffer();

        for (int i = 0; i < size; i++) {
            Float rt = fltBuffer.get();
            if (rt == Float.NaN)
                rt = null;
            Float srt = fltBuffer.get();
            if (srt == Float.NaN)
                srt = null;
            Float idt = fltBuffer.get();
            if (idt == Float.NaN)
                idt = null;
            array[i] = MSDKObjectBuilder
                    .getChromatographyInfo2D(SeparationType.UNKNOWN, rt, srt);
        }
    }
}
