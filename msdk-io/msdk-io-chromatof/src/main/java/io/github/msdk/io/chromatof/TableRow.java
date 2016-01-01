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

import java.util.LinkedHashMap;

/**
 * <p>
 * TableRow class.</p>
 *
 * @author Nils Hoffmann
 *
 */
public class TableRow extends LinkedHashMap<ChromaTofParser.TableColumn, String> {

    private ChromaTofParser.ColumnName getColumnName(ChromaTofParser.TableColumn tableColumn) {
        if (containsKey(tableColumn)) {
            return tableColumn.getColumnName();
        }
        return ChromaTofParser.ColumnName.NIL;
    }
    
    public String getValueForName(ChromaTofParser.ColumnName columnName) {
        return get(getColumnForName(columnName));
    }

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
