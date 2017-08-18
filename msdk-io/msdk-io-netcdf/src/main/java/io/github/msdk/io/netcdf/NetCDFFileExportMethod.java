package io.github.msdk.io.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;

public class NetCDFFileExportMethod implements MSDKMethod<Void> {

  private final @Nonnull RawDataFile rawDataFile;
  private final @Nonnull File target;
  private final @Nonnull double massValueScaleFactor;
  private final @Nonnull double intensityValueScaleFactor;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private boolean canceled = false;
  private int totalScans = 0;
  private float progress = 0f;

  public NetCDFFileExportMethod(@Nonnull RawDataFile rawDataFile, @Nonnull File target) {
    this(rawDataFile, target, 1, 1);
  }

  public NetCDFFileExportMethod(@Nonnull RawDataFile rawDataFile, @Nonnull File target,
      double massValueScaleFactor, double intensityValueScaleFactor) {
    this.rawDataFile = rawDataFile;
    this.target = target;
    this.massValueScaleFactor = massValueScaleFactor;
    this.intensityValueScaleFactor = intensityValueScaleFactor;
  }

  @Override
  public Void execute() throws MSDKException {

    logger.info("Started export of " + rawDataFile.getName() + " to " + target);

    List<MsScan> scans = rawDataFile.getScans();
    totalScans = scans.size();
    int scanStartPositions[] = new int[totalScans + 1];

    try {
      NetcdfFileWriter writer =
          NetcdfFileWriter.createNew(Version.netcdf3, target.getAbsolutePath());

      // Populate the scan indices
      Array scanIndexArray = Array.factory(int.class, new int[] {totalScans});
      int idx = 0;
      for (MsScan scan : scans) {
        if (canceled) {
          writer.close();
          return null;
        }

        scanStartPositions[idx + 1] = scanStartPositions[idx] + scan.getNumberOfDataPoints();
        idx++;

      }

      for (int i = 0; i < scanStartPositions.length - 1; i++)
        scanIndexArray.setInt(i, scanStartPositions[i]);

      if (canceled) {
        writer.close();
        return null;
      }

      // Write the scan indices
      Dimension scanNumberDim = writer.addDimension(null, "scan_number", totalScans);
      List<Dimension> scanIndexDims = new ArrayList<>();
      scanIndexDims.add(scanNumberDim);

      // Create scan index varaible
      Variable scanIndexVariable =
          writer.addVariable(null, "scan_index", DataType.INT, scanIndexDims);

      progress = 0.25f;

      // data values storage dimension
      Dimension pointNumDim =
          writer.addDimension(null, "point_number", scanStartPositions[totalScans]);
      List<Dimension> pointNumValDims = new ArrayList<>();
      pointNumValDims.add(pointNumDim);

      // populate the mass values
      Array massValueArray =
          Array.factory(double.class, new int[] {scanStartPositions[totalScans]});
      idx = 0;
      for (MsScan scan : scans) {
        double mzValues[] = scan.getMzValues();
        if (canceled) {
          writer.close();
          return null;
        }

        for (int i = 0; i < mzValues.length; i++)
          massValueArray.setDouble(idx++, mzValues[i]);
      }

      // Write the mass values
      Variable massValueVariable =
          writer.addVariable(null, "mass_values", DataType.FLOAT, pointNumValDims);
      massValueVariable.addAttribute(new Attribute("units", "M/Z"));
      massValueVariable.addAttribute(new Attribute("scale_factor", massValueScaleFactor));

      progress = 0.5f;

      // populate the intensity values
      Array intensityValueArray =
          Array.factory(double.class, new int[] {scanStartPositions[totalScans]});
      idx = 0;
      for (MsScan scan : scans) {
        if (canceled) {
          writer.close();
          return null;
        }
        float intensityValues[] = scan.getIntensityValues();
        for (int i = 0; i < intensityValues.length; i++)
          intensityValueArray.setDouble(idx++, intensityValues[i]);
      }

      // Write the intensity values
      Variable intensityValueVariable =
          writer.addVariable(null, "intensity_values", DataType.FLOAT, pointNumValDims);
      intensityValueVariable.addAttribute(new Attribute("units", "Arbitrary Intensity Units"));
      intensityValueVariable.addAttribute(new Attribute("scale_factor", intensityValueScaleFactor));

      progress = 0.75f;

      // Populate scan times
      Array scanTimeArray = Array.factory(float.class, new int[] {totalScans});
      idx = 0;
      for (MsScan scan : scans)
        scanTimeArray.setFloat(idx++, scan.getRetentionTime());

      if (canceled) {
        writer.close();
        return null;
      }

      // Create the scan times variable
      Variable scanTimeVariable =
          writer.addVariable(null, "scan_acquisition_time", DataType.FLOAT, scanIndexDims);

      // Create the netCDF-3 file
      writer.create();

      // Write out the non-record variables
      writer.write(scanIndexVariable, scanIndexArray);
      writer.write(massValueVariable, massValueArray);
      writer.write(intensityValueVariable, intensityValueArray);
      writer.write(scanTimeVariable, scanTimeArray);

      // Close the writer
      writer.close();

      progress = 1.0f;

    } catch (IOException | InvalidRangeException e) {
      new MSDKException(e);
    }

    return null;
  }

  @Override
  public Float getFinishedPercentage() {
    return progress;
  }

  @Override
  public Void getResult() {
    return null;
  }

  @Override
  public void cancel() {
    this.canceled = true;
  }

}
