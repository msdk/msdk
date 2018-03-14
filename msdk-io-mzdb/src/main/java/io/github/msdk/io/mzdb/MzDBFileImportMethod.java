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
import java.io.FileNotFoundException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.almworks.sqlite4java.SQLiteException;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.Chromatogram;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleChromatogram;
import io.github.msdk.datamodel.SimpleMsScan;
import io.github.msdk.io.mzdb.MzDBRawDataFile;
import fr.profi.mzdb.*;
import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;

/**
 * <p>
 * This class contains methods which parse data in MzDB format from {@link java.io.File File}.
 * </p>
 */
public class MzDBFileImportMethod implements MSDKMethod<RawDataFile> {
  private final File mzDBFile;
  // final InputStream inputStream;
  private MzDBRawDataFile newRawFile;
  private volatile boolean canceled;
  private boolean cacheEntities;
  private Float progress;
  // private int lastLoggedProgress;
  // private Logger logger;
  // private Predicate<MsScan> msScanPredicate = s -> true;
  // private Predicate<Chromatogram> chromatogramPredicate = c -> true;

  /**
   * <p>
   * Constructor for MzDBFileImportMethod.
   * </p>
   *
   * @param mzDBFilePath a {@link java.lang.String String} which contains the absolute path to the
   *        MzDB File.
   * @param cacheEntities
   */
  public MzDBFileImportMethod(String mzDBFilePath, boolean cacheEntities) {
    this.mzDBFile = new File(mzDBFilePath);
    this.cacheEntities = cacheEntities;
  }

  /**
   * {@inheritDoc}
   *
   * <p>
   * Parse the MzDB data and return the parsed data
   * </p>
   *
   * @return a {@link io.github.msdk.io.mzdb.MzDBRawDataFile MzDBRawDataFile} object containing the
   *         parsed data
   * @throws MSDKException
   */
  @Override
  public MzDBRawDataFile execute() throws MSDKException {
    Iterator<Spectrum> spectrumIterator;
    Spectrum eachSpectrum;
    SpectrumHeader eachSpectrumHeader;
    List<MsScan> msScanList = new ArrayList<>();
    List<String> msFunctions = new ArrayList<>(); // TODO
    List<Chromatogram> chromatograms = new ArrayList<>(); // TODO
    SpectrumData eachSpectrumData;
    int scanNumber = 1;
    float eachSpectrumTime;


    try {
      MzDbReader currentFile = new MzDbReader(mzDBFile, cacheEntities);

      try {
        spectrumIterator = currentFile.getSpectrumIterator();

        while (spectrumIterator.hasNext()) {
          eachSpectrum = spectrumIterator.next();
          eachSpectrumHeader = eachSpectrum.getHeader();
          eachSpectrumData = eachSpectrum.getData();
          double mzList[] = eachSpectrumData.getMzList();
          float intensityList[] = eachSpectrumData.getIntensityList();

          SimpleMsScan eachSimpleMsScan = new SimpleMsScan(scanNumber);
          eachSimpleMsScan.setDataPoints(mzList, intensityList, mzList.length);

          msScanList.add(eachSimpleMsScan);
          scanNumber++;

          // Chromatograms
          // eachSpectrumTime = eachSpectrumHeader.getTime();
          // SimpleChromatogram eachChromatogram = new SimpleChromatogram();
          // eachChromatogram.setDataPoints(rtValues, mzList, intensityList, mzList.length);
        }

      } catch (StreamCorruptedException e) {
        throw new MSDKException("Stream corrupted, detected by mzDB", e);
      }

    } catch (ClassNotFoundException e) {
      throw new MSDKException("Class can't be found by mzDB", e);
    } catch (FileNotFoundException e) {
      throw new MSDKException("File Not Found / File present but inaccessible by mzDB", e);
    } catch (SQLiteException e) {
      throw new MSDKException("SQLite Exception in mzDB", e);
    }

    msFunctions.add("ms");
    MzDBRawDataFile newRawFile =
        new MzDBRawDataFile(mzDBFile, msFunctions, msScanList, chromatograms); // returning dummy
                                                                               // msFunctions and
                                                                               // Chromatograms

    return newRawFile;
  }


  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    return progress;
  }

  /** {@inheritDoc} */
  @Override
  public RawDataFile getResult() {
    return newRawFile;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }


  /**
   * <p>
   * Getter for the field <code>mzDBFile</code>.
   * </p>
   *
   * @return a {@link java.io.File File} instance of the MzDB source if being read from a file <br>
   *         null if the MzDB source is an {@link java.io.InputStream InputStream}
   */
  public File getmzDBFile() {
    return mzDBFile;
  }

}
