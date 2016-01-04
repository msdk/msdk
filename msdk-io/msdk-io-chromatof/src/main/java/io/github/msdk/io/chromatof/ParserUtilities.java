/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package io.github.msdk.io.chromatof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic utility methods for csv parsing and numeric conversion.
 *
 */
public class ParserUtilities {
    
    private static final Logger log = LoggerFactory.getLogger(ParserUtilities.class);

    /**
     * Parse a numeric string using the specified locale. When a ParseException
     * is caught, the method returns Double.NaN.
     *
     * @param s the string to parse.
     * @param locale the locale to use for numeric conversion.
     * @return the double value. May be NaN if s is null, empty, or unparseable
     */
    public static double parseDouble(String s, Locale locale) {
        if (s == null || s.isEmpty()) {
            return Double.NaN;
        }
        try {
            return NumberFormat.getNumberInstance(locale).parse(s).doubleValue();
        } catch (ParseException ex) {
            try {
                return NumberFormat.getNumberInstance(Locale.US).parse(s).
                        doubleValue();
            } catch (ParseException ex1) {
                return Double.NaN;
            }
        }
    }
    
    /**
     * Parse a numeric string using the specified locale. When a ParseException
     * is caught, the method returns Float.NaN.
     *
     * @param s the string to parse.
     * @param locale the locale to use for numeric conversion.
     * @return the float value. May be NaN if s is null, empty, or unparseable
     */
    public static float parseFloat(String s, Locale locale) {
        if (s == null || s.isEmpty()) {
            return Float.NaN;
        }
        try {
            return NumberFormat.getNumberInstance(locale).parse(s).floatValue();
        } catch (ParseException ex) {
            try {
                return NumberFormat.getNumberInstance(Locale.US).parse(s).
                        floatValue();
            } catch (ParseException ex1) {
                return Float.NaN;
            }
        }
    }

    public static HashMap<String, String> getFilenameToGroupMap(File f, String fieldSeparator) {
        List<String> header = null;
        HashMap<String, String> filenameToGroupMap = new LinkedHashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = "";
            int lineCount = 0;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] lineArray = line.split(String.valueOf(fieldSeparator));
                    if (lineCount > 0) {
                        //                        System.out.println(
                        //                                "Adding file to group mapping: " + lineArray[0] + " " + lineArray[1]);
                        filenameToGroupMap.put(lineArray[0], lineArray[1]);
                    }
                    lineCount++;
                }
            }
        } catch (IOException ex) {
            log.warn("Caught an IO Exception while reading file " + f, ex);
        }
        return filenameToGroupMap;
    }

    /**
     * Method to convert a mass spectrum in the format contained in ChromaTOF
     * peak files as pairs of mz and intensity, separated by space :
     * {@code 102:956 107:119}.
     *
     * @param massSpectrum the mass spectrum string to parse.
     * @return a tuple of double[] masses and int[] intensities.
     */
    public static Pair<double[], int[]> convertMassSpectrum(
            String massSpectrum) {
        if (massSpectrum == null) {
            log.warn("Warning: mass spectral data was null!");
            return new Pair<>(new double[0], new int[0]);
        }
        String[] mziTuples = massSpectrum.split(" ");
        TreeMap<Float, Integer> tm = new TreeMap<>();
        for (String tuple : mziTuples) {
            if (tuple.contains(":")) {
                String[] tplArray = tuple.split(":");
                tm.put(Float.valueOf(tplArray[0]), Integer.valueOf(tplArray[1]));
            } else {
                log.warn("Warning: encountered strange tuple: {} within ms: {}", new Object[]{tuple, massSpectrum});
            }
        }
        double[] masses = new double[tm.keySet().size()];
        int[] intensities = new int[tm.keySet().size()];
        int i = 0;
        for (Float key : tm.keySet()) {
            masses[i] = key;
            intensities[i] = tm.get(key);
            i++;
        }
        return new Pair<>(masses, intensities);
    }
}
