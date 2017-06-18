/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */

package io.github.msdk.datamodel.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

/**
 * Implementation of the RawDataFile interface.
 *
 * @author plusik
 * @version $Id: $Id
 */
public class SimpleRawDataFile implements RawDataFile {

  private @Nonnull String rawDataFileName;
  private @Nonnull Optional<File> originalRawDataFile;
  private @Nonnull FileType rawDataFileType;
  private final @Nonnull ArrayList<MsScan> scans;
  private final @Nonnull ArrayList<Chromatogram> chromatograms;
  private final @Nonnull DataPointStore dataPointStore;

  /**
   * <p>Constructor for SimpleRawDataFile.</p>
   *
   * @param rawDataFileName a {@link java.lang.String} object.
   * @param originalRawDataFile a {@link java.util.Optional} object.
   * @param rawDataFileType a {@link io.github.msdk.datamodel.files.FileType} object.
   * @param dataPointStore a {@link io.github.msdk.datamodel.datastore.DataPointStore} object.
   */
  public SimpleRawDataFile(@Nonnull String rawDataFileName, @Nonnull Optional<File> originalRawDataFile,
      @Nonnull FileType rawDataFileType, @Nonnull DataPointStore dataPointStore) {
    Preconditions.checkNotNull(rawDataFileType);
    Preconditions.checkNotNull(dataPointStore);
    this.rawDataFileName = rawDataFileName;
    this.originalRawDataFile = originalRawDataFile;
    this.rawDataFileType = rawDataFileType;
    this.dataPointStore = dataPointStore;
    this.scans = new ArrayList<>();
    this.chromatograms = new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   *
   * @return a {@link java.lang.String} object.
   */
  public @Nonnull String getName() {
    return rawDataFileName;
  }

  /**
   * {@inheritDoc}
   *
   * @param name a {@link java.lang.String} object.
   */
  public void setName(@Nonnull String name) {
    Preconditions.checkNotNull(name);
    this.rawDataFileName = name;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Optional<File> getOriginalFile() {
    return originalRawDataFile;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String getOriginalFilename() {
	  if (originalRawDataFile.isPresent()) {
		  return originalRawDataFile.get().getName();
	  }

	  return "Unknown";
  }

  /**
   * {@inheritDoc}
   *
   * @param newOriginalFile a {@link java.io.File} object.
   */
  public void setOriginalFile(@Nullable File newOriginalFile) {
    this.originalRawDataFile = Optional.ofNullable(newOriginalFile);
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull FileType getRawDataFileType() {
    return rawDataFileType;
  }

  /**
   * {@inheritDoc}
   *
   * @param rawDataFileType a {@link io.github.msdk.datamodel.files.FileType} object.
   */
  public void setRawDataFileType(@Nonnull FileType rawDataFileType) {
    Preconditions.checkNotNull(rawDataFileType);
    this.rawDataFileType = rawDataFileType;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public List<MsFunction> getMsFunctions() {
    ArrayList<MsFunction> msFunctionList = new ArrayList<>();
    synchronized (scans) {
      for (MsScan scan : scans) {
        MsFunction f = scan.getMsFunction();
        if (f != null)
          msFunctionList.add(f);
      }
    }
    return msFunctionList;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull List<MsScan> getScans() {
    return ImmutableList.copyOf(scans);
  }

  /**
   * {@inheritDoc}
   *
   * @param scan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
   */
  public void addScan(@Nonnull MsScan scan) {
    Preconditions.checkNotNull(scan);
    synchronized (scans) {
      scans.add(scan);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param scan a {@link io.github.msdk.datamodel.rawdata.MsScan} object.
   */
  public void removeScan(@Nonnull MsScan scan) {
    Preconditions.checkNotNull(scan);
    synchronized (scans) {
      scans.remove(scan);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public List<Chromatogram> getChromatograms() {
    return ImmutableList.copyOf(chromatograms);
  }

  /**
   * {@inheritDoc}
   *
   * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
   */
  public void addChromatogram(@Nonnull Chromatogram chromatogram) {
    Preconditions.checkNotNull(chromatogram);
    synchronized (chromatograms) {
      chromatograms.add(chromatogram);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param chromatogram a {@link io.github.msdk.datamodel.chromatograms.Chromatogram} object.
   */
  public void removeChromatogram(@Nonnull Chromatogram chromatogram) {
    Preconditions.checkNotNull(chromatogram);
    synchronized (chromatograms) {
      chromatograms.remove(chromatogram);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    dataPointStore.dispose();
  }

}
