package io.github.msdk.io.mgf;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.util.ArrayUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
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

    String title;
    int precursorCharge;
    double precursorMass;
    Matcher matcher;
    double mz[] = new double[16];
    float intensive[] = new float[16];
    int index = 0;

    while (!(line = reader.readLine()).equals("END IONS")) {
      if (line.contains("PEPMASS")) {
        matcher = patterns.get("PEPMASS").matcher(line);
        if (matcher.find()) {
          String d = matcher.group();
          System.out.println(d);
          precursorMass = Double.parseDouble(d);
        }
      } else if (line.contains("TITLE")) {
        matcher = patterns.get("TITLE").matcher(line);
        if (matcher.find()) {
          title = matcher.group();
        }
      } else if (line.contains("CHARGE")) {
        matcher = patterns.get("CHARGE").matcher(line);
        if (matcher.find()) {
          String d = matcher.group();
          System.out.println(d);
          String trimmed = d.substring(0, d.length() - 1);
          precursorCharge = Integer.parseInt(trimmed);
          if (d.charAt(d.length() - 1) == '-') {
            precursorCharge *= -1;
          }
        }
      } else {
        String[] floats = line.split(" ");
        mz = ArrayUtil.addToArray(mz, Double.parseDouble(floats[0]), index);
        intensive = ArrayUtil.addToArray(intensive, Float.parseFloat(floats[1]), index);
        index++;
      }
    }
//    MsSpectrum spectrum = new SimpleMsSpectrum();
//    MsScan ms = (MsScan) spectrum;
//    ms.
//    spectrum.

//    Do not know the difference between types
    return new SimpleMsSpectrum(mz, intensive, index - 1, MsSpectrumType.PROFILE);
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
    spectra = new LinkedList<>();
    patterns = new Hashtable<>();
    patterns.put("PEPMASS", Pattern.compile("(?<=PEPMASS=)(\\d+\\.\\d+)"));
    patterns.put("CHARGE", Pattern.compile("(?<=CHARGE=)(\\d+)\\+|-"));
    patterns.put("TITLE", Pattern.compile("(?<=TITLE=).*"));
//    patterns.put("USEREMAIL", Pattern.compile("TITLE=[0-9A-Za-z\\.]+"));
//    patterns.put("USERNAME", Pattern.compile("TITLE=[0-9A-Za-z\\.]+"));
//    patterns.put("TOLU", Pattern.compile("TITLE=[0-9A-Za-z\\.]+"));
  }
}
