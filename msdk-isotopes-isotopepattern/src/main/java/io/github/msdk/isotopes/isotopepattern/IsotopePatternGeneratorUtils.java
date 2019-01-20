package io.github.msdk.isotopes.isotopepattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;

import com.google.common.base.Strings;

import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.datamodel.SimpleMsSpectrum;
import io.github.msdk.isotopes.isotopepattern.impl.ExtendedIsotopePattern;
import io.github.msdk.util.MsSpectrumUtil;


public class IsotopePatternGeneratorUtils {

  public static final Pattern formulaPattern =
      Pattern.compile("^[\\[\\(]?(([A-Z][a-z]?[0-9]*)+)[\\]\\)]?(([0-9]*)([-+]))?$");
  public static final Pattern unchargedFormulaPattern =
      Pattern.compile("^[\\[\\(]?(([A-Z][a-z]?[0-9]*)+)[\\]\\)]?$");
  public static final Pattern tracerPattern = Pattern.compile("^([0-9]+)([A-Z][a-z]?)$");
  public static final Pattern multiElementPattern = Pattern.compile("([A-Z][a-z]?)([0-9]*)");


  public static String formatCDKString(String cdkString) {
    int startIndex = cdkString.lastIndexOf("MF=");
    int endIndex = cdkString.length() - 1;
    return cdkString.substring(startIndex + 3, endIndex);
  }

  public static void multiTracerQualityCheck(String chemicalFormula, String capacityFormula,
      String tracer1, String tracer2, double tracer1Inc, double tracer2Inc, double tracerAllInc) {
    formulaCheck(chemicalFormula, formulaPattern);
    formulaCheck(capacityFormula, unchargedFormulaPattern);
    formulaCheck(tracer1, tracerPattern);
    formulaCheck(tracer2, tracerPattern);
    ratesCheck(tracer1Inc, tracer2Inc, tracerAllInc);
  }

  private static void ratesCheck(double... rates) {
    double sum = 0.0;
    for (double rate : rates) {
      sum = sum + rate;
    }
    if (sum < 0 || sum > 1) {
      throw new IllegalArgumentException("(Sum of) rate(s) must be in [0,1].");
    }
  }

  private static void formulaCheck(String chemicalFormula, Pattern formulaPattern) {
    Matcher m = formulaPattern.matcher(chemicalFormula);
    if (!m.matches())
      throw new IllegalArgumentException("Invalid chemical formula: " + chemicalFormula);
  }

  public static LinkedHashMap<String, Integer> formulaMap(String formula) {
    LinkedHashMap<String, Integer> elementFormula = new LinkedHashMap<String, Integer>();
    Matcher formulaMatcher = multiElementPattern.matcher(formula);
    ArrayList<String> elementTokens = new ArrayList<String>();
    while (formulaMatcher.find()) {
      elementTokens.add(formulaMatcher.group());
    }
    for (String elementToken : elementTokens) {
      Matcher elementMatcher = multiElementPattern.matcher(elementToken);
      if (elementMatcher.matches()) {
        String element = elementMatcher.group(1);
        Integer quantity = elementMatcher.group(2).equals("") ? Integer.valueOf(1)
            : Integer.valueOf(elementMatcher.group(2));
        elementFormula.put(element, quantity);
      }
    }
    return elementFormula;

  }

  public static String reduceFormula(String formula, String capacity, String tracer1,
      String tracer2) {
    LinkedHashMap<String, Integer> elementFormula = formulaMap(formula);
    LinkedHashMap<String, Integer> capacityFormula = formulaMap(capacity);
    if (!Strings.isNullOrEmpty(tracer1)) {
      String tracer1Element = tracerElement(tracer1);
      if (elementFormula.get(tracer1Element) != null) {
        elementFormula.put(tracer1Element,
            elementFormula.get(tracer1Element) - capacityFormula.get(tracer1Element));

      }
    }
    if (!Strings.isNullOrEmpty(tracer2)) {
      String tracer2Element = tracerElement(tracer2);
      if (elementFormula.get(tracer2Element) != null) {
        elementFormula.put(tracer2Element,
            elementFormula.get(tracer2Element) - capacityFormula.get(tracer2Element));
      }
    }
    return toString(elementFormula);
  }

  public static String toString(LinkedHashMap<String, Integer> formula) {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, Integer> entry : formula.entrySet()) {
      if (entry.getValue() != 0) {
        String number = entry.getValue() > 1 ? String.valueOf(entry.getValue()) : "";
        builder.append(entry.getKey() + number);
      }
    }
    return builder.toString();
  }

  public static MsSpectrum addTracerMass(MsSpectrum spectrum, String capacityFormula,
      String tracer1, String tracer2) throws IOException {
    Double massToAdd = 0.0;
    if (tracer1 != null) {
      massToAdd = massToAdd + totalTracerMass(tracer1, capacityFormula);
    }
    if (tracer2 != null) {
      massToAdd = massToAdd + totalTracerMass(tracer2, capacityFormula);
    }
    return addMass(spectrum, massToAdd);

  }

  private static MsSpectrum addMass(MsSpectrum spectrum, Double massToAdd) {
    double[] mzValues = spectrum.getMzValues();
    for (int i = 0; i < mzValues.length; i++) {
      mzValues[i] = mzValues[i] + massToAdd;
    }
    if (spectrum instanceof ExtendedIsotopePattern) {
      String description = ((ExtendedIsotopePattern) spectrum).getDescription();
      String[] isotopeComposition = ((ExtendedIsotopePattern) spectrum).getIsotopeComposition();
      return new ExtendedIsotopePattern(mzValues, spectrum.getIntensityValues(), mzValues.length,
          spectrum.getSpectrumType(), description, isotopeComposition);
    }
    return new SimpleMsSpectrum(mzValues, spectrum.getIntensityValues(), mzValues.length,
        spectrum.getSpectrumType());
  }

  private static Double totalTracerMass(String tracer, String capacityFormula) throws IOException {
    IsotopeFactory isotopeFactory = Isotopes.getInstance();
    Double tracerMass =
        isotopeFactory.getIsotope(tracerElement(tracer), tracerMassNumber(tracer)).getExactMass();
    Integer factor = formulaMap(capacityFormula).get(tracerElement(tracer));
    return factor * tracerMass;
  }

  public static String tracerElement(String tracer) {
    Matcher tracerMatcher = tracerPattern.matcher(tracer);
    tracerMatcher.matches();
    String tracerElement = tracerMatcher.group(2);
    return tracerElement;
  }

  public static int tracerMassNumber(String tracer) {
    Matcher tracerMatcher = tracerPattern.matcher(tracer);
    tracerMatcher.matches();
    String tracerMassNumber = tracerMatcher.group(1);
    return Integer.parseInt(tracerMassNumber);
  }

  public static MsSpectrum merge(MsSpectrum spectrum1, MsSpectrum spectrum2) {
    double[] mzValues1 = spectrum1.getMzValues();
    float[] intensityValues1 = spectrum1.getIntensityValues();
    double[] mzValues2 = spectrum2.getMzValues();
    float[] intensityValues2 = spectrum2.getIntensityValues();
    LinkedHashMap<Double, Float> datapoints = new LinkedHashMap<Double, Float>();
    for (int i = 0; i < mzValues1.length; i++) {
      if (datapoints.get(mzValues1[i]) != null) {
        datapoints.put(mzValues1[i], datapoints.get(mzValues1[i]) + intensityValues1[i]);
      } else {
        datapoints.put(mzValues1[i], intensityValues1[i]);
      }
    }
    for (int i = 0; i < mzValues2.length; i++) {
      if (datapoints.get(mzValues2[i]) != null) {
        datapoints.put(mzValues2[i], datapoints.get(mzValues2[i]) + intensityValues2[i]);
      } else {
        datapoints.put(mzValues2[i], intensityValues2[i]);
      }
    }
    List<Entry<Double, Float>> datapointList = new ArrayList<>(datapoints.entrySet());
    datapointList.sort(Entry.comparingByKey());
    int size = datapointList.size();
    double[] newMzValues = new double[size];
    float[] newIntensityValues = new float[size];
    for (int i = 0; i < size; i++) {
      newMzValues[i] = datapointList.get(i).getKey();
      newIntensityValues[i] = datapointList.get(i).getValue();
    }
    // TODO: check how to handle the IsotopeCompositions
    return new SimpleMsSpectrum(newMzValues, newIntensityValues, size, spectrum1.getSpectrumType());
  }

  public static MsSpectrum normalize(MsSpectrum spectrum, Float intensityScale) {
    float[] intensityValues = spectrum.getIntensityValues();
    MsSpectrumUtil.normalizeIntensity(intensityValues, intensityValues.length, intensityScale);
    double[] mzValues = spectrum.getMzValues();
    if (spectrum instanceof ExtendedIsotopePattern) {
      String description = ((ExtendedIsotopePattern) spectrum).getDescription();
      String[] isotopeComposition = ((ExtendedIsotopePattern) spectrum).getIsotopeComposition();
      return new ExtendedIsotopePattern(mzValues, intensityValues, mzValues.length,
          spectrum.getSpectrumType(), description, isotopeComposition);
    }
    return new SimpleMsSpectrum(mzValues, intensityValues, mzValues.length,
        spectrum.getSpectrumType());
  }

}
