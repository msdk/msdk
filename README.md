# About MSDK

Mass Spectrometry Development Kit (MSDK) is a Java library of algorithms for processing mass spectrometry data. The goals of the library are to provide a flexible data model with Java interfaces for mass-spectrometry related objects (including raw spectra, processed data sets, identification results etc.) and to integrate the existing algorithms that are currently scattered around various Java-based graphical tools.

## Java version

MSDK requires Java runtime (JRE) version 1.7 or newer.

## Development


### Tutorial

Please read our brief [tutorial](https://msdk.github.io/pull-request-tutorial.html) on how to contribute new code to MSDK.

### Code style

Since this is a collaborative project, please adhere to the following code formatting conventions:
* All Java sources should be formatted according to the official [Java Code Conventions](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html) with only one adjustment: use spaces for indentation instead of tabs
* You can use the `msdk-eclipse-code-formater.xml` file to automatically format your code in the Eclipse IDE
* Please write JavaDoc comments as full sentences, starting with a capital letter and ending with a period. Brevity is preferred (e.g., "Calculates standard deviation" is preferred over "This method calculates and returns a standard deviation of given set of numbers").

### Logging

MSDK uses the [SLF4J library](http://www.slf4j.org) for logging. This library can forward all logging calls to your favorite logging framework (java.util.logging, Apache log4j, or others). Please see [SLF4J documentation](http://www.slf4j.org/docs.html) for details.

### Building 

[Apache Maven](http://maven.apache.org) is required to build MSDK. To compile the source codes, run the following command in the source code directory:

```
$ mvn package
```
