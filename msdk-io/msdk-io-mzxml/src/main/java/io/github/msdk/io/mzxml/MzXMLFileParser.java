package io.github.msdk.io.mzxml;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;

public class MzXMLFileParser {

  Logger logger = Logger.getLogger(this.getClass().getName());

  private final @Nonnull File mzXMLFile;
  private MzXMLRawDataFile newRawFile;

  public MzXMLFileParser(File mzXMLFile) {
    this.mzXMLFile = mzXMLFile;
  }

  public MzXMLFileParser(String mzXMLFileName) {
    this(new File(mzXMLFileName));
  }

  public MzXMLFileParser(Path mzXMLFilePath) {
    this(mzXMLFilePath.toFile());
  }

  public RawDataFile execute() throws MSDKException {
    
    try {
      
    }
    
  }

}
