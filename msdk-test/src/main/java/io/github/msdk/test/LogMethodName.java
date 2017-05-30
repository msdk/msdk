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

package io.github.msdk.test;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * TestWatcher that outputs a logging message when a test method is invoked.
 *
 * Activate in your tests by adding the following line to your test class:
 * {@code @Rule public LogMethodName logMethodName;}
 */
public class LogMethodName extends TestWatcher {

  /** {@inheritDoc} */
  @Override
  public void starting(Description method) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 80; i++) {
      sb.append("#");
    }
    sb.append("\n").append("# ").append(method.getMethodName()).append("\n");
    for (int i = 0; i < 80; i++) {
      sb.append("#");
    }
    System.out.println(sb.toString());
  }

}
