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

package io.github.msdk.io.csv;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;

/**
 * <p>
 * CsvFileImportMethod class.
 * </p>
 *
 */
public class CsvFileImportMethod implements MSDKMethod<FeatureTable> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private int parsedLines, totalLines = 0;

	private final @Nonnull File sourceFile;
	private final @Nonnull DataPointStore dataStore;

	private FeatureTable newFeatureTable;
	private boolean canceled = false;

	/**
	 * <p>
	 * Constructor for CsvFileImportMethod.
	 * </p>
	 *
	 * @param sourceFile
	 *            a {@link java.io.File} object.
	 * @param dataStore
	 *            a
	 *            {@link io.github.msdk.datamodel.datapointstore.DataPointStore}
	 *            object.
	 */
	public CsvFileImportMethod(@Nonnull File sourceFile, @Nonnull DataPointStore dataStore) {
		this.sourceFile = sourceFile;
		this.dataStore = dataStore;
	}

	/** {@inheritDoc} */
	@Override
	public FeatureTable execute() throws MSDKException {

		logger.info("Started parsing file " + sourceFile);

		// Check if the file is readable
		if (!sourceFile.canRead()) {
			throw new MSDKException("Cannot read file " + sourceFile);
		}

		String fileName = sourceFile.getName();
		newFeatureTable = MSDKObjectBuilder.getFeatureTable(fileName, dataStore);

		// Read all lines from the csv file into an array
		final List<String> lines;
		try {
			lines = Files.asCharSource(sourceFile, Charset.defaultCharset()).readLines();
		} catch (IOException ex) {
			throw new MSDKException(ex);
		}

		// Update total lines
		totalLines = lines.size();

		for (String line : lines) {

			/*
			 * TODO: Write method
			 */

			parsedLines++;

			// Check if cancel is requested
			if (canceled)
				return null;

		}

		return newFeatureTable;

	}

	/** {@inheritDoc} */
	@Override
	@Nullable
	public FeatureTable getResult() {
		return newFeatureTable;
	}

	/** {@inheritDoc} */
	@Override
	public Float getFinishedPercentage() {
		return totalLines == 0 ? null : (float) parsedLines / totalLines;
	}

	/** {@inheritDoc} */
	@Override
	public void cancel() {
		this.canceled = true;
	}

}
