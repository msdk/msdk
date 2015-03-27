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

package io.github.msdk.io.rawdataimport.xmlformats;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.DataPoint;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.RawDataFileType;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionAlgorithm;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.SortingDirection;
import io.github.msdk.util.SortingProperty;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzml_wrapper.MzMlWrapper;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 * This class reads XML-based mass spec data formats (mzData, mzXML, and mzML)
 * using the jmzreader library.
 */
public class XMLFileImportAlgorithm implements MSDKMethod<RawDataFile> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private final File sourceFile;
    private final RawDataFileType fileType;
    private boolean canceled = false;

    private RawDataFile newRawFile;
    private long totalScans = 0, parsedScans;
    private int lastScanNumber = 0;

    private Map<String, Integer> scanIdTable = new Hashtable<String, Integer>();

    public XMLFileImportAlgorithm(File sourceFile, RawDataFileType fileType) {
        this.sourceFile = sourceFile;
        this.fileType = fileType;
    }

    /**
     * @throws JMzReaderException
     * @throws MzXMLParsingException
     * @throws MSDKException
     */
    @Override
    public RawDataFile execute() throws MSDKException {

        logger.info("Started parsing file " + sourceFile);

        newRawFile = MSDKObjectBuilder.getRawDataFile();
        newRawFile.setName(sourceFile.getName());

        JMzReader parser = null;

        try {
            switch (fileType) {
            case MZDATA:
                parser = new MzDataFile(sourceFile);
                break;
            case MZML:
                parser = new MzMlWrapper(sourceFile);
                break;
            case MZXML:
                parser = new MzXMLFile(sourceFile);
                break;
            default:
                throw new IllegalArgumentException(
                        "This reader cannot read file type " + fileType);
            }
        } catch (Exception e) {
            throw new MSDKException(e);
        }

        totalScans = parser.getSpectraCount();

        Iterator<Spectrum> iterator = parser.getSpectrumIterator();

        while (iterator.hasNext()) {

            if (canceled)
                return null;

            Spectrum spectrum = iterator.next();

            // Create a new MsScan or MsMsScan instance depending on the MS
            // level
            Integer msLevel = spectrum.getMsLevel();
            MsScan scan = MSDKObjectBuilder.getMsScan(newRawFile);
            if ((msLevel != null) && (msLevel > 1)) {
            }

            // Store the scan MS level

            // Store the scan number
            String scanId = spectrum.getId();
            int scanNumber = convertScanIdToScanNumber(scanId);
            scan.setScanNumber(scanNumber);

            // Get parent scan number
            int parentScan = extractParentScanNumber(spectrum);

            // Store the chromatography data
            ChromatographyInfo chromData = extractChromatographyData(spectrum);

            // Store the scan data points
            DataPoint dataPoints[] = extractDataPoints(spectrum);
            // scan.setDataPoints(dataPoints);

            // Auto-detect whether this scan is centroided
            SpectrumTypeDetectionAlgorithm detector = new SpectrumTypeDetectionAlgorithm(
                    scan);
            detector.execute();
            MassSpectrumType spectrumType = detector.getResult();
            scan.setSpectrumType(spectrumType);

            // Add the scan to the final raw data file
            newRawFile.addScan(scan);

            parsedScans++;

        }

        logger.info("Finished importing " + sourceFile + ", parsed "
                + parsedScans + " scans");

        return newRawFile;

    }

    private int convertScanIdToScanNumber(String scanId) {

        if (scanIdTable.containsKey(scanId))
            return scanIdTable.get(scanId);

        final Pattern pattern = Pattern.compile("scan=([0-9]+)");
        final Matcher matcher = pattern.matcher(scanId);
        boolean scanNumberFound = matcher.find();

        // Some vendors include scan=XX in the ID, some don't, such as
        // mzML converted from WIFF files. See the definition of nativeID in
        // http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo
        if (scanNumberFound) {
            int scanNumber = Integer.parseInt(matcher.group(1));
            lastScanNumber = scanNumber;
            scanIdTable.put(scanId, scanNumber);
            return scanNumber;
        }

        int scanNumber = lastScanNumber + 1;
        lastScanNumber++;
        scanIdTable.put(scanId, scanNumber);
        return scanNumber;
    }

    private ChromatographyInfo extractChromatographyData(Spectrum spectrum) {

        ParamGroup params = spectrum.getAdditional();

        ParamGroup additional = spectrum.getAdditional();
        /*
         * 
         * for (CvParam cvParam : additional.getCvParams()) {
         * System.out.println("CV PARAM "+ cvParam.getAccession() + " - " +
         * cvParam.getName() + " = " + cvParam.getValue()); } for (UserParam
         * userParam : additional.getUserParams()) {
         * System.out.println("USER PARAM "+ userParam.getName() + " = " +
         * userParam.getValue()); }
         */

        List<CvParam> cvParams = params.getCvParams();
        List<Param> paramsList = params.getParams();

        for (CvParam param : cvParams) {
            String accession = param.getAccession();
            // String unitAccession = param.getUnitAccession();
            String value = param.getValue();
            if ((accession == null) || (value == null))
                continue;

            // Retention time (actually "Scan start time") MS:1000016
            if (accession.equals("MS:1000016")) {
                // MS:1000038 is used in mzML 1.0, while UO:0000031
                // is used in mzML 1.1.0 :-/
                double retentionTime;
                String unitAccession = "UO:0000031";
                if ((unitAccession == null)
                        || (unitAccession.equals("MS:1000038"))
                        || unitAccession.equals("UO:0000031")) {
                    retentionTime = Double.parseDouble(value);
                } else {
                    retentionTime = Double.parseDouble(value) / 60d;
                }
                // TODO Update with specific code for separation method
                final ChromatographyInfo newChromData = MSDKObjectBuilder
                        .getChromatographyInfo1D(SeparationType.GC,
                                (float) retentionTime);
                return newChromData;

            }
        }

        return null;
    }

    private DataPoint[] extractDataPoints(Spectrum spectrum) {
        Map<Double, Double> jmzreaderPeakList = spectrum.getPeakList();
        DataPoint dataPoints[] = new DataPoint[jmzreaderPeakList.size()];
        int i = 0;
        for (Double mz : jmzreaderPeakList.keySet()) {
            float intensity = jmzreaderPeakList.get(mz).floatValue();
            dataPoints[i] = MSDKObjectBuilder.getDataPoint(mz, intensity);
            i++;
        }
        Arrays.sort(dataPoints, new DataPointSorter(SortingProperty.MZ,
                SortingDirection.ASCENDING));
        return dataPoints;

    }

    private int extractParentScanNumber(Spectrum spectrum) {

        /*
         * PrecursorList precursorListElement = spectrum.getPrecursorList(); if
         * ((precursorListElement == null) ||
         * (precursorListElement.getCount().equals(0))) return -1;
         * 
         * List<Precursor> precursorList = precursorListElement.getPrecursor();
         * for (Precursor parent : precursorList) { // Get the precursor scan
         * number String precursorScanId = parent.getSpectrumRef(); if
         * (precursorScanId == null) {
         * logger.warning("Missing precursor spectrumRef tag for spectrum ID " +
         * spectrum.getId()); return -1; } int parentScan =
         * convertScanIdToScanNumber(precursorScanId); return parentScan; }
         */
        return -1;
    }

    @Override
    public Float getFinishedPercentage() {
        return totalScans == 0 ? null : (float) parsedScans / totalScans;
    }

    @Override
    @Nullable
    public RawDataFile getResult() {
        return newRawFile;
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }

}
