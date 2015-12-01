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

package io.github.msdk.io.mzml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.MSDKVersion;
import io.github.msdk.datamodel.chromatograms.Chromatogram;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CV;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.DataProcessing;
import uk.ac.ebi.jmzml.model.mzml.DataProcessingList;
import uk.ac.ebi.jmzml.model.mzml.ProcessingMethod;
import uk.ac.ebi.jmzml.model.mzml.Software;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.utilities.CommonCvParams;
import uk.ac.ebi.jmzml.xml.io.MzMLInstantMarshaller;

/**
 * This class reads mzML data format using the jmzml library.
 */
public class MzMLFileExportMethod implements MSDKMethod<Void> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull RawDataFile rawDataFile;
    private final @Nonnull File target;

    private boolean canceled = false;

    private long totalScans = 0, totalChromatograms = 0, parsedScans,
            parsedChromatograms;

    /**
     * <p>
     * Constructor for MzMLFileExportMethod.
     * </p>
     *
     * @param targetFile
     *            a {@link java.io.File} object.
     */
    public MzMLFileExportMethod(@Nonnull RawDataFile rawDataFile,
            @Nonnull File target) {
        this.rawDataFile = rawDataFile;
        this.target = target;
    }

    /** {@inheritDoc} */
    @Override
    public Void execute() throws MSDKException {

        logger.info(
                "Started export of " + rawDataFile.getName() + " to " + target);

        List<MsScan> scans = rawDataFile.getScans();
        List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
        totalScans = scans.size();
        totalChromatograms = chromatograms.size();
        final MsSpectrumDataPointList dataPoints = MSDKObjectBuilder
                .getMsSpectrumDataPointList();
        final Map<String, String> atts = new Hashtable<>();

        try {

            // Open the target file for writing
            FileWriter writer = new FileWriter(target);
            MzMLInstantMarshaller marshaller = new MzMLInstantMarshaller();

            // <mzML>
            writer.write(marshaller.createXmlHeader());
            writer.write("\n");
            writer.write(marshaller.createMzMLStartTag(rawDataFile.getName()));
            writer.write("\n");

            // <cvList>
            CVList cvList = new CVList();
            writer.write(marshaller.marshall(cvList));
            writer.write("\n");

            // <dataProcessingList>

            ProcessingMethod pm = new ProcessingMethod();
            DataProcessingList dpl = new DataProcessingList();
            DataProcessing dp = new DataProcessing();
            dp.setId("MSDK_mzml_export");
            pm.setOrder(0);
            Software msdk = new Software();
            msdk.setId("msdk");
            msdk.setVersion(MSDKVersion.getMSDKVersion());
            pm.setSoftware(msdk);
            dp.getProcessingMethod().add(pm);
            dpl.getDataProcessing().add(dp);
            dpl.setCount(1);
            writer.write(marshaller.marshall(dpl));
            writer.write("\n");

            // <run>
            atts.put("id", rawDataFile.getName());
            writer.write(marshaller.createRunStartTag(atts));
            writer.write("\n");

            // <spectrumList>
            atts.clear();
            atts.put("count", String.valueOf(scans.size()));
            writer.write(marshaller.createSpecListStartTag(atts));
            writer.write("\n");

            final BinaryDataArray bdaMz = new BinaryDataArray();
            final BinaryDataArray bdaInt = new BinaryDataArray();
            final CV centroidCV = new CV();
            centroidCV.setId(MzMLCV.cvCentroidSpectrum);
            centroidCV.setFullName("centroid mass spectrum");
            final CV profileCV = new CV();
            profileCV.setId(MzMLCV.cvProfileSpectrum);
            profileCV.setFullName("profile spectrum");

            for (MsScan scan : scans) {

                scan.getDataPoints(dataPoints);

                /*
                 * ScanList scanList = new ScanList(); Scan mzMLScan = new
                 * Scan();
                 */

                bdaMz.set64BitFloatArrayAsBinaryData(dataPoints.getMzBuffer(),
                        true, CommonCvParams.MZ_PARAM.getCv());
                bdaInt.set32BitFloatArrayAsBinaryData(
                        dataPoints.getIntensityBuffer(), true,
                        CommonCvParams.INTENSITY_PARAM.getCv());
                bdaMz.setArrayLength(dataPoints.getSize());
                bdaInt.setArrayLength(dataPoints.getSize());

                BinaryDataArrayList bdal = new BinaryDataArrayList();
                bdal.setCount(2);
                bdal.getBinaryDataArray().add(bdaMz);
                bdal.getBinaryDataArray().add(bdaInt);

                // <spectrum>
                Spectrum spectrum = new Spectrum();
                spectrum.setId(String.valueOf(scan.getScanNumber()));
                spectrum.setIndex((int) parsedScans);
                spectrum.setBinaryDataArrayList(bdal);
                spectrum.setDefaultArrayLength(bdaMz.getArrayLength());
                
                // <cvParamList>
                List<CVParam> cvParams = spectrum.getCvParam();
                final CVParam cvParam = new CVParam();
                if (scan.getSpectrumType() == MsSpectrumType.CENTROIDED)
                    cvParam.setCv(centroidCV);
                else
                    cvParam.setCv(profileCV);
                cvParams.add(cvParam);
                writer.write(marshaller.marshall(spectrum));
                writer.write("\n");

                parsedScans++;

            }

            writer.write(marshaller.createSpecListCloseTag());
            writer.write("\n");

            for (Chromatogram chromatogram : chromatograms) {

                parsedChromatograms++;
            }

            // Write ending tags
            writer.write(marshaller.createRunCloseTag());
            writer.write("\n");
            writer.write(marshaller.createMzMLCloseTag());
            writer.write("\n");

            // Close file
            writer.close();

        } catch (IOException e) {
            throw new MSDKException(e);
        }

        logger.info("Finished export of " + rawDataFile.getName());
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Float getFinishedPercentage() {
        return (totalScans + totalChromatograms) == 0 ? null
                : (float) (parsedScans + parsedChromatograms)
                        / (totalScans + totalChromatograms);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Void getResult() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        this.canceled = true;
    }

}
