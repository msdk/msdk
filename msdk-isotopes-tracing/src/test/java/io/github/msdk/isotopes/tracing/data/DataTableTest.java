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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.github.msdk.isotopes.tracing.data.constants.PathConstants;
import junit.framework.TestCase;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class DataTableTest extends TestCase {

  public void testWriteToCsv() throws IOException {
    ArrayList<String> headers = new ArrayList<String>();
    for (int i = 0; i < 5; i++) {
      String header = "Test" + i;
      headers.add(header);
    }
    DataTable dataTable = new DataTable(headers);
    ArrayList<String> colWithTwoRows = new ArrayList<>();
    colWithTwoRows.add("2 rows");
    colWithTwoRows.add("2 rows");
    dataTable.addColumn(colWithTwoRows);
    LinkedHashMap<String, Double> colsWith7Rows = new LinkedHashMap<>();
    for (int i = 0; i < 7; i++) {
      String key = "Row " + i;
      colsWith7Rows.put(key, 1.2);
    }
    dataTable.addColumn(colsWith7Rows);
    ArrayList<String> insertedCol = new ArrayList<>();
    insertedCol.add("This was inserted at index 0");
    dataTable.addColumn(0, insertedCol);
    dataTable.addConstantValueColumn("This was created from a const");

    File tmpFolder = new File(PathConstants.TMP_FOLDER.toAbsolutePath());
    if (!tmpFolder.exists()) {
      tmpFolder.mkdir();
    }
    dataTable.writeToCsv("NA", true, PathConstants.TMP_FOLDER.toAbsolutePath("testfile.csv"));
    File[] created = new File(PathConstants.TMP_FOLDER.toAbsolutePath()).listFiles();
    for (File file : created) {
      file.delete();
    }
  }
}
