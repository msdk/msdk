# About MSDK

Mass Spectrometry Development Kit (MSDK) is a Java library of algorithms for processing mass spectrometry data. The goals of the library are to provide a flexible data model with Java interfaces for mass-spectrometry related objects (including raw spectra, processed data sets, identification results etc.) and to integrate the existing algorithms that are currently scattered around various Java-based graphical tools.

## Java version

MSDK requires Java runtime (JRE) version 1.7 or newer.

## Code style

Since this is a collaborative project, please adhere to the following code formatting conventions:
* All Java sources should be formatted according to the official [Java Code Conventions](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html) with only one adjustment: use spaces for indentation instead of tabs
* You can use the msdk-eclipse-code-formater.xml file to automatically format your code in the Eclipse IDE
* Please write JavaDoc comments as full sentences, starting with a capital letter and ending with a period. Brevity is preferred (e.g., "Calculates standard deviation" is preferred over "This method calculates and returns a standard deviation of given set of numbers").

## Building 

[Apache Maven](http://maven.apache.org) are required to build MSDK. To compile the source codes, run the following command in the source code directory:

```
$ mvn package
```
