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
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.PolarityType;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.DataProcessing;
import uk.ac.ebi.jmzml.model.mzml.DataProcessingList;
import uk.ac.ebi.jmzml.model.mzml.ProcessingMethod;
import uk.ac.ebi.jmzml.model.mzml.Scan;
import uk.ac.ebi.jmzml.model.mzml.ScanList;
import uk.ac.ebi.jmzml.model.mzml.Software;
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.utilities.CommonCvParams;
import uk.ac.ebi.jmzml.xml.io.MzMLInstantMarshaller;

/**
 * This class exports RawDataFile objects into mzML data format using the jmzml library.
 *
 */
public class MzMLFileExportMethod implements MSDKMethod<Void> {

  private static final String dataProcessingId = "MSDK_mzml_export";
  private static final String softwareId = "MSDK";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull RawDataFile rawDataFile;
  private final @Nonnull File target;

  private boolean canceled = false;

  private long totalScans = 0, totalChromatograms = 0, parsedScans, parsedChromatograms;

  /**
   * <p>
   * Constructor for MzMLFileExportMethod.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object.
   * @param target a {@link java.io.File} object.
   */
  public MzMLFileExportMethod(@Nonnull RawDataFile rawDataFile, @Nonnull File target) {
    this.rawDataFile = rawDataFile;
    this.target = target;
  }

  /** {@inheritDoc} */
  @Override
  public Void execute() throws MSDKException {

    logger.info("Started export of " + rawDataFile.getName() + " to " + target);

    List<MsScan> scans = rawDataFile.getScans();
    List<Chromatogram> chromatograms = rawDataFile.getChromatograms();
    totalScans = scans.size();
    totalChromatograms = chromatograms.size();

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
      dp.setId(dataProcessingId);
      pm.setOrder(0);
      Software msdk = new Software();
      msdk.setId(softwareId);
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

      // prepare BinaryDataArray instances
      final BinaryDataArray bdaMz = new BinaryDataArray();
      final BinaryDataArray bdaInt = new BinaryDataArray();

      double mzBuffer[] = new double[10000];
      float intensityBuffer[] = new float[10000];

      for (MsScan scan : scans) {

        if (canceled) {
          writer.close();
          target.delete();
          return null;
        }

        // Convert data points to BinaryDataArrays
        mzBuffer = scan.getMzValues();
        intensityBuffer = scan.getIntensityValues();
        int size = scan.getNumberOfDataPoints();
        bdaMz.set64BitFloatArrayAsBinaryData(mzBuffer, true, CommonCvParams.MZ_PARAM.getCv());
        bdaInt.set32BitFloatArrayAsBinaryData(intensityBuffer, true,
            CommonCvParams.INTENSITY_PARAM.getCv());
        bdaMz.setArrayLength(size);
        bdaInt.setArrayLength(size);
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

        rawDataFile.getOriginalFile().ifPresent(originalFile -> {
          SourceFile sourceFile = new SourceFile();
          sourceFile.setLocation(originalFile.getPath());
          sourceFile.setName(originalFile.getName());
          spectrum.setSourceFile(sourceFile);
        });

        // spectrum type CV param
        if (scan.getSpectrumType() == MsSpectrumType.CENTROIDED)
          spectrum.getCvParam().add(MzMLCV.centroidCvParam);
        else
          spectrum.getCvParam().add(MzMLCV.profileCvParam);

        // ms level CV param
        if (scan.getMsLevel() != null) {
          Integer msLevel = scan.getMsLevel();
          CVParam msLevelCvParam = new CVParam();
          msLevelCvParam.setAccession(MzMLCV.cvMSLevel);
          msLevelCvParam.setName("ms level");
          msLevelCvParam.setValue(String.valueOf(msLevel));
          spectrum.getCvParam().add(msLevelCvParam);
        }

        // <scan>
        ScanList scanList = new ScanList();
        scanList.setCount(1);
        spectrum.setScanList(scanList);
        Scan mzMlScan = new Scan();
        scanList.getScan().add(mzMlScan);

        // retention time CV param
        if (scan.getRetentionTime() != null) {
          Float rt = scan.getRetentionTime();
          CVParam rtCvParam = new CVParam();
          rtCvParam.setAccession(MzMLCV.cvScanStartTime);
          rtCvParam.setName("scan time");
          rtCvParam.setValue(String.valueOf(rt));
          rtCvParam.setUnitAccession(MzMLCV.cvUnitsSec);
          rtCvParam.setUnitName("second");
          mzMlScan.getCvParam().add(rtCvParam);
        }

        // scan polarity CV param
        if (scan.getPolarity() == PolarityType.POSITIVE)
          mzMlScan.getCvParam().add(MzMLCV.polarityPositiveCvParam);
        else if (scan.getPolarity() == PolarityType.NEGATIVE)
          mzMlScan.getCvParam().add(MzMLCV.polarityNegativeCvParam);

        // write the spectrum
        writer.write(marshaller.marshall(spectrum));
        writer.write("\n");

        parsedScans++;

      }

      writer.write(marshaller.createSpecListCloseTag());
      writer.write("\n");

      for (Chromatogram chromatogram : chromatograms) {
        if (canceled) {
          writer.close();
          target.delete();
          return null;
        }

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
        : (float) (parsedScans + parsedChromatograms) / (totalScans + totalChromatograms);
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
