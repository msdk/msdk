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

package io.github.msdk.datamodel.impl;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.msdk.datamodel.rawdata.MsFunction;

/**
 * Object builder
 *
 */
public class MSDKObjectBuilder {

  /**
   * The number of MS functions used in a project is typically small, but each scan has to be
   * annotated with its MS function. So we take advantage of the immutable nature of MsFunction and
   * recycle the instances using this List.
   */
  private static final List<WeakReference<MsFunction>> msFunctions = new LinkedList<>();

  /**
   * Creates a new MsFunction reference.
   *
   * @param name a {@link java.lang.String} object.
   * @param msLevel a {@link java.lang.Integer} object.
   * @return a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
   */
  public static final @Nonnull MsFunction getMsFunction(@Nonnull String name,
      @Nullable Integer msLevel) {

    synchronized (msFunctions) {
      Iterator<WeakReference<MsFunction>> iter = msFunctions.iterator();
      while (iter.hasNext()) {
        WeakReference<MsFunction> ref = iter.next();
        MsFunction func = ref.get();
        if (func == null) {
          iter.remove();
          continue;
        }
        if (!func.getName().equals(name))
          continue;
        if ((func.getMsLevel() == null) && (msLevel == null))
          return func;
        if ((func.getMsLevel() != null) && (func.getMsLevel().equals(msLevel)))
          return func;
      }
      MsFunction newFunc = new SimpleMsFunction(name, msLevel);
      WeakReference<MsFunction> ref = new WeakReference<>(newFunc);
      msFunctions.add(ref);
      return newFunc;

    }
  }

  /**
   * Creates a new MsFunction reference.
   *
   * @param name a {@link java.lang.String} object.
   * @return a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
   */
  public static final @Nonnull MsFunction getMsFunction(@Nonnull String name) {
    return getMsFunction(name, null);
  }

  /**
   * Creates a new MsFunction reference.
   *
   * @param msLevel a {@link java.lang.Integer} object.
   * @return a {@link io.github.msdk.datamodel.rawdata.MsFunction} object.
   */
  public static final @Nonnull MsFunction getMsFunction(@Nullable Integer msLevel) {
    return getMsFunction(MsFunction.DEFAULT_MS_FUNCTION_NAME, msLevel);
  }





}
