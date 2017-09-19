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

package io.github.msdk.featuredetection.openms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.msdk.MSDKException;

/**
 * <p>FeatureFinderMetaboLocator class.</p>
 */
public class FeatureFinderMetaboLocator {

  private final static String OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME = "FeatureFinderMetabo";
  private final static String OPENMS_DEFAULT_LOCATION_WINDOWS = "C:\\Program Files\\";
  private final static String OPENMS_DEFAULT_LOCATION_MAC = "/Applications/";
  private final static String OPENMS_DEFAULT_LOCATION_LINUX = "/usr/local/";
  private final static String FEATURE_FINDER_METABO_EVAL = "No options given. Aborting!";

  /**
   * Check for FeatureFinderMetabo in PATH &amp; default Installation directory on various platforms.
   *
   * @return <i>FeatureFinderMetabo</i> if found in path.
   *         <p>
   *         The full location to FeatureFinderMetabo if not found in path.
   *         <p>
   *         <i>null</i> if not found anywhere.
   * @throws io.github.msdk.MSDKException if any.
   */
  public static String findFeatureFinderMetabo() throws MSDKException {
    if (isFeatureFinderMetaboHere(OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME)) {
      return OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME;
    }

    final String OS = System.getProperty("os.name").toLowerCase();
    if (OS.indexOf("win") >= 0) {
      for (String s : new File(OPENMS_DEFAULT_LOCATION_WINDOWS).list()) {
        if (s.toLowerCase().contains("openms")) {
          String path = "\"" + OPENMS_DEFAULT_LOCATION_WINDOWS + s + "\\bin\\"
              + OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + "\"";
          if (isFeatureFinderMetaboHere(path))
            return path;
        }
      }
    } else if (OS.indexOf("mac") >= 0) {
      for (String s : new File(OPENMS_DEFAULT_LOCATION_MAC).list()) {
        if (s.toLowerCase().contains("openms")) {
          String path = "\"" + OPENMS_DEFAULT_LOCATION_MAC + s + "\\bin\\"
              + OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + "\"";
          if (isFeatureFinderMetaboHere(path))
            return path;
        }
      }
    } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
      for (String s : new File(OPENMS_DEFAULT_LOCATION_LINUX).list()) {
        if (s.toLowerCase().contains("openms")) {
          String path = "\"" + OPENMS_DEFAULT_LOCATION_LINUX + s + "\\bin\\"
              + OPENMS_FEATURE_FINDER_METABO_LIBRARY_NAME + "\"";
          if (isFeatureFinderMetaboHere(path))
            return path;
        }

      }
    }

    return null;
  }

  /**
   * FeatureFinderMetabo is tried to be executed from the given path.
   * 
   * @param path Location to check for FeatureFinderMetabo
   * 
   * @return true if the library was found in the given path
   */
  private static boolean isFeatureFinderMetaboHere(String path) throws MSDKException {
    /*
     * FEATURE_FINDER_METABO_EVAL is the text the binary returns when no parameters are passed
     */
    return execShellCommand(path).contains(FEATURE_FINDER_METABO_EVAL);
  }

  /**
   * Executes the specified string command in a separate process.
   * 
   * @param cmd The command to be executed
   * 
   * @return The output obtained after executing the passed cmd
   */
  private static String execShellCommand(String cmd) {
    String out = "";
    try {
      Process cmdProc = Runtime.getRuntime().exec(cmd);
      BufferedReader stdoutReader =
          new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
      String line;
      while ((line = stdoutReader.readLine()) != null) {
        out += line + "\n";
      }
    } catch (IOException ie) {
    }
    return out;
  }

}
