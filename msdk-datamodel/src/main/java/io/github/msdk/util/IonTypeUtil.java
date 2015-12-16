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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import io.github.msdk.MSDKRuntimeException;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.rawdata.PolarityType;

/**
 * Text processing utilities
 */
public class IonTypeUtil {

    private static final Pattern ionTypePattern = Pattern
            .compile("\\[(\\d*)M([+-]?.*)\\](\\d*)([+-])");

    /**
     * Creates an IonType from a string. The expected string format is [M+2H]2+.
     *
     * @param text
     *            a {@link java.lang.String} object.
     * @return a {@link io.github.msdk.datamodel.ionannotations.IonType} object.
     */
    public static @Nonnull IonType createIonType(final @Nonnull String adduct) {
        // Expected string format: [M+2H]2+

        Matcher m = ionTypePattern.matcher(adduct);

        if (!m.matches())
            throw new MSDKRuntimeException("Cannot parse ion type " + adduct);

        final String numOfMoleculesGroup = m.group(1);
        final String adductFormulaGroup = m.group(2);
        final String chargeGroup = m.group(3);
        final String polarityGroup = m.group(4);

        try {

            // Polarity type
            PolarityType polarity;
            switch (polarityGroup) {
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
            Integer numberOfMolecules = 1;
            if (!Strings.isNullOrEmpty(numOfMoleculesGroup))
                numberOfMolecules = Integer.parseInt(numOfMoleculesGroup);

            // Charge
            Integer charge = 1;
            if (!Strings.isNullOrEmpty(chargeGroup))
                charge = Integer.parseInt(chargeGroup);

            // Create ionType
            IonType ionType = MSDKObjectBuilder.getIonType(adduct, polarity,
                    numberOfMolecules, adductFormulaGroup, charge);

            return ionType;

        } catch (Exception e) {
            throw new MSDKRuntimeException("Cannot parse ion type " + adduct);
        }

    }

}
