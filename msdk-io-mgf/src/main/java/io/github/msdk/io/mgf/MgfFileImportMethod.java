/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.io.mgf;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.MsSpectrumType;
import io.github.msdk.util.ArrayUtil;
import io.github.msdk.util.DataPointSorter;
import io.github.msdk.util.DataPointSorter.SortingDirection;
import io.github.msdk.util.DataPointSorter.SortingProperty;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MgfFileImportMethod implements MSDKMethod<List<MgfMsSpectrum>> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private List<MgfMsSpectrum> spectra;
  private final @Nonnull File target;
  private boolean canceled = false;
  private long processedSpectra = 0;
  private Hashtable<String, Pattern> patterns;

  @Nullable
  @Override
  public List<MgfMsSpectrum> execute() throws MSDKException {
    logger.info("Started MGF import from {} file", target);

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
      throw new MSDKException(e);
    }

    return spectra;
  }

  private MgfMsSpectrum processSpectrum(BufferedReader reader) throws IOException {
    String title = "";
    int precursorCharge = 0;
    double precursorMass = 0;
    double mz[] = new double[16];
    float intensity[] = new float[16];


    int index = 0;
    String line;
    String groupped;
    while (true) {
      line = reader.readLine();
      if (line == null || line.equals("END IONS"))
        break;

      if (line.contains("PEPMASS")) {
        groupped = patterns.get("PEPMASS").matcher(line).group();
        precursorMass = Double.parseDouble(groupped);
      } else if (line.contains("TITLE")) {
        groupped = patterns.get("TITLE").matcher(line).group();
        title = groupped;
      } else if (line.contains("CHARGE")) {
        groupped =  patterns.get("CHARGE").matcher(line).group();
        String trimmed = groupped.substring(0, groupped.length() - 1);
        precursorCharge = Integer.parseInt(trimmed);
        if (groupped.charAt(groupped.length() - 1) == '-') {
          precursorCharge *= -1;
        }
      } else {
        String[] floats = line.split(" ");
        double mzValue = Double.parseDouble(floats[0]);
        float intensityValue = Float.parseFloat(floats[1]);
        mz = ArrayUtil.addToArray(mz, mzValue, index);
        intensity = ArrayUtil.addToArray(intensity, intensityValue, index);
        index++;
      }
    }

    /*
     Do not like this code
     May be implement a Builder pattern for this?
    */

    DataPointSorter.sortDataPoints(mz, intensity, index - 1, SortingProperty.MZ, SortingDirection.ASCENDING);
    MgfMsSpectrum spectrum = new MgfMsSpectrum(mz, intensity, index - 1, title, precursorCharge,
        precursorMass, MsSpectrumType.CENTROIDED);

    return spectrum;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Float getFinishedPercentage() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public List<MgfMsSpectrum> getResult() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
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
  }
}
