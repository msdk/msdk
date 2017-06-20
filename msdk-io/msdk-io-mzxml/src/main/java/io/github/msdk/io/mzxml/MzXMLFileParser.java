package io.github.msdk.io.mzxml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.impl.SimpleMsScan;
import io.github.msdk.datamodel.rawdata.MsFunction;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import it.unimi.dsi.io.ByteBufferInputStream;
import javolution.text.CharArray;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;

public class MzXMLFileParser {

  Logger logger = Logger.getLogger(this.getClass().getName());

  private final @Nonnull File mzXMLFile;
  private MzXMLRawDataFile newRawFile;
  private volatile boolean canceled;

  private Optional<Integer> scanCount;
  private Optional<String> startTime;
  private Optional<String> endTime;
  private int peaksCount;
  private SimpleMsScan buildingScan;

  final static String TAG_MS_RUN = "msRun";
  final static String TAG_SCAN = "scan";
  final static String TAG_PEAKS = "peaks";
  final static String TAG_PRECURSOR_MZ = "precursorMz";

  public MzXMLFileParser(File mzXMLFile) {
    this.mzXMLFile = mzXMLFile;
    this.canceled = false;
    this.scanCount = Optional.ofNullable(null);
    this.peaksCount = 0;
  }

  public MzXMLFileParser(String mzXMLFileName) {
    this(new File(mzXMLFileName));
  }

  public MzXMLFileParser(Path mzXMLFilePath) {
    this(mzXMLFilePath.toFile());
  }

  public RawDataFile execute() throws MSDKException {

    try {
      MzXMLFileMemoryMapper mapper = new MzXMLFileMemoryMapper();
      ByteBufferInputStream is = mapper.mapToMemory(mzXMLFile);

      final XMLStreamReaderImpl xmlStreamReader = new XMLStreamReaderImpl();
      xmlStreamReader.setInput(is, "UTF-8");

      Vars vars = new Vars();

      int eventType;
      try {

        if (canceled)
          return null;

        loop: do {
          eventType = xmlStreamReader.next();

          switch (eventType) {
            case XMLStreamConstants.START_ELEMENT:
              final CharArray openingTagName = xmlStreamReader.getLocalName();

              if (openingTagName.contentEquals(TAG_MS_RUN)) {
                CharArray scanCount = xmlStreamReader.getAttributeValue(null, "scanCount");
                Integer scanCountInt =
                    scanCount == null ? null : Integer.valueOf(scanCount.toString());
                this.scanCount = Optional.ofNullable(scanCountInt);

              }
              if (openingTagName.contentEquals(TAG_SCAN)) {
                CharArray scanNumber = xmlStreamReader.getAttributeValue(null, "num");
                CharArray msLevel = xmlStreamReader.getAttributeValue(null, "msLevel");
                CharArray peaksCount = xmlStreamReader.getAttributeValue(null, "peaksCount");

                if (scanNumber == null || msLevel == null || peaksCount == null) {
                  throw (new IllegalStateException(
                      "Tag " + openingTagName + " does not contain mandatory attribute"));
                }
                int scanNumberInt = scanNumber.toInt();
                int msLevelInt = msLevel.toInt();
                this.peaksCount = peaksCount.toInt();

                String msFuncName = xmlStreamReader.getAttributeValue(null, "scanType").toString();
                if (Strings.isNullOrEmpty(msFuncName))
                  msFuncName = MsFunction.DEFAULT_MS_FUNCTION_NAME;
                MsFunction msFunc = MSDKObjectBuilder.getMsFunction(msFuncName, msLevelInt);

              }

              break;

            case XMLStreamConstants.END_ELEMENT:
              break;

            case XMLStreamConstants.CHARACTERS:
              break;
          }

        } while (eventType != XMLStreamConstants.END_DOCUMENT);

      } finally {
        if (xmlStreamReader != null) {
          xmlStreamReader.close();
        }
      }
    } catch (IOException | XMLStreamException | javax.xml.stream.XMLStreamException e) {
      throw (new MSDKException(e));
    }
    return newRawFile;

  }

}



class Vars {
  Vars() {

  }
}
