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

package io.github.msdk.io.mzml2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.mzml2.data.MzMLParser;
import io.github.msdk.io.mzml2.data.MzMLRawDataFile;
import io.github.msdk.io.mzml2.util.ByteBufferInputStream;
import io.github.msdk.io.mzml2.util.FileMemoryMapper;
import javolution.text.CharArray;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;

/**
 * <p>
 * MzMLFileParser class.
 * </p>
 *
 */
public class MzMLFileImportMethod implements MSDKMethod<RawDataFile> {
  private final File mzMLFile;
  final InputStream inputStream;
  private MzMLRawDataFile newRawFile;
  private volatile boolean canceled;
  private Float progress;
  private int lastLoggedProgress;
  private Logger logger;
  private Predicate<MsScan> msScanPredicate;
  private Predicate<Chromatogram> chromatogramPredicate;

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.lang.String} object.
   */
  public MzMLFileImportMethod(String mzMLFilePath) {
    this(new File(mzMLFilePath), null, null);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.lang.String} object.
   */
  public MzMLFileImportMethod(String mzMLFilePath, Predicate<MsScan> msScanPredicate,
      Predicate<Chromatogram> chromatogramPredicate) {
    this(new File(mzMLFilePath), msScanPredicate, chromatogramPredicate);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.nio.file.Path} object.
   */
  public MzMLFileImportMethod(Path mzMLFilePath) {
    this(mzMLFilePath.toFile(), null, null);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFilePath a {@link java.nio.file.Path} object.
   */
  public MzMLFileImportMethod(Path mzMLFilePath, Predicate<MsScan> msScanPredicate,
      Predicate<Chromatogram> chromatogramPredicate) {
    this(mzMLFilePath.toFile(), msScanPredicate, chromatogramPredicate);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFile a {@link java.io.File} object.
   */
  public MzMLFileImportMethod(File mzMLFile) {
    this(mzMLFile, null, null, null);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param mzMLFile a {@link java.io.File} object.
   */
  public MzMLFileImportMethod(File mzMLFile, Predicate<MsScan> msScanPredicate,
      Predicate<Chromatogram> chromatogramPredicate) {
    this(mzMLFile, null, msScanPredicate, chromatogramPredicate);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param inputStream a {@link java.io.InputStream} object.
   */
  public MzMLFileImportMethod(InputStream inputStream) {
    this(null, inputStream, null, null);
  }

  /**
   * <p>
   * Constructor for MzMLFileParser.
   * </p>
   *
   * @param inputStream a {@link java.io.InputStream} object.
   */
  public MzMLFileImportMethod(InputStream inputStream, Predicate<MsScan> msScanPredicate,
      Predicate<Chromatogram> chromatogramPredicate) {
    this(null, inputStream, msScanPredicate, chromatogramPredicate);
  }

  private MzMLFileImportMethod(File mzMLFile, InputStream inputStream,
      Predicate<MsScan> msScanPredicate, Predicate<Chromatogram> chromatogramPredicate) {
    this.mzMLFile = mzMLFile;
    this.inputStream = inputStream;
    this.canceled = false;
    this.progress = 0f;
    this.lastLoggedProgress = 0;
    this.logger = LoggerFactory.getLogger(this.getClass());
    this.msScanPredicate = msScanPredicate != null ? msScanPredicate : s -> true;
    this.chromatogramPredicate = chromatogramPredicate != null ? chromatogramPredicate : c -> true;
  }

  /**
   * <p>
   * execute.
   * </p>
   *
   * @return a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object. @throws
   *         io.github.msdk.MSDKException if any. @throws
   */
  @Override
  public MzMLRawDataFile execute() throws MSDKException {

    try {

      InputStream is = null;

      if (mzMLFile != null) {
        logger.info("Began parsing file: " + mzMLFile.getAbsolutePath());
        is = FileMemoryMapper.mapToMemory(mzMLFile);
      } else if (inputStream != null) {
        logger.info("Began parsing file from stream");
        is = inputStream;
      } else {
        throw new MSDKException("Invalid input");
      }
      // It's ok to directly create this particular reader, this class is `public final`
      // and we precisely want that fast UFT-8 reader implementation
      final XMLStreamReaderImpl xmlStreamReader = new XMLStreamReaderImpl();
      xmlStreamReader.setInput(is, "UTF-8");

      MzMLParser parser = new MzMLParser(this);
      this.newRawFile = parser.getMzMLRawFile();

      lastLoggedProgress = 0;

      int eventType;
      try {
        do {
          // check if parsing has been cancelled?
          if (canceled)
            return null;

          eventType = xmlStreamReader.next();

          // XXX Can't track progress this way now, switched to using the primitive InputStream
          // without the length() function
          // Update: We can track progress if source is a file
          if (mzMLFile != null)
            progress = ((float) (xmlStreamReader.getLocation().getCharacterOffset())
                / ((ByteBufferInputStream) is).length());

          // Log progress after every 10% completion
          if ((int) (progress * 100) >= lastLoggedProgress + 10) {
            lastLoggedProgress = (int) (progress * 10) * 10;
            logger.debug("Parsing in progress... " + lastLoggedProgress + "% completed");
          }

          switch (eventType) {
            case XMLStreamConstants.START_ELEMENT:
              final CharArray openingTagName = xmlStreamReader.getLocalName();
              parser.processOpeningTag(xmlStreamReader, is, openingTagName);
              break;

            case XMLStreamConstants.END_ELEMENT:
              final CharArray closingTagName = xmlStreamReader.getLocalName();
              parser.processClosingTag(xmlStreamReader, closingTagName);
              break;

            case XMLStreamConstants.CHARACTERS:
              parser.processCharacters(xmlStreamReader);
              break;
          }

        } while (eventType != XMLStreamConstants.END_DOCUMENT);

      } finally {
        if (xmlStreamReader != null)
          xmlStreamReader.close();
      }
      progress = 1f;
      logger.info("Parsing Complete");
    } catch (IOException | XMLStreamException e) {
      throw (new MSDKException(e));
    }

    progress = 1f;
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

  public Predicate<MsScan> getMsScanPredicate() {
    return msScanPredicate;
  }

  public Predicate<Chromatogram> getChromatogramPredicate() {
    return chromatogramPredicate;
  }

  public File getMzMLFile() {
    return mzMLFile;
  }

}
