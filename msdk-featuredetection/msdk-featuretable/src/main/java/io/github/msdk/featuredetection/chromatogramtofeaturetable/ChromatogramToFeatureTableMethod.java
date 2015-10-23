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

package io.github.msdk.featuredetection.chromatogramtofeaturetable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.featuretables.ColumnName;
import io.github.msdk.datamodel.featuretables.FeatureTable;
import io.github.msdk.datamodel.featuretables.FeatureTableColumn;
import io.github.msdk.datamodel.featuretables.FeatureTableRow;
import io.github.msdk.datamodel.featuretables.Sample;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.FeatureTableUtil;

/**
 * This class adds a list of chromatograms to a feature table.
 */
public class ChromatogramToFeatureTableMethod implements MSDKMethod<FeatureTable> {

	private @Nonnull List<Chromatogram> chromatograms;
	private @Nonnull FeatureTable featureTable;
	private @Nonnull Sample sample;

	private Map<String, FeatureTableColumn<?>> tableColumns = new HashMap<String, FeatureTableColumn<?>>();

	private boolean canceled = false;
	private int processedChromatograms = 0, totalChromatograms = 0;

	/**
	 * @param chromatograms,
	 *            featureTable
	 */
	public ChromatogramToFeatureTableMethod(@Nonnull List<Chromatogram> chromatograms,
			@Nonnull FeatureTable featureTable, @Nonnull Sample sample) {
		this.chromatograms = chromatograms;
		this.featureTable = featureTable;
		this.sample = sample;
	}

	/**
	 * @throws MSDKException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public FeatureTable execute() throws MSDKException {
		totalChromatograms = chromatograms.size();

		// Add the columns to the table if needed
		addColumns(featureTable);

		// Check if cancel is requested
		if (canceled)
			return null;

		// Id of last row in feature table
		int lastID = 0;
		List<FeatureTableRow> rows = featureTable.getRows();
		if (!rows.isEmpty()) {
			lastID = featureTable.getRows().get(featureTable.getRows().size()).getId();
		}
		FeatureTableColumn column;
		for (Chromatogram chromatogram : chromatograms) {
			lastID++;
			FeatureTableRow newRow = MSDKObjectBuilder.getFeatureTableRow(featureTable, lastID);

			double mz = chromatogram.getMz();
			column = tableColumns.get(ColumnName.MZ.getName());
			newRow.setData(column, mz);

			float rt = ChromatogramUtil.getRt(chromatogram);
			column = tableColumns.get(ColumnName.RT.getName());
			newRow.setData(column, rt);

			float rtStart = ChromatogramUtil.getRtStart(chromatogram);
			column = tableColumns.get(ColumnName.RTSTART.getName());
			newRow.setData(column, rtStart);

			float rtEnd = ChromatogramUtil.getRtEnd(chromatogram);
			column = tableColumns.get(ColumnName.RTEND.getName());
			newRow.setData(column, rtEnd);

			float duration = ChromatogramUtil.getDuration(chromatogram);
			column = tableColumns.get(ColumnName.DURATION.getName());
			newRow.setData(column, duration);

			float area = ChromatogramUtil.getArea(chromatogram);
			column = tableColumns.get(ColumnName.AREA.getName());
			newRow.setData(column, area);

			float height = ChromatogramUtil.getMaxHeight(chromatogram);
			column = tableColumns.get(ColumnName.HEIGHT.getName());
			newRow.setData(column, height);

			int datapoints = ChromatogramUtil.getNumberOfDataPoints(chromatogram);
			column = tableColumns.get(ColumnName.NUMBEROFDATAPOINTS.getName());
			newRow.setData(column, datapoints);

			float fwhm = ChromatogramUtil.getFwhm(chromatogram);
			column = tableColumns.get(ColumnName.FWHM.getName());
			newRow.setData(column, fwhm);

			float tailingFactor = ChromatogramUtil.getTailingFactor(chromatogram);
			column = tableColumns.get(ColumnName.TAILINGFACTOR.getName());
			newRow.setData(column, tailingFactor);

			float asymmetryFactor = ChromatogramUtil.getAsymmetryFactor(chromatogram);
			column = tableColumns.get(ColumnName.ASYMMETRYFACTOR.getName());
			newRow.setData(column, asymmetryFactor);

			// Add row to feature table
			featureTable.addRow(newRow);

			// Check if cancel is requested
			if (canceled)
				return null;
		}

		// Re-calculate average row m/z and RT values
		FeatureTableUtil.recalculateAverages(featureTable);

		return featureTable;
	}

	private void addColumns(@Nonnull FeatureTable featureTable) {

		/*
		 * TODO: Check if columns are already present before adding them!
		 */

		// Common columns
		FeatureTableColumn<Integer> idColumn = MSDKObjectBuilder.getIdFeatureTableColumn();
		FeatureTableColumn<Double> mzColumn = MSDKObjectBuilder.getMzFeatureTableColumn();
		FeatureTableColumn<ChromatographyInfo> chromatographyInfoColumn = MSDKObjectBuilder
				.getChromatographyInfoFeatureTableColumn();
		FeatureTableColumn<IonAnnotation> ionAnnotationColumn = MSDKObjectBuilder.getIonAnnotationFeatureTableColumn();
		featureTable.addColumn(idColumn);
		featureTable.addColumn(mzColumn);
		featureTable.addColumn(chromatographyInfoColumn);
		featureTable.addColumn(ionAnnotationColumn);

		// Sample columns
		FeatureTableColumn<Double> sampleMzColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.MZ, sample);
		featureTable.addColumn(sampleMzColumn);
		tableColumns.put(ColumnName.MZ.getName(), sampleMzColumn);

		FeatureTableColumn<ChromatographyInfo> sampleChromatographyInfoColumn = MSDKObjectBuilder
				.getFeatureTableColumn(ColumnName.RT, sample);
		featureTable.addColumn(sampleChromatographyInfoColumn);
		tableColumns.put(ColumnName.RT.getName(), sampleChromatographyInfoColumn);

		FeatureTableColumn<Double> sampleRtStartColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.RTSTART,
				sample);
		featureTable.addColumn(sampleRtStartColumn);
		tableColumns.put(ColumnName.RTSTART.getName(), sampleRtStartColumn);

		FeatureTableColumn<Double> sampleRtEndColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.RTEND,
				sample);
		featureTable.addColumn(sampleRtEndColumn);
		tableColumns.put(ColumnName.RTEND.getName(), sampleRtEndColumn);

		FeatureTableColumn<Double> sampleDurationColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.DURATION,
				sample);
		featureTable.addColumn(sampleDurationColumn);
		tableColumns.put(ColumnName.DURATION.getName(), sampleDurationColumn);

		FeatureTableColumn<Double> sampleAreaColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.AREA, sample);
		featureTable.addColumn(sampleAreaColumn);
		tableColumns.put(ColumnName.AREA.getName(), sampleAreaColumn);

		FeatureTableColumn<Double> sampleHeightColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.HEIGHT,
				sample);
		featureTable.addColumn(sampleHeightColumn);
		tableColumns.put(ColumnName.HEIGHT.getName(), sampleHeightColumn);

		FeatureTableColumn<Integer> sampleDatapointsColumn = MSDKObjectBuilder
				.getFeatureTableColumn(ColumnName.NUMBEROFDATAPOINTS, sample);
		featureTable.addColumn(sampleDatapointsColumn);
		tableColumns.put(ColumnName.NUMBEROFDATAPOINTS.getName(), sampleDatapointsColumn);

		FeatureTableColumn<Double> sampleFWHMColumn = MSDKObjectBuilder.getFeatureTableColumn(ColumnName.FWHM, sample);
		featureTable.addColumn(sampleFWHMColumn);
		tableColumns.put(ColumnName.FWHM.getName(), sampleFWHMColumn);

		FeatureTableColumn<Double> sampleTailingColumn = MSDKObjectBuilder
				.getFeatureTableColumn(ColumnName.TAILINGFACTOR, sample);
		featureTable.addColumn(sampleTailingColumn);
		tableColumns.put(ColumnName.TAILINGFACTOR.getName(), sampleTailingColumn);

		FeatureTableColumn<Double> sampleAsymmetryColumn = MSDKObjectBuilder
				.getFeatureTableColumn(ColumnName.ASYMMETRYFACTOR, sample);
		featureTable.addColumn(sampleAsymmetryColumn);
		tableColumns.put(ColumnName.ASYMMETRYFACTOR.getName(), sampleAsymmetryColumn);

	}

	@Override
	@Nullable
	public Float getFinishedPercentage() {
		return totalChromatograms == 0 ? null : (float) processedChromatograms / totalChromatograms;
	}

	@Override
	@Nullable
	public FeatureTable getResult() {
		return featureTable;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

}
