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

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.rules.TestWatcher;

/**
 * TestWatcher to set up slf4j / log4j-backed logging.
 *
 * Activate in your tests by adding the following line to your test class:
 * {@code @Rule public SetupLogging setupLogging;}
 */
public class SetupLogging extends TestWatcher {

  private final Properties config;

  /**
   * <p>
   * Constructor for SetupLogging.
   * </p>
   */
  public SetupLogging() {
    this(SetupLogging.class.getResource("/log4j.properties"));
  }

  /**
   * Create new instance with specified properties.
   *
   * @param props the properties
   */
  public SetupLogging(Properties props) {
    this.config = props;
    PropertyConfigurator.configure(config);
  }

  /**
   * Create new instance with specified properties from a URL, e.g. a classpath resource.
   *
   * @param props the properties url
   */
  public SetupLogging(URL props) {
    Properties config = new Properties();
    try {
      config.load(props.openStream());
    } catch (IOException ex) {
      Logger.getLogger(SetupLogging.class.getName()).log(Level.SEVERE, null, ex);
      config.setProperty("log4j.rootLogger", "INFO, A1");
      config.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
      config.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
      config.setProperty("log4j.appender.A1.layout.ConversionPattern",
          "%-4r [%t] %-5p %c %x - %m%n");
    }
    this.config = config;
    PropertyConfigurator.configure(config);
  }

  /**
   * Returns the current logging properties configuration.
   *
   * @return the loggin properties
   */
  public Properties getConfig() {
    return config;
  }

  /**
   * Set the log level (log4j) ( <code>log4j.category.clazz.getName(), level</code>) for a specific
   * class.
   *
   * @param clazz the class to set the level for
   * @param level the log level, one of OFF, ERROR, WARN, INFO, DEBUG
   */
  public void setLogLevel(Class clazz, String level) {
    getConfig().put("log4j.category." + clazz.getName(), level);
    update();
  }

  /**
   * Set the log level (log4j) ( <code>log4j.category.clazz.getName(), level</code>) for a specific
   * class.
   *
   * @param packageOrClass the class or package to set the level for
   * @param level the log level, one of OFF, ERROR, WARN, INFO, DEBUG
   */
  public void setLogLevel(String packageOrClass, String level) {
    if (packageOrClass.startsWith("log4j.category.")) {
      getConfig().put(packageOrClass, level);
    } else {
      getConfig().put("log4j.category." + packageOrClass, level);
    }
    update();
  }

  /**
   * Update the log4j logging backend with the current configuration.
   */
  public void update() {
    PropertyConfigurator.configure(config);
  }
}
