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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.SeparationType;

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
	private @Nonnull String separator;

	// Columns maps to keep track of column names and sample names
	private Map<Integer, String> columnNames = new HashMap<Integer, String>();
	private Map<Integer, Sample> sampleNames = new HashMap<Integer, Sample>();
	private Map<Integer, FeatureTableColumn<?>> columns = new HashMap<Integer, FeatureTableColumn<?>>();

	private FeatureTable newFeatureTable;
	private final Sample fileSample;
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
		this.fileSample = MSDKObjectBuilder.getSimpleSample(sourceFile.getName());
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

		// Read all lines from the CSV file into an array
		final List<String> lines;
		try {
			lines = Files.asCharSource(sourceFile, Charset.defaultCharset()).readLines();
		} catch (IOException ex) {
			throw new MSDKException(ex);
		}

		// Update total lines
		totalLines = lines.size();

		int rowId = 0;

		boolean firstLine = true;
		for (String line : lines) {

			// If first line the extract column names and sample names
			if (firstLine) {
				// Find the separator used in the header
				separator = findSeparator(line);

				// Extract the column names and sample names from the input
				findNames(line);

				// Add the columns to the feature table
				for (int i = 0; i < columnNames.size(); i++) {
					String columnName = columnNames.get(i);
					Sample sample = sampleNames.get(i);

					// Remove the sample name from the column name
					if (sample != null) {
						columnName = columnName.replace(sample.getName(), "");
						if (columnName.startsWith(" "))
							columnName = columnName.substring(1, columnName.length());
					}

					// Map the column name to the MSDK ColumnName
					FeatureTableColumn<?> column = createNewColumn(columnName, sample);

					// Make sure that there is only on ion annotation column
					FeatureTableColumn<?> ionAnnotationColumn = newFeatureTable.getColumn(ColumnName.IONANNOTATION,
							null);
					if (column.getName().equals(ColumnName.IONANNOTATION.getName())) {
						if (ionAnnotationColumn != null)
							column = ionAnnotationColumn;
						else
							// Add the column to the feature table
							newFeatureTable.addColumn(column);
					} else {
						// Add the column to the feature table
						newFeatureTable.addColumn(column);
					}

					// Add the column to the map
					columns.put(i, column);
				}

				firstLine = false;

			} else {

				// Feature table row
				rowId++;
				FeatureTableRow row = MSDKObjectBuilder.getFeatureTableRow(newFeatureTable, rowId);
				newFeatureTable.addRow(row);

				// Split string based on separator
				String[] data = line.split(separator);

				// Loop through all the data and add it to the row
				for (int i = 0; i < data.length; i++) {
					String stringData = data[i];

					// Ignore null values
					if (stringData.equals("") || stringData.equals("null"))
						continue;

					Object objectData = null;
					FeatureTableColumn currentColumn = columns.get(i);
					Class<?> currentClass = currentColumn.getDataTypeClass();

					// Handle ChromatographyInfo and List (= Ion Annotation)
					// class separately
					if (currentClass.getSimpleName().equals("ChromatographyInfo")) {
						ChromatographyInfo chromatographyInfo = null;

						// Two dimensional data (e.g. 360 , 1.630)
						if (stringData.contains(",")) {
							String[] rtValues = stringData.split(",");
							Float rt1 = Float.parseFloat(rtValues[0].replace(" ", ""));
							Float rt2 = Float.parseFloat(rtValues[1].replace(" ", ""));
							chromatographyInfo = MSDKObjectBuilder.getChromatographyInfo2D(SeparationType.UNKNOWN, rt1,
									rt2);
						} else {
							Float rt = Float.parseFloat(stringData);
							chromatographyInfo = MSDKObjectBuilder.getChromatographyInfo1D(SeparationType.UNKNOWN, rt);
						}

						// Add the data
						row.setData(currentColumn, chromatographyInfo);

					} else if (currentClass.getSimpleName().equals("List")) {
						FeatureTableColumn<List<IonAnnotation>> ionAnnotationColumn = newFeatureTable
								.getColumn(ColumnName.IONANNOTATION, null);
						List<IonAnnotation> ionAnnotations = row.getData(ionAnnotationColumn);
						IonAnnotation ionAnnotation;

						// Get ion annotation or create a new
						if (ionAnnotations != null) {
							ionAnnotation = ionAnnotations.get(0);
						} else {
							ionAnnotation = MSDKObjectBuilder.getSimpleIonAnnotation();
						}

						switch (columnNames.get(i).toLowerCase()) {
						case "name":
							ionAnnotation.setDescription(stringData);
							break;
						case "molecular formula":
							// Create chemical structure
							IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(stringData,
									DefaultChemObjectBuilder.getInstance());
							ionAnnotation.setFormula(formula);
							break;
						}

						// Add the data
						List<IonAnnotation> newIonAnnotations = new ArrayList<IonAnnotation>();
						newIonAnnotations.add(ionAnnotation);
						row.setData(currentColumn, newIonAnnotations);

					} else {
						switch (currentClass.getSimpleName()) {
						case "Integer":
							objectData = Integer.parseInt(stringData);
							break;
						case "Double":
							objectData = Double.parseDouble(stringData);
							break;
						default:
							objectData = stringData;
							break;
						}
						// Add the data
						row.setData(currentColumn, objectData);

					}

				}

			}

			parsedLines++;

			// Check if cancel is requested
			if (canceled)
				return null;

		}

		return newFeatureTable;

	}

	private String findSeparator(String line) {
		// Default is a comma ","
		String result = ",";

		// Tab
		if (line.contains("\t"))
			result = "\t";

		return result;
	}

	private FeatureTableColumn<?> createNewColumn(String columnName, Sample sample) {

		ColumnName newColumnName = null;

		// Check if the column name matches any in MSDK ColumnName
		for (ColumnName defaultName : ColumnName.values()) {
			if (columnName.toUpperCase().contains(defaultName.getName().toUpperCase()))
				newColumnName = defaultName;
		}

		// If no match, then check common matches
		if (newColumnName == null) {
			if (columnName.toLowerCase().contains("retention time"))
				newColumnName = ColumnName.RT;
			if (columnName.toLowerCase().contains("R.T."))
				newColumnName = ColumnName.RT;
			if (columnName.toLowerCase().contains("name"))
				newColumnName = ColumnName.IONANNOTATION;
			if (columnName.toLowerCase().contains("formula"))
				newColumnName = ColumnName.IONANNOTATION;
		}

		// If no samples were found, then assume that all data in the file is
		// from one sample
		if (sampleNames.size() == 0)
			sample = fileSample;

		// If still no match, then create a new column with String.class
		FeatureTableColumn<?> column = null;
		if (newColumnName == null) {
			column = MSDKObjectBuilder.getFeatureTableColumn(columnName, String.class, sample);
		} else {
			column = MSDKObjectBuilder.getFeatureTableColumn(newColumnName, sample);
		}

		return column;
	}

	// Find the longest possible common ending of the column name which is found
	// multiple times in the column header line and use this as the column name.
	// The remaining part of the name will be the sample name.
	private void findNames(String firstLine) {

		// Lists for column names and sample names
		List<String> colums = new ArrayList<String>();
		List<String> samples = new ArrayList<String>();

		// Split string based on separator
		String[] columns = firstLine.split(separator);

		// 1st iteration: Loop through all column names to find sample names.
		// Assumption: Two or more data columns are present for each sample.
		for (int firstIndex = columns.length - 1; firstIndex >= 0; firstIndex--) {
			String firstColumn = columns[firstIndex];
			int firstColumnLength = firstColumn.length();

			// Loop through all possible length of the column name
			int maxCharacters = 0;
			for (int characters = 0; characters <= firstColumnLength; characters++) {
				int currentCharacterLength = firstColumnLength - characters;

				// Loop through all column names before the current
				for (int secondIndex = firstIndex - 1; secondIndex >= 0; secondIndex--) {
					String column = columns[secondIndex];
					int columnLength = column.length();

					if (columnLength >= currentCharacterLength) {
						String firstName = firstColumn.substring(characters, firstColumnLength);
						String secondName = column.substring(columnLength - currentCharacterLength, columnLength);

						// Check that there are no integers in the column name
						// since these are typically part of the sample name
						Boolean containsInts = containsIntegers(firstName);

						if (firstName.equals(secondName) & !containsInts) {
							maxCharacters = currentCharacterLength;
							break;
						}
					}
				}

				// Break if a max character has been set
				if (maxCharacters > 0)
					break;
			}

			String commonName;
			String sampleName;
			int sampleCounter = 0;
			// Require a minimum of 3 characters for sample name
			if (maxCharacters > 2) {
				commonName = firstColumn.substring(firstColumnLength - maxCharacters, firstColumnLength);
				sampleName = firstColumn.substring(0, firstColumnLength - maxCharacters);

				// Replace initial space if present
				if (commonName.startsWith(" "))
					commonName = commonName.substring(1, commonName.length());

				// Count how many columns have the sample in the name
				sampleCounter = 0;
				for (String column : columns) {
					if (column.contains(sampleName))
						sampleCounter++;
				}

				// Only accept sampleNames which are found multiple times
				if (sampleCounter < 2) {
					sampleName = null;
					commonName = firstColumn;
				}

			} else {
				commonName = firstColumn;
				sampleName = null;
			}

			// Add the sample name to the sample list
			if (sampleName != null & !samples.contains(sampleName)) {
				if (sampleName.length() > 0)
					samples.add(sampleName);
			}

		}

		// 2nd iteration: Loop through all column names to find common names.
		// Assumption: Only one data columns is present for each sample.
		if (samples.size() == 0) {

			for (int firstIndex = columns.length - 1; firstIndex >= 0; firstIndex--) {
				String firstColumn = columns[firstIndex];
				int firstColumnLength = firstColumn.length();

				// Loop through all possible length of the column name from
				// right to left
				int maxCharacters = 0;
				for (int characters = firstColumnLength - 1; characters >= 0; characters--) {
					int currentCharacterLength = firstColumnLength - characters;

					// Loop through all column names before the current
					for (int secondIndex = firstIndex - 1; secondIndex >= 0; secondIndex--) {
						String column = columns[secondIndex];
						int columnLength = column.length();

						if (columnLength >= currentCharacterLength) {
							String firstName = firstColumn.substring(characters, firstColumnLength);
							String secondName = column.substring(columnLength - currentCharacterLength, columnLength);

							// Check that there are no integers in the column
							// name since these are typically part of the sample
							// name
							Boolean containsInts = containsIntegers(firstName);
							if (containsInts)
								break;

							if (firstName.equals(secondName)) {
								maxCharacters = currentCharacterLength;
							}
						}
					}
				}

				String commonName;
				int nameCounter = 0;
				// Require a minimum of 3 characters for common name
				if (maxCharacters > 2) {
					commonName = firstColumn.substring(firstColumnLength - maxCharacters, firstColumnLength);

					// Replace initial space if present
					if (commonName.startsWith(" "))
						commonName = commonName.substring(1, commonName.length());

					// Count how many columns have the sample in the name
					nameCounter = 0;
					for (String column : columns) {
						if (column.contains(commonName))
							nameCounter++;
					}

					// Only accept column names which are found multiple times
					if (nameCounter > 2 & !colums.contains(commonName)) {
						colums.add(commonName);

						for (int i = 0; i < columns.length; i++) {
							String columnName = columns[i];
							if (columnName.contains(commonName)) {
								String sampleName = columnName.replace(commonName, "");

								// Replace ending space if present
								if (sampleName.endsWith(" "))
									sampleName = sampleName.substring(0, sampleName.length() - 1);

								// Add the sample name to the sample list
								if (sampleName.length() > 0)
									samples.add(sampleName);
							}
						}

					}
				}
			}

		}

		// 3rd iteration: All column names are unique columns.
		// Assumption: The CSV file corresponds to one sample.
		if (samples.size() == 0) {
			// Add all columns to map
			for (int i = 0; i < columns.length; i++) {
				String columnName = columns[i];
				columnNames.put(i, columnName);
			}
		}

		// Samples names were found - convert them to MSDK samples
		if (samples.size() > 0) {
			for (String sampleName : samples) {
				// Create a new sample
				Sample sample = MSDKObjectBuilder.getSimpleSample(sampleName);

				// Find all columns which have the sample in their name
				for (int i = 0; i < columns.length; i++) {
					String columnHeadder = columns[i];
					if (columnHeadder.contains(sampleName)) {
						// Find new column name
						String columnName = columnHeadder.replace(sampleName, "");

						// Replace initial space if present
						if (columnName.startsWith(" "))
							columnName = columnName.substring(1, columnName.length());

						// Add the index and sample and column name to the map
						sampleNames.put(i, sample);
						columnNames.put(i, columnName);
					} else {
						columnNames.put(i, columnHeadder);
					}
				}
			}
		}

	}

	private Boolean containsIntegers(String firstName) {
		for (int i = 0; i < firstName.length(); i++) {
			if (isInteger(firstName.substring(i, i + 1)))
				return true;
		}
		return false;
	}

	public boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
			return false;
		}
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
		/*
		 * TODO add counter to findNames() since this is taking most of the time
		 */
	}

	/** {@inheritDoc} */
	@Override
	public void cancel() {
		this.canceled = true;
	}

}
