package io.github.msdk.io.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.files.FileType;
import io.github.msdk.datamodel.impl.SimpleRawDataFile;
import ucar.nc2.NetcdfFile;

public class NetCDFRawDataFile extends SimpleRawDataFile {

  private NetcdfFile inputFile;

  public NetCDFRawDataFile(String rawDataFileName, Optional<File> originalRawDataFile,
      FileType rawDataFileType, NetcdfFile inputFile) {
    super(rawDataFileName, originalRawDataFile, rawDataFileType);
    this.inputFile = inputFile;
  }

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
