/* 
 * (C) Copyright 2015-2017 by MSDK Development Team
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

package io.github.msdk.io.mzml2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;

class MappedByteBufferInputStream extends InputStream {
	private MappedByteBuffer buf;
	private long pos = 0;
	private long mark = 0;

	MappedByteBufferInputStream(MappedByteBuffer buf) {
		this.buf = buf;
	}

	public synchronized int read() throws IOException {
		if (!buf.hasRemaining()) {
			return -1;
		}
		int readBuffer = buf.get();
		if (readBuffer > 0)
			pos += 1;
		return readBuffer;
	}

	public synchronized int read(byte[] bytes, int off, int len) throws IOException {
		if (!buf.hasRemaining()) {
			return -1;
		}

		len = Math.min(len, buf.remaining());
		buf.get(bytes, off, len);
		pos += len;
		return len;
	}

	public synchronized long getCurrentPosition() {
		return pos;
	}

	public synchronized void setMark() {
		mark = pos;
	}
}
