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

package io.github.msdk.io.mzxml;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

/**
 * This class reads XML-based mass spec data formats (mzData, mzXML) using the
 * jmzreader library.
 */
class MzXMLRawDataFile implements RawDataFile {

    private final @Nonnull File sourceFile;
    private final @Nonnull FileType fileType;
    private @Nullable MzXMLFile parser;

    private final @Nonnull List<MsFunction> msFunctions;
    private final @Nonnull List<MsScan> msScans;
    private final @Nonnull List<Chromatogram> chromatograms;

    private @Nonnull String name;

    /**
     * <p>Constructor for MzXMLRawDataFile.</p>
     *
     * @param sourceFile a {@link java.io.File} object.
     * @param fileType a {@link io.github.msdk.datamodel.files.FileType} object.
     * @param parser a {@link uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile} object.
     * @param msFunctions a {@link java.util.List} object.
     * @param msScans a {@link java.util.List} object.
     * @param chromatograms a {@link java.util.List} object.
     */
    @SuppressWarnings("null")
    public MzXMLRawDataFile(@Nonnull File sourceFile,
            @Nonnull FileType fileType, @Nonnull MzXMLFile parser,
            List<MsFunction> msFunctions, List<MsScan> msScans,
            List<Chromatogram> chromatograms) {
        this.sourceFile = sourceFile;
        this.fileType = fileType;
        this.parser = parser;
        this.name = sourceFile.getName();
        this.msFunctions = msFunctions;
        this.msScans = msScans;
        this.chromatograms = chromatograms;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@Nonnull String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public File getOriginalFile() {
        return sourceFile;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public FileType getRawDataFileType() {
        return fileType;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    @Nonnull
    public List<MsFunction> getMsFunctions() {
        return ImmutableList.copyOf(msFunctions);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("null")
    @Override
    @Nonnull
    public List<MsScan> getScans() {
        return ImmutableList.copyOf(msScans);
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
    public void dispose() {
        parser = null;
    }

    @Nullable
    MzXMLFile getParser() {
        return parser;
    }

    /*
     * Unsupported set-operations
     */

    /** {@inheritDoc} */
    @Override
    public void setOriginalFile(@Nullable File newOriginalFile) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setRawDataFileType(@Nonnull FileType rawDataFileType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void addScan(@Nonnull MsScan scan) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removeScan(@Nonnull MsScan scan) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void addChromatogram(@Nonnull Chromatogram chromatogram) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void removeChromatogram(@Nonnull Chromatogram chromatogram) {
        throw new UnsupportedOperationException();
    }

}
