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

import java.util.LinkedHashMap;

/**
 * <p>
 * TableRow class.
 * </p>
 */
public class TableRow extends LinkedHashMap<ChromaTofParser.TableColumn, String> {

  /**
   * <p>
   * getValueForName.
   * </p>
   *
   * @param columnName a {@link io.github.msdk.io.chromatof.ChromaTofParser.ColumnName} object.
   * @return a {@link java.lang.String} object.
   */
  public String getValueForName(ChromaTofParser.ColumnName columnName) {
    return get(getColumnForName(columnName));
  }

  /**
   * <p>
   * getColumnForName.
   * </p>
   *
   * @param columnName a {@link io.github.msdk.io.chromatof.ChromaTofParser.ColumnName} object.
   * @return a {@link io.github.msdk.io.chromatof.ChromaTofParser.TableColumn} object.
   */
  public ChromaTofParser.TableColumn getColumnForName(ChromaTofParser.ColumnName columnName) {
    if (columnName != ChromaTofParser.ColumnName.UNMAPPED) {
      for (ChromaTofParser.TableColumn tc : keySet()) {
        if (tc.getColumnName() == columnName) {
          return tc;
        }
      }
    }
    return ChromaTofParser.TableColumn.NIL;
  }

}
