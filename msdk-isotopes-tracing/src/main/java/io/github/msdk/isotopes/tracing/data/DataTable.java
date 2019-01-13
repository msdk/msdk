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

package io.github.msdk.isotopes.tracing.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import io.github.msdk.isotopes.tracing.util.FileWriterUtils;

/**
 * A DataTable object can be used to store data that shall be written to a csv file. Data will be
 * added column wise. The size of the columns may differ. When writing to csv shorter columns will
 * be filled by an defined (NA) value.
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
@SuppressWarnings("serial")
public class DataTable extends ArrayList<ArrayList<String>> {

  private ArrayList<String> header = new ArrayList<>();
  private int numberOfColumns;

  public DataTable(int numberOfColumns) {
    this.numberOfColumns = numberOfColumns;
  }

  public DataTable(ArrayList<String> header) {
    this.setHeader(header);
  }

  public DataTable(String... headers) {
    this.numberOfColumns = headers.length;
    for (String header : headers) {
      this.header.add(header);
    }
  }

  /**
   * @return the header
   */
  public ArrayList<String> getHeader() {
    return header;
  }

  /**
   * @param header the header to set
   */
  public void setHeader(ArrayList<String> header) {
    this.header = header;
  }

  /**
   * 
   * @return the number of rows without header. This is the maximal size of all columns.
   */
  public int numberOfRows() {
    int max = 0;
    for (ArrayList<String> column : this) {
      max = Math.max(max, column.size());
    }
    return max;
  }

  public void addRow(String... rowValues) {
    int columnCount = 0;
    for (String value : rowValues) {
      if (this.isEmpty()) {
        for (int i = 0; i < numberOfColumns; i++) {
          ArrayList<String> column = new ArrayList<>();
          this.addColumn(column);
        }
      }
      this.get(columnCount).add(value);
      columnCount++;
    }
  }

  /**
   * Writes this table as csv file to the defined path.
   * 
   * @param naValue, the String used for not assigned values
   * @param withHeader
   * @param pathToFile
   * @throws IOException
   */
  @SuppressWarnings("resource")
  public void writeToCsv(String naValue, boolean withHeader, String pathToFile) throws IOException {
    int numberOfColumns = this.size();
    if (header.size() != numberOfColumns) {
      throw new IOException("Header " + header + " do not fit to the number of columns ["
          + numberOfColumns + "] in this Table.");
    }
    pathToFile = FileWriterUtils.checkFilePath(pathToFile, FileWriterUtils.CSV_EXTENSION);
    BufferedWriter writer = Files.newBufferedWriter(Paths.get(pathToFile));
    CSVFormat csvFormat =
        withHeader ? CSVFormat.DEFAULT.withHeader(header.toArray(new String[header.size()]))
            : CSVFormat.DEFAULT;
    CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
    int numberOfRows = numberOfRows();
    for (int row = 0; row < numberOfRows; row++) {
      ArrayList<String> recordValues = new ArrayList<>();
      for (int column = 0; column < numberOfColumns; column++) {
        try {
          recordValues.add(this.get(column).get(row));
        } catch (IndexOutOfBoundsException e) {
          recordValues.add(naValue);
        }
      }
      csvPrinter.printRecord(recordValues);
    }
    csvPrinter.flush();
  }

  /**
   * adds this maps as columns to the right.
   * 
   * @param maps
   */
  @SuppressWarnings("unchecked")
  public <K, T> void addColumns(Map<K, T>... maps) {
    for (Map<K, T> map : maps) {
      addColumn(map);
    }
  }

  /**
   * adds this map as two columns (keyColumn and value column) to the right
   * 
   * @param map
   */
  public <K, T> void addColumn(Map<K, T> map) {
    ArrayList<String> keyColumn = new ArrayList<>();
    ArrayList<String> valueColumn = new ArrayList<>();
    for (Entry<K, T> entry : map.entrySet()) {
      keyColumn.add(String.valueOf(entry.getKey()));
      valueColumn.add(String.valueOf(entry.getValue()));
    }
    this.add(keyColumn);
    this.add(valueColumn);
  }

  public void addColumn(String... columnValues) {
    ArrayList<String> column = new ArrayList<>();
    for (String value : columnValues) {
      column.add(value);
    }
    this.add(column);
  }

  /**
   * inserts this maps at the given column index.
   * 
   * @param columnIndex
   * @param maps
   */
  @SuppressWarnings("unchecked")
  public <K, T> void addColumns(int columnIndex, Map<K, T>... maps) {
    int shift = 0;
    for (Map<K, T> map : maps) {
      addColumn(columnIndex + shift, map);
      shift++;
    }
  }

  /**
   * inserts this map as two columns (keyColumn and value column) at the specified column index
   * 
   * @param columnIndex
   * @param map
   */
  public <K, T> void addColumn(int columnIndex, Map<K, T> map) {
    ArrayList<String> keyColumn = new ArrayList<>();
    ArrayList<String> valueColumn = new ArrayList<>();
    for (Entry<K, T> entry : map.entrySet()) {
      keyColumn.add(String.valueOf(entry.getKey()));
      valueColumn.add(String.valueOf(entry.getValue()));
    }
    this.add(columnIndex, valueColumn);
    this.add(columnIndex, keyColumn);
  }

  /**
   * add this lists as columns to the right
   * 
   * @param <T>
   * @param arrayLists
   */
  @SuppressWarnings("unchecked")
  public <T> void addColumns(ArrayList<T>... arrayLists) {
    for (ArrayList<T> list : arrayLists) {
      addColumn(list);
    }
  }

  /**
   * add this list as column to the right
   * 
   * @param <T>
   * @param list
   */
  public <T> void addColumn(ArrayList<T> list) {
    ArrayList<String> stringList = new ArrayList<>();
    for (T entry : list) {
      stringList.add(String.valueOf(entry));
    }
    this.add(stringList);
  }

  /**
   * add this list as column to the specified index
   * 
   * @param <T>
   * @param list
   */
  public <T> void addColumn(int columnIndex, ArrayList<T> list) {
    ArrayList<String> stringList = new ArrayList<>();
    for (T entry : list) {
      stringList.add(String.valueOf(entry));
    }
    this.add(columnIndex, stringList);
  }

  /**
   * Creates a column with all entries equal to the given value. This method should only be used if
   * the total row number of this table will not change anymore. Otherwise the created column will
   * miss some entries.
   * 
   * @param value
   */
  public <T> void addConstantValueColumn(T value) {
    ArrayList<String> stringList = new ArrayList<>();
    int numberOfRows = numberOfRows();
    for (int row = 0; row < numberOfRows; row++) {
      stringList.add(String.valueOf(value));
    }
    this.add(stringList);
  }

  /**
   * Creates a column with all entries equal to the given value and inserts it at the given
   * colzmnIndex. This method should only be used if the total row number of this table will not
   * change anymore. Otherwise the created column will miss some entries.
   * 
   * @param value
   */
  public <T> void addConstantValueColumn(int columnIndex, T value) {
    ArrayList<String> stringList = new ArrayList<>();
    int numberOfRows = numberOfRows();
    for (int row = 0; row < numberOfRows; row++) {
      stringList.add(String.valueOf(value));
    }
    this.add(columnIndex, stringList);
  }

  public String toString(String naValue, boolean withHeader) {
    StringBuffer tableStringBuffer = new StringBuffer();
    tableStringBuffer.append("\n");
    int numberOfRows = numberOfRows();
    int numberOfColumns = this.size();
    int[] maximalCharacterLengthPerColumn = new int[numberOfColumns];
    for (int index = 0; index < numberOfColumns; index++) {
      int maxCharLength = header.get(index).length();
      for (int row = 0; row < this.get(index).size(); row++) {
        maxCharLength = Math.max(maxCharLength, this.get(index).get(row).length());
      }
      maximalCharacterLengthPerColumn[index] = maxCharLength;
    }
    for (String headline : header) {
      int maxCharLength = maximalCharacterLengthPerColumn[header.indexOf(headline)];
      tableStringBuffer.append(String.format("%" + maxCharLength + "s | ", headline));
    }
    tableStringBuffer.append("\n");
    StringBuffer horizontalLineBuffer = new StringBuffer();
    for (int column = 0; column < numberOfColumns; column++) {
      for (int digit = 0; digit < maximalCharacterLengthPerColumn[column]; digit++) {
        horizontalLineBuffer.append("_");
      }
      horizontalLineBuffer.append("_|_");
    }
    tableStringBuffer.append(horizontalLineBuffer.toString());
    tableStringBuffer.append("\n");
    for (int row = 0; row < numberOfRows; row++) {
      for (int column = 0; column < numberOfColumns; column++) {
        int maxCharLength = maximalCharacterLengthPerColumn[column];
        try {
          tableStringBuffer
              .append(String.format("%" + maxCharLength + "s | ", this.get(column).get(row)));
        } catch (IndexOutOfBoundsException e) {
          tableStringBuffer.append(String.format("%" + maxCharLength + "s | ", naValue));
        }
      }
      tableStringBuffer.append("\n");
    }
    return tableStringBuffer.toString();
  }
}
