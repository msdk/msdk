/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.mzdb;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;

public class MzDBRawDataFile implements RawDataFile {
  private static final @Nonnull FileType fileType = FileType.MZDB;
  private final File sourceFile;
  private final @Nonnull List<String> msFunctions;
  private final @Nonnull List<MsScan> msScans;
  private final @Nonnull List<Chromatogram> chromatograms;
  private @Nonnull String name;

  @SuppressWarnings("null")
  public MzDBRawDataFile(File sourceFile, List<String> msFunctions, List<MsScan> msScans,
      List<Chromatogram> chromatograms) {
    this.sourceFile = sourceFile;
    this.name = sourceFile != null ? sourceFile.getName() : null;
    this.msFunctions = msFunctions;
    this.msScans = msScans;
    this.chromatograms = chromatograms;
  }

  public String getName() {
    return name;
  }

  public Optional<File> getOriginalFile() {
    return Optional.ofNullable(sourceFile);
  }

  public FileType getRawDataFileType() {
    return fileType;
  }

  public List<String> getMsFunctions() {
    return ImmutableList.copyOf(msFunctions);
  }

  /** {@inheritDoc} */
  @SuppressWarnings("null")
  @Nonnull
  public List<Chromatogram> getChromatograms() {
    return ImmutableList.copyOf(chromatograms);
  }


  public List<MsScan> getScans() {
    return ImmutableList.copyOf(msScans);
  }

  public void dispose() {
    // TODO Auto-generated method stub

  }

}
