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

package io.github.msdk.isotopes.tracing.util;

/**
 * 
 * @author Susanne Fürst, susannefuerst@freenet.de, susanne.fuerst@mdc-berlin.de
 *
 */
public class StringUtils {

  private static final String ON = "1";
  private static final String OFF = "0";
  private static final String ON_OFF = "10";

  /**
   * 
   * @param binary, a string representation of sequence of bits, e.g "10011"
   * @return a string representation of the input binary, where the last off bit is switched witch
   *         the following bit.
   */
  public static String switchLastOffBit(String binary) {
    int lastOffIndex = binary.lastIndexOf(OFF);
    // String firstPart = binary.substring(0, lastOffIndex);
    // String lastPart = binary.substring(lastOffIndex + 2);
    // String switched = firstPart + ON_OFF + lastPart;
    // return switched;
    return binary.substring(0, lastOffIndex) + ON_OFF + binary.substring(lastOffIndex + 2);
  }

  /**
   * 
   * @param binary, a string representation of sequence of bits, e.g "10011"
   * @return a string representation of the input binary, where the off bit next to the last off bit
   *         is switched witch the following bit and all following ons are shifted to the right.
   */
  public static String switchNextToLastOffBitAndShiftRemainingOnsToTheRight(String binary) {
    int lastOnIndex = binary.lastIndexOf(ON);
    String substringBeforeLastOn = binary.substring(0, lastOnIndex);
    int lastOffBeforeLastOn = substringBeforeLastOn.lastIndexOf(OFF);
    // String firstPart = binary.substring(0, lastOffBeforeLastOn);
    // String lastPart = binary.substring(lastOffBeforeLastOn + 2);
    // String switchedAnShifted = firstPart + ON_OFF + shiftAllOnesToRight(lastPart);
    // return switchedAnShifted;
    return binary.substring(0, lastOffBeforeLastOn) + ON_OFF
        + shiftAllOnesToRight(binary.substring(lastOffBeforeLastOn + 2));
  }

  /**
   * 
   * @param binary, a sequence of leading ons, followed by only offs
   * @return The input binary where all leading ons are shifted to the right end.
   */
  private static String shiftAllOnesToRight(String binary) {
    int lastOnIndex = binary.lastIndexOf(ON);
    // String offs = binary.substring(lastOnIndex + 1);
    // String ons = binary.substring(0, lastOnIndex + 1);
    // return offs + ons;
    return binary.substring(lastOnIndex + 1) + binary.substring(0, lastOnIndex + 1);
  }

  /**
   * Returns the input string with all numbers converted to superscript.
   * 
   * @param string
   * @return The input string with all numbers converted to superscript.
   */
  public static String superscript(String string) {
    string = string.replaceAll("0", "⁰");
    string = string.replaceAll("1", "¹");
    string = string.replaceAll("2", "²");
    string = string.replaceAll("3", "³");
    string = string.replaceAll("4", "⁴");
    string = string.replaceAll("5", "⁵");
    string = string.replaceAll("6", "⁶");
    string = string.replaceAll("7", "⁷");
    string = string.replaceAll("8", "⁸");
    string = string.replaceAll("9", "⁹");
    return string;
  }

  /**
   * Returns the input string with all numbers converted to subscript.
   * 
   * @param string
   * @return The input string with all numbers converted to subscript.
   */
  public static String subscript(String str) {
    str = str.replaceAll("0", "₀");
    str = str.replaceAll("1", "₁");
    str = str.replaceAll("2", "₂");
    str = str.replaceAll("3", "₃");
    str = str.replaceAll("4", "₄");
    str = str.replaceAll("5", "₅");
    str = str.replaceAll("6", "₆");
    str = str.replaceAll("7", "₇");
    str = str.replaceAll("8", "₈");
    str = str.replaceAll("9", "₉");
    return str;
  }

}
