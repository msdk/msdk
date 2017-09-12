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

package io.github.msdk.io.chromatof;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ChromaTofParser class.
 * </p>
 */
public class ChromaTofParser {

  private static final Logger log = LoggerFactory.getLogger(ChromaTofParser.class);

  /** Constant <code>FIELD_SEPARATOR_TAB="\t"</code> */
  public static String FIELD_SEPARATOR_TAB = "\t";
  /** Constant <code>FIELD_SEPARATOR_COMMA=","</code> */
  public static String FIELD_SEPARATOR_COMMA = ",";
  /** Constant <code>FIELD_SEPARATOR_SEMICOLON=";"</code> */
  public static String FIELD_SEPARATOR_SEMICOLON = ";";

  /** Constant <code>QUOTATION_CHARACTER_DOUBLETICK="\""</code> */
  public static String QUOTATION_CHARACTER_DOUBLETICK = "\"";
  /** Constant <code>QUOTATION_CHARACTER_NONE=""</code> */
  public static String QUOTATION_CHARACTER_NONE = "";
  /** Constant <code>QUOTATION_CHARACTER_SINGLETICK="\'"</code> */
  public static String QUOTATION_CHARACTER_SINGLETICK = "\'";

  private final String fieldSeparator;
  private final String quotationCharacter;
  private final Locale locale;
  private final ParserUtilities parserUtils = new ParserUtilities();

  /**
   * <p>
   * Constructor for ChromaTofParser.
   * </p>
   *
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param quotationCharacter a {@link java.lang.String} object.
   */
  public ChromaTofParser(@Nonnull String fieldSeparator, @Nonnull String quotationCharacter) {
    this(fieldSeparator, quotationCharacter, Locale.getDefault());
  }

  /**
   * <p>
   * Constructor for ChromaTofParser.
   * </p>
   *
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param quotationCharacter a {@link java.lang.String} object.
   * @param locale a {@link java.util.Locale} object.
   */
  public ChromaTofParser(@Nonnull String fieldSeparator, @Nonnull String quotationCharacter,
      @Nonnull Locale locale) {
    this.fieldSeparator = fieldSeparator;
    this.quotationCharacter = quotationCharacter;
    this.locale = locale;
  }

  /**
   * <p>
   * Getter for the field <code>fieldSeparator</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFieldSeparator() {
    return fieldSeparator;
  }

  /**
   * <p>
   * Getter for the field <code>quotationCharacter</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQuotationCharacter() {
    return quotationCharacter;
  }

  /**
   * <p>
   * Getter for the field <code>locale</code>.
   * </p>
   *
   * @return a {@link java.util.Locale} object.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * <p>
   * Getter for the field <code>parserUtils</code>.
   * </p>
   *
   * @return a {@link io.github.msdk.io.chromatof.ParserUtilities} object.
   */
  public ParserUtilities getParserUtils() {
    return parserUtils;
  }

  public final static class TableColumn {

    public final static TableColumn NIL = new TableColumn("NIL", -1);

    private final String name;
    private final int index;
    private final ColumnName columnName;

    public TableColumn(String name, int index) {
      this.name = name;
      this.index = index;
      this.columnName = ChromaTofParser.ColumnName.fromString(name);
    }

    public String getName() {
      return name;
    }

    public int getIndex() {
      return index;
    }

    public ColumnName getColumnName() {
      return columnName;
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 47 * hash + Objects.hashCode(this.name);
      hash = 47 * hash + this.index;
      hash = 47 * hash + Objects.hashCode(this.columnName);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final TableColumn other = (TableColumn) obj;
      if (!Objects.equals(this.name, other.name)) {
        return false;
      }
      if (this.index != other.index) {
        return false;
      }
      if (this.columnName != other.columnName) {
        return false;
      }
      return true;
    }

  }

  public enum Mode {

    /**
     * The default mode for 1D GC-MS data.
     */
    RT_1D,
    /**
     * The mode for 2D GC-MS data with combined retention times. {@literal 'R.T. (s)'} contains two
     * retention times, separated by a comma.
     */
    RT_2D_FUSED,
    /**
     * The mode for 2D GC-MS data with separate retention times. '1st Dimension Time (s)' and '2nd
     * Dimension Time (s)' contain the two retention times.
     */
    RT_2D_SEPARATE
  }

  public enum ColumnName {

    /**
     * Putative name. Original column name: "Name"
     */
    NAME("Name"),
    /**
     * Retention Time in seconds. Original column name: {@literal "R.T. (s)"}
     */
    RETENTION_TIME_SECONDS("R.T. (s)"),
    /**
     * The first column retention time of a GCxGC peak. Original column name: "1st Dimension Time
     * (s)"
     */
    FIRST_DIMENSION_TIME_SECONDS("1st Dimension Time (s)"),
    /**
     * The second column retention time of a GCxGC peak. Original column name: "2nd Dimension Time
     * (s)"
     */
    SECOND_DIMENSION_TIME_SECONDS("2nd Dimension Time (s)"),
    /**
     * The peak type. Original column name: "Type"
     */
    TYPE("Type"),
    /**
     * The unique mass. Original column name: "UniqueMass"
     */
    UNIQUE_MASS("UniqueMass"),
    /**
     * The masses used for area quantification. Original column name: "Quant Masses"
     */
    QUANT_MASSES("Quant Masses"),
    /**
     * The single mass used for area quantification. Original column name: "Quant Mass"
     */
    QUANT_MASS("Quant Mass"),
    /**
     * The Signal-to-Noise ratio of the quantification masses' signals. Original column name: "Quant
     * S/N"
     */
    QUANT_SN("Quant S/N"),
    /**
     * The concentration of the peak. Original column name: "Concentration"
     */
    CONCENTRATION("Concentration"),
    /**
     * The sample concentration. Original column name: "Sample Concentration"
     */
    SAMPLE_CONCENTRATION("Sample Concentration"),
    /**
     * The match. Original column name: "Match"
     */
    MATCH("Match"),
    /**
     * Then quantified area. Original column name: "Area"
     */
    AREA("Area"),
    /**
     * The putative sum formula. Original column name: "Formula"
     */
    FORMULA("Formula"),
    /**
     * The chemical abstracts service number of the putative identification. Original column name:
     * "CAS"
     */
    CAS("CAS"),
    /**
     * The match similarity (0-999) of the putative identification. Original column name:
     * "Similarity"
     */
    SIMILARITY("Similarity"),
    /**
     * The reverse match similarity (0-999). Original column name: "Reverse"
     */
    REVERSE("Reverse"),
    /**
     * The probability. Original column name: "Probability"
     */
    PROBABILITY("Probability"),
    /**
     * The purity. Original column name: "Purity"
     */
    PURITY("Purity"),
    /**
     * Free form concerns. Original column name: "Concerns"
     */
    CONCERNS("Concerns"),
    /**
     * The Signal-to-Noise ratio of the actual signal. Original column name: "S/N"
     */
    SIGNAL_TO_NOISE("S/N"),
    /**
     * The modified baseline. Original column name: "BaselineModified"
     */
    BASELINE_MODIFIED("BaselineModified"),
    /**
     * The quantification. Original column name: "Quantification"
     */
    QUANTIFICATION("Quantification"),
    /**
     * The full width at half height characterizes the peak elongation. Original column name: "Full
     * Width at Half Height"
     */
    FULL_WIDTH_AT_HALF_HEIGHT("Full Width at Half Height"),
    /**
     * The start of area integration. Original column name: "IntegrationBegin"
     */
    INTEGRATION_BEGIN("IntegrationBegin"),
    /**
     * The end of area integration. Original column name: "IntegrationEnd"
     */
    INTEGRATION_END("IntegrationEnd"),
    /**
     * The name of the first database match. Original column name: "Hit 1 Name"
     */
    HIT_1_NAME("Hit 1 Name"),
    /**
     * The similarity of the first database match. Original column name: "Hit 1 Similarity"
     */
    HIT_1_SIMILARITY("Hit 1 Similarity"),
    /**
     * The reverse similarity of the first database match. Original column name: "Hit 1 Reverse"
     */
    HIT_1_REVERSE("Hit 1 Reverse"),
    /**
     * The probability of the first database match. Original column name: "Hit 1 Probability"
     */
    HIT_1_PROBABILITY("Hit 1 Probability"),
    /**
     * The CAS id of the first database match. Original column name: "Hit 1 CAS"
     */
    HIT_1_CAS("Hit 1 CAS"),
    /**
     * The library name of the database. Original column name: "Hit 1 Library"
     */
    HIT_1_LIBRARY("Hit 1 Library"),
    /**
     * The native id of the first match. Original column name: "Hit 1 Id"
     */
    HIT_1_ID("Hit 1 Id"),
    /**
     * The sum formula of the first match. Original column name: "Hit 1 Formula"
     */
    HIT_1_FORMULA("Hit 1 Formula"),
    /**
     * The molecular weight of the first match. Original column name: "Hit 1 Weight"
     */
    HIT_1_WEIGHT("Hit 1 Weight"),
    /**
     * The contributor of the first match. Original column name: "Hit 1 Contributor"
     */
    HIT_1_CONTRIBUTOR("Hit 1 Contributor"),
    /**
     * The spectrum of the match. Original column name: "Spectra"
     */
    SPECTRA("Spectra"),
    /**
     * An unmapped column.
     */
    UNMAPPED("Unmapped"),
    /**
     * Marker column name for non-existant columns.
     */
    NIL("NIL");

    private final String originalName;

    private ColumnName(String originalName) {
      this.originalName = originalName;
    }

    @Override
    public String toString() {
      return originalName;
    }

    public static ColumnName fromString(String name) {
      switch (name) {
        case "Name":
        case "NAME":
          return NAME;
        case "R.T. (s)":
        case "R.T._(S)":
          return RETENTION_TIME_SECONDS;
        case "1st Dimension Time (s)":
        case "1ST_DIMENSION_TIME_(S)":
          return FIRST_DIMENSION_TIME_SECONDS;
        case "2nd Dimension Time (s)":
        case "2ND_DIMENSION_TIME_(S)":
          return SECOND_DIMENSION_TIME_SECONDS;
        case "Type":
        case "TYPE":
          return TYPE;
        case "UniqueMass":
        case "UNIQUEMASS":
          return UNIQUE_MASS;
        case "Concentration":
        case "CONCENTRATION":
          return CONCENTRATION;
        case "Sample Concentration":
        case "SAMPLE_CONCENTRATION":
          return SAMPLE_CONCENTRATION;
        case "Match":
        case "MATCH":
          return MATCH;
        case "Quant Masses":
        case "QUANT_MASSES":
          return QUANT_MASSES;
        case "Quant S/N":
        case "QUANT_S/N":
          return QUANT_SN;
        case "Quant Mass":
        case "QUANT_MASS":
          return QUANT_MASS;
        case "Area":
        case "AREA":
          return AREA;
        case "Formula":
        case "FORMULA":
          return FORMULA;
        case "Cas":
        case "CAS":
          return CAS;
        case "Similarity":
        case "SIMILARITY":
          return SIMILARITY;
        case "Reverse":
        case "REVERSE":
          return REVERSE;
        case "Probability":
        case "PROBABILITY":
          return PROBABILITY;
        case "Purity":
        case "PURITY":
          return PURITY;
        case "Concerns":
        case "CONCERNS":
          return CONCERNS;
        case "s/n":
        case "S/N":
          return SIGNAL_TO_NOISE;
        case "BaselineModified":
        case "BASELINEMODIFIED":
          return BASELINE_MODIFIED;
        case "Quantification":
        case "QUANTIFICATION":
          return QUANTIFICATION;
        case "Full Width at Half Height":
        case "FULL_WIDTH_AT_HALF_HEIGHT":
          return FULL_WIDTH_AT_HALF_HEIGHT;
        case "IntegrationBegin":
        case "INTEGRATIONBEGIN":
          return INTEGRATION_BEGIN;
        case "IntegrationEnd":
        case "INTEGRATIONEND":
          return INTEGRATION_END;
        case "Hit 1 Name":
        case "HIT_1_NAME":
          return HIT_1_NAME;
        case "Hit 1 Similarity":
        case "HIT_1_SIMILARITY":
          return HIT_1_SIMILARITY;
        case "Hit 1 Reverse":
        case "HIT_1_REVERSE":
          return HIT_1_REVERSE;
        case "Hit 1 Probability":
        case "HIT_1_PROBABILITY":
          return HIT_1_PROBABILITY;
        case "Hit 1 CAS":
        case "HIT_1_CAS":
          return HIT_1_CAS;
        case "Hit 1 Library":
        case "HIT_1_LIBRARY":
          return HIT_1_LIBRARY;
        case "Hit 1 Id":
        case "HIT_1_ID":
          return HIT_1_ID;
        case "Hit 1 Formula":
        case "HIT_1_FORMULA":
          return HIT_1_FORMULA;
        case "Hit 1 Weight":
        case "HIT_1_WEIGHT":
          return HIT_1_WEIGHT;
        case "Hit 1 Contributor":
        case "HIT_1_CONTRIBUTOR":
          return HIT_1_CONTRIBUTOR;
        case "Spectra":
        case "SPECTRA":
          return SPECTRA;
        default:
          log.debug("Unsupported column name '" + name + "'");
          return UNMAPPED;
        // throw new
        // IllegalArgumentException("Unsupported column name '" + name +
        // "'");
      }
    }
  };

  /**
   * <p>
   * parseReport.
   * </p>
   *
   * @param parser a {@link io.github.msdk.io.chromatof.ChromaTofParser} object.
   * @param f a {@link java.io.File} object.
   * @param normalizeColumnNames a boolean.
   * @return a {@link io.github.msdk.io.chromatof.Pair} object.
   */
  public static Pair<LinkedHashSet<ChromaTofParser.TableColumn>, List<TableRow>> parseReport(
      ChromaTofParser parser, File f, boolean normalizeColumnNames) {
    LinkedHashSet<ChromaTofParser.TableColumn> header = parser.parseHeader(f, normalizeColumnNames);
    List<TableRow> table = parser.parseBody(header, f, normalizeColumnNames);
    return new Pair<>(header, table);
  }

  /**
   * <p>
   * parseReport.
   * </p>
   *
   * @param f a {@link java.io.File} object.
   * @param locale a {@link java.util.Locale} object.
   * @return a {@link io.github.msdk.io.chromatof.Pair} object.
   */
  public static Pair<LinkedHashSet<ChromaTofParser.TableColumn>, List<TableRow>> parseReport(File f,
      Locale locale) {
    return parseReport(f, true, locale);
  }

  /**
   * <p>
   * parseReport.
   * </p>
   *
   * @param f a {@link java.io.File} object.
   * @param normalizeColumnNames a boolean.
   * @param locale a {@link java.util.Locale} object.
   * @return a {@link io.github.msdk.io.chromatof.Pair} object.
   */
  public static Pair<LinkedHashSet<ChromaTofParser.TableColumn>, List<TableRow>> parseReport(File f,
      boolean normalizeColumnNames, Locale locale) {
    ChromaTofParser parser = create(f, locale);
    return parseReport(parser, f, normalizeColumnNames);
  }

  /**
   * <p>
   * create.
   * </p>
   *
   * @param f a {@link java.io.File} object.
   * @param normalizeColumnNames a boolean.
   * @param locale a {@link java.util.Locale} object.
   * @return a {@link io.github.msdk.io.chromatof.ChromaTofParser} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static ChromaTofParser create(File f, boolean normalizeColumnNames, Locale locale)
      throws IllegalArgumentException {
    ChromaTofParser parser;
    if (f.getName().toLowerCase().endsWith("csv")) {
      parser = new ChromaTofParser(FIELD_SEPARATOR_COMMA, QUOTATION_CHARACTER_DOUBLETICK, locale);
    } else if (f.getName().toLowerCase().endsWith("tsv")
        || f.getName().toLowerCase().endsWith("txt")) {
      parser = new ChromaTofParser(FIELD_SEPARATOR_TAB, QUOTATION_CHARACTER_NONE, locale);
    } else {
      throw new IllegalArgumentException("Unsupported file extension '" + f.getName().toLowerCase()
          + "'! Supported are '.csv', '.tsv', '.txt'.");
    }
    return parser;
  }

  /**
   * <p>
   * create.
   * </p>
   *
   * @param f a {@link java.io.File} object.
   * @param locale a {@link java.util.Locale} object.
   * @return a {@link io.github.msdk.io.chromatof.ChromaTofParser} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static ChromaTofParser create(File f, Locale locale) throws IllegalArgumentException {
    return create(f, true, locale);
  }

  /**
   * <p>
   * parseDoubleArray.
   * </p>
   *
   * @param fieldName a {@link io.github.msdk.io.chromatof.ChromaTofParser.TableColumn} object.
   * @param row a {@link io.github.msdk.io.chromatof.TableRow} object.
   * @param elementSeparator a {@link java.lang.String} object.
   * @return an array of double.
   */
  public double[] parseDoubleArray(TableColumn fieldName, TableRow row, String elementSeparator) {
    if (row.get(fieldName).contains(elementSeparator)) {
      String[] values = row.get(fieldName).split(elementSeparator);
      double[] v = new double[values.length];
      for (int i = 0; i < v.length; i++) {
        v[i] = parseDouble(values[i]);
      }
      return v;
    }
    return new double[] {parseDouble(row.get(fieldName))};
  }

  /**
   * <p>
   * parseDouble.
   * </p>
   *
   * @param fieldName a {@link io.github.msdk.io.chromatof.ChromaTofParser.TableColumn} object.
   * @param tr a {@link io.github.msdk.io.chromatof.TableRow} object.
   * @return a double.
   */
  public double parseDouble(TableColumn fieldName, TableRow tr) {
    return parseDouble(tr.get(fieldName));
  }

  /**
   * <p>
   * parseDouble.
   * </p>
   *
   * @param s a {@link java.lang.String} object.
   * @return a double.
   */
  public double parseDouble(String s) {
    return ParserUtilities.parseDouble(s, locale);
  }

  /**
   * <p>
   * parseIntegrationStartEnd.
   * </p>
   *
   * @param s a {@link java.lang.String} object.
   * @return a double.
   */
  public double parseIntegrationStartEnd(String s) {
    if (s == null || s.isEmpty()) {
      return Double.NaN;
    }
    if (s.contains(",")) {
      String[] tokens = s.split(",");
      return parseDouble(tokens[0]);
    }
    return parseDouble(s);
  }

  /**
   * <p>
   * getMode.
   * </p>
   *
   * @param body a {@link java.util.List} object.
   * @return a {@link io.github.msdk.io.chromatof.ChromaTofParser.Mode} object.
   */
  public Mode getMode(List<TableRow> body) {
    for (TableRow tr : body) {

      if (tr
          .getColumnForName(ChromaTofParser.ColumnName.RETENTION_TIME_SECONDS) != TableColumn.NIL) {
        // fused RT mode
        String rt = tr.getValueForName(ChromaTofParser.ColumnName.RETENTION_TIME_SECONDS);
        if (rt.contains(",")) {// 2D mode
          return ChromaTofParser.Mode.RT_2D_FUSED;
        } else {
          return ChromaTofParser.Mode.RT_1D;
        }
      } else {
        if (tr.getColumnForName(
            ChromaTofParser.ColumnName.FIRST_DIMENSION_TIME_SECONDS) != TableColumn.NIL
            && tr.getColumnForName(
                ChromaTofParser.ColumnName.SECOND_DIMENSION_TIME_SECONDS) != TableColumn.NIL) {
          return ChromaTofParser.Mode.RT_2D_SEPARATE;
        }
      }
    }
    return ChromaTofParser.Mode.RT_1D;
  }

  /**
   * Parse the header of the given file.
   *
   * @param f the file to parse.
   * @param normalizeColumnNames if true, column names are capitalized and spaces are replaced by
   *        '_'.
   * @return the set of unique column names in order of appearance.
   */
  public LinkedHashSet<ChromaTofParser.TableColumn> parseHeader(File f,
      boolean normalizeColumnNames) {
    LinkedHashSet<ChromaTofParser.TableColumn> globalHeader = new LinkedHashSet<>();
    ArrayList<String> header = null;
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(f));
      String line = "";
      while ((line = br.readLine()) != null) {
        if (!line.isEmpty()) {
          String[] lineArray = splitLine(line, fieldSeparator, quotationCharacter);
          if (header == null) {
            if (normalizeColumnNames) {
              for (int i = 0; i < lineArray.length; i++) {
                lineArray[i] = lineArray[i].trim().toUpperCase().replaceAll(" ", "_");
              }
            }
            header = new ArrayList<>(Arrays.asList(lineArray));
            break;
          }
        }
      }
    } catch (IOException ex) {
      log.warn("Caught an IO Exception while reading file " + f, ex);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException ex) {
        log.warn("Caught an IO Exception while trying to close stream of file " + f, ex);
      }
    }
    int index = 0;
    for (String str : header) {
      try {
        TableColumn tc = new TableColumn(str, index);
        globalHeader.add(tc);
      } catch (IllegalArgumentException iae) {
        log.debug("Unsupported column name '{}'", str);
      }
      index++;
    }
    return globalHeader;
  }

  /**
   * Parse the header of the given file.
   *
   * @param f the file to parse.
   * @param normalizeColumnNames if true, column names are capitalized and spaces are replaced by
   *        '_'.
   * @return the set of unique column names in order of appearance.
   * @deprecated use {@link #parseHeader(java.io.File, boolean)}
   */
  public LinkedHashSet<ChromaTofParser.TableColumn> getHeader(File f,
      boolean normalizeColumnNames) {
    return parseHeader(f, normalizeColumnNames);
  }

  /**
   * <p>
   * splitLine.
   * </p>
   *
   * @param line a {@link java.lang.String} object.
   * @param fieldSeparator a {@link java.lang.String} object.
   * @param quoteSymbol a {@link java.lang.String} object.
   * @return an array of {@link java.lang.String} objects.
   */
  public String[] splitLine(String line, String fieldSeparator, String quoteSymbol) {
    switch (fieldSeparator) {
      case ",":
        Pattern p = Pattern.compile("((\")([^\"]*)(\"))");
        Matcher m = p.matcher(line);
        List<String> results = new LinkedList<>();
        int match = 1;
        while (m.find()) {
          results.add(m.group(3).trim());
        }
        Pattern endPattern = Pattern.compile(",([\"]{0,1}([^\"]*)[^\"]{0,1}$)");
        Matcher m2 = endPattern.matcher(line);
        while (m2.find()) {
          results.add(m2.group(1).trim());
        }
        return results.toArray(new String[results.size()]);
      case "\t":
        return line.replaceAll("\"", "").split("\t");
      default:
        throw new IllegalArgumentException("Field separator " + fieldSeparator
            + " is not supported, only ',' and '\t' are valid!");
    }
  }

  /**
   * <p>
   * parseBody.
   * </p>
   *
   * @param globalHeader a {@link java.util.LinkedHashSet} object.
   * @param f a {@link java.io.File} object.
   * @param normalizeColumnNames a boolean.
   * @return a {@link java.util.List} object.
   */
  public List<TableRow> parseBody(LinkedHashSet<ChromaTofParser.TableColumn> globalHeader, File f,
      boolean normalizeColumnNames) {
    List<TableRow> body = new ArrayList<>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(f));
      String line = "";
      List<ChromaTofParser.TableColumn> header = null;
      while ((line = br.readLine()) != null) {
        if (!line.isEmpty()) {
          ArrayList<String> lineList =
              new ArrayList<>(Arrays.asList(splitLine(line, fieldSeparator, quotationCharacter)));// .split(String.valueOf(FIELD_SEPARATOR))));
          if (header == null) {
            if (normalizeColumnNames) {
              for (int i = 0; i < lineList.size(); i++) {
                lineList.set(i, lineList.get(i).trim().toUpperCase().replaceAll(" ", "_"));
              }
            }
            header = new ArrayList<>();
            int index = 0;
            for (String str : lineList) {
              TableColumn tc = new TableColumn(str, index);
              header.add(tc);
              index++;
            }
          } else {
            TableRow tr = new TableRow();
            for (ChromaTofParser.TableColumn headerColumn : globalHeader) {

              int localIndex = getIndexOfHeaderColumn(header, headerColumn);
              if (localIndex >= 0 && localIndex < lineList.size()) {// found
                                                                    // column
                                                                    // name
                tr.put(headerColumn, lineList.get(localIndex));
              } else {// did not find column name
                log.debug("Could not find index of column '{}'", headerColumn.getColumnName());
                tr.put(headerColumn, null);
              }
            }
            body.add(tr);
          }
        }
      }
    } catch (IOException ex) {
      log.warn("Caught an IO Exception while reading file " + f, ex);
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException ex) {
        log.warn("Caught an IO Exception while trying to close stream of file " + f, ex);
      }
    }
    return body;
  }

  /**
   * <p>
   * getIndexOfHeaderColumn.
   * </p>
   *
   * @param header a {@link java.util.List} object.
   * @param column a {@link io.github.msdk.io.chromatof.ChromaTofParser.TableColumn} object.
   * @return a int.
   */
  public int getIndexOfHeaderColumn(List<ChromaTofParser.TableColumn> header,
      ChromaTofParser.TableColumn column) {
    if (column != TableColumn.NIL) {
      for (ChromaTofParser.TableColumn str : header) {
        if (str.getColumnName() == column.getColumnName()
            && (str.getName().equals(column.getName()))) {
          return str.getIndex();
        }
      }
    }
    return -1;
  }
}
