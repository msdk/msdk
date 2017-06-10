/* 
 * Copyright 2016 Dmitry Avtonomov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.msdk.io.mzml2.util;

import io.github.msdk.MSDKException;

/**
* @author Dmitry Avtonomov
*/
public class Base64Context {

    /**
     * Place holder for the bytes we're dealing with for our based logic.
     * Bitwise operations store and extract the encoding or decoding from this variable.
     */
    int ibitWorkArea;

    /**
     * Place holder for the bytes we're dealing with for our based logic.
     * Bitwise operations store and extract the encoding or decoding from this variable.
     */
    long lbitWorkArea;

    /**
     * Buffer for streaming.
     */
    ByteArrayHolder bytesHolder;

    /**
     * Position where next character should be written in the buffer.
     */
    int pos;

    /**
     * Position where next character should be read from the buffer.
     */
    int readPos;

    /**
     * Boolean flag to indicate the EOF has been reached. Once EOF has been reached, this object becomes useless,
     * and must be thrown away.
     */
    boolean eof;

    /**
     * Variable tracks how many characters have been written to the current line. Only used when encoding. We use
     * it to make sure each encoded line never goes beyond lineLength (if lineLength > 0).
     */
    int currentLinePos;

    /**
     * Writes to the buffer only occur after every 3/5 reads when encoding, and every 4/8 reads when decoding. This
     * variable helps track that.
     */
    int modulus;

    public Base64Context() {
    }

    /**
     * Returns a String useful for debugging (especially within a debugger.)
     *
     * @return a String useful for debugging.
     */
    @SuppressWarnings("boxing") // OK to ignore boxing here
    @Override
    public String toString() {
        return String.format("%s[currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, " +
                        "modulus=%s, pos=%s, readPos=%s]", this.getClass().getSimpleName(),
                currentLinePos, eof, ibitWorkArea, lbitWorkArea, modulus, pos, readPos);
    }

    /**
     * Ensure, that the output data buffer has enough space left for 
     * {@code size} mode bytes.
     * @param size
     * @return the output buffer
     * @throws MSDKException 
     */
    public byte[] ensureBufferHasCapacityLeft(int size) throws MSDKException {
        if (bytesHolder == null) {
            bytesHolder = new ByteArrayHolder(size);
        } else {
            bytesHolder.ensureHasSpace(size);
        }
        return bytesHolder.getUnderlyingBytes();
    }

    /**
     * If your underlying buffer has navigation capabilities, you can use this method
     * to sync positions of the reader with your structure.
     */
    public void syncBufferPos() {
        if (bytesHolder != null)
            bytesHolder.setPosition(pos);
    }


    /**
     * Returns the amount of buffered data available for reading.
     * @return The amount of buffered data available for reading.
     */
    protected int available() {  // package protected for access from I/O streams
        return bytesHolder != null ? pos - readPos : 0;
    }

    public ByteArrayHolder readResults() {
        if (bytesHolder != null) {
            syncBufferPos();
            return bytesHolder;
        }
        return null;
    }

    /** should be called when context becomes useless */
    public void close() throws MSDKException {

    }
}
