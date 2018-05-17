package io.github.msdk.io.mgf;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.util.ArrayUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by evger on 17-May-18.
 */
public class MgfFileImportMethod implements MSDKMethod<Collection<MsSpectrum>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private Collection<MsSpectrum> spectra;
  private final @Nonnull File target;
  private boolean canceled = false;
  private long processedSpectra = 0;
  private Hashtable<String, Pattern> patterns;

  @Nullable
  @Override
  public Collection<MsSpectrum> execute() throws MSDKException {
    logger.info("Started MGF import from {} file", target);

    double mzValues[] = null;
    float intensityValues[] = null;
    int numOfDataPoints;
    try (final BufferedReader reader = new BufferedReader(new FileReader(target))) {
      String line;
      while ((line = reader.readLine()) != null) {
        switch (line) {
          case "BEGIN IONS":
            spectra.add(processSpectrum(reader));
            processedSpectra++;
            break;
        }
      }

      reader.close();
    } catch (IOException e) {
//      TODO: Eliminate catching of this exception
      System.out.println("Well");
    }

      return spectra;
  }

  private MsSpectrum processSpectrum(BufferedReader reader) throws IOException {
    String line;
    int precursorCharge;
    double precursorMass;
    Matcher matcher;
    double mz[] = new double[16];
    double intensive[] = new double[16];
    int index = 0;

    while ((line = reader.readLine()) != "END IONS") {
      if (line.contains("PEPMASS")) {
        matcher = patterns.get("PEPMASS").matcher(line);
        String d = matcher.group();
        System.out.println(d);
      } else if (line.contains("TITLE")) {
        matcher = patterns.get("TITLE").matcher(line);
        String d = matcher.group();
        System.out.println(d);
      } else if (line.contains("CHARGE")) {
        matcher = patterns.get("CHARGE").matcher(line);
        String d = matcher.group();
        System.out.println(d);
      } else {
        String[] doubles = line.split(" ");
        mz = ArrayUtil.addToArray(mz, Double.parseDouble(doubles[0]), index);
        intensive = ArrayUtil.addToArray(intensive, Double.parseDouble(doubles[1]), index);
        index++;
      }
    }


    return new SimpleMsSpectrum();
  }

  /** {@inheritDoc} */
  @Override
  public Float getFinishedPercentage() {
    int totalSpectra = spectra.size();
    return totalSpectra == 0 ? null : (float) (processedSpectra / totalSpectra);
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Collection<MsSpectrum> getResult() {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    this.canceled = true;
  }

  public MgfFileImportMethod(File target) {
    this.target = target;
    patterns.put("PEPMASS", Pattern.compile("PEPMASS=(\\d+\\.\\d+)"));
    patterns.put("CHARGE", Pattern.compile("CHARGE=(\\d+\\.\\d+)+|-"));
    patterns.put("TITLE", Pattern.compile("TITLE=*"));
//    patterns.put("USEREMAIL", Pattern.compile("TITLE=[0-9A-Za-z\\.]+"));
//    patterns.put("USERNAME", Pattern.compile("TITLE=[0-9A-Za-z\\.]+"));
//    patterns.put("TOLU", Pattern.compile("TITLE=[0-9A-Za-z\\.]+"));
  }
}
