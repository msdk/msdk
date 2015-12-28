/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package io.github.msdk.util;

import java.util.regex.Pattern;

/**
 * Text processing utilities
 */
public class TextUtils {

    /**
     * Generates a regular expression from a string that contains asterisks (*)
     * as wild cards. Basically, it replaces all * with .*
     *
     * @param text
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String createRegexFromWildcards(String text) {
        final StringBuilder regex = new StringBuilder("^");
        String sections[] = text.split("\\*", -1);
        for (int i = 0; i < sections.length; i++) {
            if (i > 0)
                regex.append(".*");
            regex.append(Pattern.quote(sections[i]));
        }
        regex.append("$");
        return regex.toString();
    }

}
