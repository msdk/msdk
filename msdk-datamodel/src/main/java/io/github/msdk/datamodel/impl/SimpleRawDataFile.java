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
 */
public class SimpleRawDataFile implements RawDataFile {

  private @Nonnull String rawDataFileName;
  private @Nullable File originalRawDataFile;
  private @Nonnull FileType rawDataFileType;
  private final @Nonnull ArrayList<MsScan> scans;
  private final @Nonnull ArrayList<Chromatogram> chromatograms;
  private final @Nonnull DataPointStore dataPointStore;

  public SimpleRawDataFile(@Nonnull String rawDataFileName, @Nullable File originalRawDataFile,
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

  /** {@inheritDoc} */
  public @Nonnull String getName() {
    return rawDataFileName;
  }

  /** {@inheritDoc} */
  public void setName(@Nonnull String name) {
    Preconditions.checkNotNull(name);
    this.rawDataFileName = name;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public File getOriginalFile() {
    return originalRawDataFile;
  }

  /** {@inheritDoc} */
  public void setOriginalFile(@Nullable File newOriginalFile) {
    this.originalRawDataFile = newOriginalFile;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull FileType getRawDataFileType() {
    return rawDataFileType;
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  public void addScan(@Nonnull MsScan scan) {
    Preconditions.checkNotNull(scan);
    synchronized (scans) {
      scans.add(scan);
    }
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  public void addChromatogram(@Nonnull Chromatogram chromatogram) {
    Preconditions.checkNotNull(chromatogram);
    synchronized (chromatograms) {
      chromatograms.add(chromatogram);
    }
  }

  /** {@inheritDoc} */
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
