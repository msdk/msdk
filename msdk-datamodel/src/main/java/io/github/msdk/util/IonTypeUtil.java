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

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.PolarityType;

/**
 * Text processing utilities
 */
public class IonTypeUtil {

    /**
     * Creates an IonType from a string. The expected string format is [M+2H]2+.
     *
     * @param text
     *            a {@link java.lang.String} object.
     * @return a {@link io.github.msdk.datamodel.ionannotations.IonType} object.
     */
    public static IonType createIonType(String adduct) {
        // Expected string format: [M+2H]2+

        // Polarity type
        PolarityType polarity;
        String lastChar = adduct.substring(adduct.length() - 1);
        switch (lastChar) {
        case "+":
            polarity = PolarityType.POSITIVE;
            break;
        case "-":
            polarity = PolarityType.NEGATIVE;
            break;
        default:
            polarity = PolarityType.UNKNOWN;
        }

        // Number of molecules
        Integer numberOfMolecules = null;
        String secondChar = adduct.substring(1, 2);
        try {
            numberOfMolecules = Integer.parseInt(secondChar);
        } catch (Exception e) {
            numberOfMolecules = 1;
        }

        // Adduct formula
        String adductFormula = null;
        /*
         * TODO: Extract e.g. NH4 from [M+NH4]+
         */
        adductFormula = "[M+H]";

        // Charge
        Integer charge = null;
        String secondLastChar = adduct.substring(adduct.length() - 2,
                adduct.length() - 1);
        try {
            charge = Integer.parseInt(secondLastChar);
        } catch (Exception e) {
        }

        // Create ionType
        IonType ionType = MSDKObjectBuilder.getIonType(adduct, polarity,
                numberOfMolecules, adductFormula, charge);

        return ionType;

    }

}
