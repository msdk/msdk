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

package io.github.msdk.io.mzml2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import javax.annotation.Nonnull;

public class MzMLFileMemoryMapper {
	private final @Nonnull File mzMLFile;

	public MzMLFileMemoryMapper(String mzMLFilePath) {
		this(new File(mzMLFilePath));
	}

	public MzMLFileMemoryMapper(Path mzMLFilePath) {
		this(mzMLFilePath.toFile());
	}

	public MzMLFileMemoryMapper(File mzMLFile) {
		this.mzMLFile = mzMLFile;
	}

	public InputStream execute() throws IOException {

		RandomAccessFile aFile = new RandomAccessFile(mzMLFile, "r");
		FileChannel inChannel = aFile.getChannel();
		MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
		MappedByteBufferInputStream is = new MappedByteBufferInputStream(buffer);
		aFile.close();

		return is;
	}
}
