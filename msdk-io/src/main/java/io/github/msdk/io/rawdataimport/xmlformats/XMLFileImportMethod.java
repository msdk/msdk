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
import io.github.msdk.datamodel.MSDKObjectBuilder;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.datamodel.rawdata.DataPointList;
import io.github.msdk.datamodel.rawdata.MassSpectrumType;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.datamodel.rawdata.RawDataFileType;
import io.github.msdk.datamodel.rawdata.SeparationType;
import io.github.msdk.datapointstore.DataPointStore;
import io.github.msdk.io.spectrumtypedetection.SpectrumTypeDetectionMethod;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Param;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.UserParam;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzml_wrapper.MzMlWrapper;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;

/**
 * This class reads XML-based mass spec data formats (mzData, mzXML, and mzML)
 * using the jmzreader library.
 */
public class XMLFileImportMethod implements MSDKMethod<RawDataFile> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull File sourceFile;
    private final @Nonnull RawDataFileType fileType;
    private final @Nonnull DataPointStore dataStore;

    private boolean canceled = false;

    private RawDataFile newRawFile;
    private long totalScans = 0, parsedScans;
    private int lastScanNumber = 0;

    private Map<String, Integer> scanIdTable = new Hashtable<String, Integer>();

    public XMLFileImportMethod(@Nonnull File sourceFile,
            @Nonnull RawDataFileType fileType, @Nonnull DataPointStore dataStore) {
        this.sourceFile = sourceFile;
        this.fileType = fileType;
        this.dataStore = dataStore;
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
                throw new MSDKException("This reader cannot read file type "
                        + fileType);
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

            // Store the scan MS level

            // Get the scan number
            String scanId = spectrum.getId();
            int scanNumber = convertScanIdToScanNumber(scanId);

            // Get the MS function
            MsFunction msFunction = extractMsFunction(spectrum);

            // Create a new MsScan instance
            MsScan scan = MSDKObjectBuilder.getMsScan(dataStore, scanNumber,
                    msFunction);

            // Store the chromatography data
            ChromatographyInfo chromData = extractChromatographyData(spectrum);

            // Store the scan data points
            DataPointList dataPoints = extractDataPoints(spectrum);
            scan.setDataPoints(dataPoints);

            // Auto-detect whether this scan is centroided
            SpectrumTypeDetectionMethod detector = new SpectrumTypeDetectionMethod(
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

    private MsFunction extractMsFunction(Spectrum spectrum) {
        Integer msLevel = spectrum.getMsLevel();
        return MSDKObjectBuilder.getMsFunction(msLevel);

    }

    private ChromatographyInfo extractChromatographyData(Spectrum spectrum) {

        ParamGroup params = spectrum.getAdditional();

        ParamGroup additional = spectrum.getAdditional();

        if (false) {
            for (CvParam cvParam : additional.getCvParams()) {
                System.out.println("CV PARAM " + cvParam.getAccession() + " - "
                        + cvParam.getName() + " = " + cvParam.getValue());
            }
            for (Param userParam : additional.getParams()) {
                System.out.println("PARAM " + userParam.getName() + " = "
                        + userParam.getValue());
            }
            for (UserParam userParam : additional.getUserParams()) {
                System.out.println("USER PARAM " + userParam.getName() + " = "
                        + userParam.getValue());
            }
        }

        if (true)
            return null;
        List<CvParam> cvParams = params.getCvParams();
        List<Param> paramss = params.getParams();

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

    private DataPointList extractDataPoints(Spectrum spectrum) {
        Map<Double, Double> jmzreaderPeakList = spectrum.getPeakList();
        DataPointList dataPoints = MSDKObjectBuilder
                .getDataPointList(jmzreaderPeakList.size());

        for (Double mz : jmzreaderPeakList.keySet()) {
            final float intensity = jmzreaderPeakList.get(mz).floatValue();
            dataPoints.add(mz, intensity);
        }

        return dataPoints;

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
