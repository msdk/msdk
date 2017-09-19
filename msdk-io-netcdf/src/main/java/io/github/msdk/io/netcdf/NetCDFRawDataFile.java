package io.github.msdk.io.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.FileType;
import io.github.msdk.datamodel.SimpleRawDataFile;
import ucar.nc2.NetcdfFile;

/**
 * <p>NetCDFRawDataFile class.</p>
 *
 */
public class NetCDFRawDataFile extends SimpleRawDataFile {

  private NetcdfFile inputFile;

  /**
   * <p>Constructor for NetCDFRawDataFile.</p>
   *
   * @param rawDataFileName a {@link java.lang.String} object.
   * @param originalRawDataFile a {@link java.util.Optional} object.
   * @param rawDataFileType a {@link io.github.msdk.datamodel.FileType} object.
   * @param inputFile a {@link ucar.nc2.NetcdfFile} object.
   */
  public NetCDFRawDataFile(String rawDataFileName, Optional<File> originalRawDataFile,
      FileType rawDataFileType, NetcdfFile inputFile) {
    super(rawDataFileName, originalRawDataFile, rawDataFileType);
    this.inputFile = inputFile;
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    try {
      inputFile.close();
    } catch (IOException e) {
      new MSDKRuntimeException(e);
    }
    super.dispose();
  }

}
