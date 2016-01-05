/* 
 * (C) Copyright 2015-2016 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package io.github.msdk.io.chromatof;

import java.util.LinkedHashMap;

/**
 * <p>
 * TableRow class.
 * </p>
 *
 */
public class TableRow extends
        LinkedHashMap<ChromaTofParser.TableColumn, String> {

    private ChromaTofParser.ColumnName getColumnName(
            ChromaTofParser.TableColumn tableColumn) {
        if (containsKey(tableColumn)) {
            return tableColumn.getColumnName();
        }
        return ChromaTofParser.ColumnName.NIL;
    }

    public String getValueForName(ChromaTofParser.ColumnName columnName) {
        return get(getColumnForName(columnName));
    }

    public ChromaTofParser.TableColumn getColumnForName(
            ChromaTofParser.ColumnName columnName) {
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
