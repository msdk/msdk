package io.github.msdk.io.mzxml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;

/**
 * <p>MzXMLRawDataFile class.</p>
 *
 */
public class MzXMLRawDataFile implements RawDataFile {

  private static final @Nonnull FileType fileType = FileType.MZXML;

  private final @Nonnull File sourceFile;

  private final @Nonnull List<MsScan> msScans;
  private final @Nonnull List<Chromatogram> chromatograms;

  private @Nonnull String name;

  /**
   * <p>
   * Constructor for MzXMLRawDataFile.
   * </p>
   *
   * @param sourceFile a {@link java.io.File} object.
   */
  public MzXMLRawDataFile(@Nonnull File sourceFile) {
    this.sourceFile = sourceFile;
    this.name = sourceFile.getName();
    this.msScans = new ArrayList<>();
    this.chromatograms = new ArrayList<>();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Optional<File> getOriginalFile() {
    return Optional.ofNullable(sourceFile);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public FileType getRawDataFileType() {
    return fileType;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull List<MsScan> getScans() {
    return ImmutableList.copyOf(msScans);
  }

  /**
   * {@inheritDoc}
   *
   * @param scan a {@link io.github.msdk.datamodel.MsScan} object.
   */
  public void addScan(@Nonnull MsScan scan) {
    Preconditions.checkNotNull(scan);
    synchronized (msScans) {
      msScans.add(scan);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param scan a {@link io.github.msdk.datamodel.MsScan} object.
   */
  public void removeScan(@Nonnull MsScan scan) {
    Preconditions.checkNotNull(scan);
    synchronized (msScans) {
      msScans.remove(scan);
    }
  }

  /** {@inheritDoc} */
  @SuppressWarnings("null")
  @Override
  @Nonnull
  public List<Chromatogram> getChromatograms() {
    return ImmutableList.copyOf(chromatograms);
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {}

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public List<String> getMsFunctions() {
    ArrayList<String> msFunctionList = new ArrayList<>();
    synchronized (msScans) {
      for (MsScan scan : msScans) {
        String f = scan.getMsFunction();
        if ((f != null) && (!msFunctionList.contains(f)))
          msFunctionList.add(f);
      }
    }
    return msFunctionList;
  }

}
