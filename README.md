# About MSDK
[![Build Status](https://travis-ci.org/msdk/msdk.svg?branch=master)](https://travis-ci.org/msdk/msdk)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.msdk/msdk-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.msdk/msdk-all)

Mass Spectrometry Development Kit (MSDK) is a Java library of algorithms for processing mass spectrometry data. The goals of the library are to provide a flexible data model with Java interfaces for mass-spectrometry related objects (including raw spectra, processed data sets, identification results etc.) and to integrate the existing algorithms that are currently scattered around various Java-based graphical tools.

## Java version

MSDK requires Java runtime (JRE) version 1.8 or newer.


## Usage

### API

Please see the complete [MSDK API](http://msdk.github.io/api/).

### Maven artifacts

MSDK jars are automatically deployed to Maven Central. In order to use MSDK, simply add the following dependency to your pom.xml:

```
 <dependency>
    <groupId>io.github.msdk</groupId>
    <artifactId>msdk-all</artifactId>
    <version>0.0.12</version>
 </dependency>
```

## Development


### Tutorial

Please read our brief [tutorial](https://msdk.github.io/pull-request-tutorial.html) on how to contribute new code to MSDK.

### Code style

* We use the Google Java Style Guide (https://github.com/google/styleguide)
* You can use the `eclipse-java-google-style.xml` file to automatically format your code in the Eclipse IDE
* Please write JavaDoc comments as full sentences, starting with a capital letter and ending with a period. Brevity is preferred (e.g., "Calculates standard deviation" instead of "This method calculates and returns a standard deviation of given set of numbers").

### Logging

MSDK uses the [SLF4J library](http://www.slf4j.org) for logging. This library can forward all logging calls to your favorite logging framework (java.util.logging, Apache log4j, or others). Please see [SLF4J documentation](http://www.slf4j.org/docs.html) for details.

### Building 

See the [BUILD.md](BUILD.md) file
