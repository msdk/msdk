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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Auto-growing array of bytes, with access to the underlying data.
 * Almost exactly follows Java's ByteArrayOutputStream, but gives access
 * to underlying data and allows basic navigation.
 * @author Dmitry Avtonomov
 */
public class ByteArrayHolder extends OutputStream {

    /**
     * The buffer where data is stored.
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count;

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param   size   the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
    public ByteArrayHolder(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }

        buf = new byte[size];
        count = 0;
    }

    /**
     * Creates a new byte array output stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public ByteArrayHolder() {
        this(32);
    }

    public ByteArrayHolder(byte[] underlyingBuf) {
        this.buf = underlyingBuf;
        this.count = underlyingBuf.length - 1;
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity < 0) {
            if (minCapacity < 0) // overflow
                throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        //System.out.printf("Growing BAH-ol-method[%s] from %d to %d, new cap is: %d (%.2fkb)\n",
        //        System.identityHashCode(this), getCapacity(), minCapacity, newCapacity, newCapacity/1000d);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    private void grow(int minCapacity, boolean keepData) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity < 0) {
            if (minCapacity < 0) // overflow
                throw new OutOfMemoryError("Not enought memory to extend a ByteArrayHolder");
            newCapacity = Integer.MAX_VALUE;
        }
        //System.out.printf("Growing BAH-my-method[%s] from %d to %d, new cap is: %d (%.2fkb)\n",
        //        System.identityHashCode(this), getCapacity(), minCapacity, newCapacity, newCapacity/1000d);
        if (keepData) {
            buf = Arrays.copyOf(buf, newCapacity);
        } else {
            buf = new byte[newCapacity];
        }
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    @Override
    public synchronized void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    @Override
    public synchronized void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    /**
     * Writes the complete contents of this byte array output stream to
     * the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param      out   the output stream to which to write the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    /**
     * Resets the <code>count</code> field of this byte array output
     * stream to zero, so that all currently accumulated output in the
     * output stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space.
     *
     * @see     java.io.ByteArrayInputStream#count
     */
    public synchronized void reset() {
        count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.
     *
     * @return  the current contents of this output stream, as a byte array.
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public synchronized byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return  the value of the <code>count</code> field, which is the number
     *          of valid bytes in this output stream.
     * @see     java.io.ByteArrayOutputStream#count
     */
    public synchronized int size() {
        return count;
    }

    /**
     * Converts the buffer's contents into a string decoding bytes using the
     * platform's default character set. The length of the new <tt>String</tt>
     * is a function of the character set, and hence may not be equal to the
     * size of the buffer.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with the default replacement string for the platform's
     * default character set. The {@linkplain java.nio.charset.CharsetDecoder}
     * class should be used when more control over the decoding process is
     * required.
     *
     * @return String decoded from the buffer's contents.
     * @since  JDK1.1
     */
    @Override
    public synchronized String toString() {
        return new String(buf, 0, count);
    }

    /**
     * Converts the buffer's contents into a string by decoding the bytes using
     * the specified {@link java.nio.charset.Charset charsetName}. The length of
     * the new <tt>String</tt> is a function of the charset, and hence may not be
     * equal to the length of the byte array.
     *
     * <p> This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement string. The {@link
     * java.nio.charset.CharsetDecoder} class should be used when more control
     * over the decoding process is required.
     *
     * @param  charsetName  the name of a supported
     *              {@linkplain java.nio.charset.Charset </code>charset<code>}
     * @return String decoded from the buffer's contents.
     * @exception  UnsupportedEncodingException
     *             If the named charset is not supported
     * @since   JDK1.1
     */
    public synchronized String toString(String charsetName)
        throws UnsupportedEncodingException
    {
        return new String(buf, 0, count, charsetName);
    }

    /**
     * Creates a newly allocated string. Its size is the current size of
     * the output stream and the valid contents of the buffer have been
     * copied into it. Each character <i>c</i> in the resulting string is
     * constructed from the corresponding element <i>b</i> in the byte
     * array such that:
     * <blockquote><pre>
     *     c == (char)(((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </pre></blockquote>
     *
     * @deprecated This method does not properly convert bytes into characters.
     * As of JDK&nbsp;1.1, the preferred way to do this is via the
     * <code>toString(String enc)</code> method, which takes an encoding-name
     * argument, or the <code>toString()</code> method, which uses the
     * platform's default character encoding.
     *
     * @param      hibyte    the high byte of each resulting Unicode character.
     * @return     the current contents of the output stream, as a string.
     * @see        java.io.ByteArrayOutputStream#size()
     * @see        java.io.ByteArrayOutputStream#toString(String)
     * @see        java.io.ByteArrayOutputStream#toString()
     */
    @Deprecated
    public synchronized String toString(int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    /**
     * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * <p>
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(this.buf, 0, size());
    }

    public byte[] getUnderlyingBytes() {
        return buf;
    }

    public final int getCapacity() {
        return buf.length;
    }

    public final int getCapacityLeft() {
        return buf.length - count;
    }

    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is
     * interpreted as a request for the unsatisfiably large capacity
     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    public void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    /**
     *
     * @param minCapacity the min capacity to grow the underlying buffer to. The actual
     * increased capacity might be more than that.
     * @param keepData if true, will copy the previous contents of the buffer to the new one.
     * If you are just reusing an old buffer for new data, you might want to set this to false, then
     * the buffer will be reset.
     */
    public void ensureCapacity(int minCapacity, boolean keepData) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0) {
            grow(minCapacity, keepData);
        }
        if (!keepData)
            this.reset();
    }

    /**
     * Checks if there is enough space in the buffer to write N additional bytes.
     * Will grow the buffer if necessary.<br/>
     * It takes current position in the buffer into account.
     * @param numBytesToAdd the number of bytes you want to add to the buffer
     */
    public void ensureHasSpace(int numBytesToAdd) {
        if (numBytesToAdd < 0) {
            throw new IllegalArgumentException("Number of bytes can't be negative");
        }
        int capacityLeft = getCapacityLeft();
        if (capacityLeft < numBytesToAdd) {
            grow(numBytesToAdd - capacityLeft, true);
        }
    }

    /**
     * This is the position of the next write to the underlying buffer.
     * @return the number of valid bytes in the underlying buffer
     */
    public final int getPosition() {
        return this.size();
    }

    /**
     * This is a dangerous method, be sure you know what you're doing.
     * @param newPos
     */
    public final void setPosition(int newPos) {
        if (newPos > getCapacity()) {
            throw new IndexOutOfBoundsException("New position can't be greater than the capacity");
        }
        if (newPos < 0) {
            throw new IndexOutOfBoundsException("New position can't be less than zero");
        }
        count = newPos;
    }

    public final void clear() {
        Arrays.fill(buf, (byte)0);
        setPosition(0);
    }

}
