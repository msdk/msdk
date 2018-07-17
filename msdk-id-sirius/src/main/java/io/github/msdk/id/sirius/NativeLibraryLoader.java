/*
 * (C) Copyright 2015-2018 by MSDK Development Team
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

package io.github.msdk.id.sirius;

import com.google.common.io.Files;
import io.github.msdk.MSDKException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p> Class NativeLibraryLoader </p> This class allows to dynamically load native libraries from
 * .jar files (also works with IDE) with updating java.library.path variable
 */
public class NativeLibraryLoader {
  private static final Logger logger = LoggerFactory.getLogger(NativeLibraryLoader.class);
  private NativeLibraryLoader() {}

  /**
   * Returns path to requested resource
   * @param resource - file to find Path to
   * @return Path
   * @throws MSDKException if any
   */
  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = NativeLibraryLoader.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  /**
   * Public method for external usage, copies all files from `folder`
   * <p>Loads libraries from `folder` in order specified by `libs` array</p>
   *
   * The folder structure is strict
   * folder
   * -windows64
   * --lib1
   * --lib2
   * -windows32
   * -- lib
   * -linux64
   * -- lib1 ...
   * -linux32"
   * -- lib1 ...
   * -mac64
   * --lib1 ...
   *
   * @param folder - specify the name of the library to be loaded (example - glpk_4_60)
   * @param libs - array of exact names of libraries (without extensions)
   * @throws MSDKException if any
   * @throws IOException if any
   */
  public static void loadLibraryFromJar(String folder, String[] libs)
      throws MSDKException, IOException {
    logger.info("Started loading libraries from {} folder", folder);

    String arch = getArch();
    String osname = getOsName();
    String subfolder = getSubfolder(arch, osname);

    logger.info("OS type = {} and OS arch = {}", subfolder, arch);

    // Make new java.library.path
    File temporaryDir = createLibraryPath();

    folder += "/" + subfolder + arch + "/";

    final File jarFile = new File(NativeLibraryLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    Map<String, InputStream> libraries;

    if(jarFile.isFile()) {  // Run with JAR file
      logger.info("Loading libraries from JAR");
      final JarFile jar = new JarFile(jarFile);
      libraries = getJarStreams(jar, folder);
      moveLibraries(libraries, temporaryDir);
      jar.close();
    } else { // Run with IDE
      logger.info("Loading libraries from IDE level");
      String realPath = getResourcePath(folder).toString();
      libraries = getIdeStreams(realPath);
      moveLibraries(libraries, temporaryDir);
    }

    loadLibraries(libs);
  }

  /**
   * <p>Method for updating java.library.path</p>
   * Method for updating the java.library.path with a new temp folder for native libraries
   * System.setProperty(path) does not make any sense because JVM sets it during initialization
   * Altering library path requires additional code
   *
   * @return temporary folder file
   * @throws MSDKException if any
   */
  private static File createLibraryPath() throws MSDKException {
    File tempDir = Files.createTempDir();
    tempDir.deleteOnExit();

    String absPath = tempDir.getAbsolutePath();

    try {
      addLibraryPath(absPath);
      logger.debug("Successfully added temp folder to java.library.path [{}]", absPath);
    } catch (Exception e) {
      throw new MSDKException(e);  //Catches NoSuchFieldException & IllegalAccessException
    }

    return tempDir;
  }

  /**
   * <p>Adds the specified path to the java library path w/o reinitialization of a variable</p>
   *
   * @param pathToAdd the path to add
   * @throws Exception if any
   */
  private static void addLibraryPath(String pathToAdd)
      throws NoSuchFieldException, IllegalAccessException {
    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);

    //get array of paths
    final String[] paths = (String[]) usrPathsField.get(null);

    //check if the path to add is already present
    for (String path : paths) {
      if (path.equals(pathToAdd)) {
        return;
      }
    }

    //add the new path
    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length - 1] = pathToAdd;
    usrPathsField.set(null, newPaths);
  }

  /**
   * * <p>Method moves library from .jar to temporary folder and loads it</p>
   * Temp folder previously should be added to java.library.path
   *
   * @param fileStreams - map of <filename, inputStream> pairs, used for interoperability between loading from JAR and loading from IDE
   * @param tempDirectory - temp directory for libraries
   * @throws FileNotFoundException - in case of troubleshoot with temporary folder.
   * @throws IOException - in case of incorrect InputStreams or inproper `temp` file created
   */
  private static void moveLibraries(Map<String, InputStream> fileStreams, File tempDirectory) throws FileNotFoundException, IOException {
    if (tempDirectory == null || !tempDirectory.exists())
      throw new FileNotFoundException("Temporary directory was not created");

    OutputStream outputStream;
    byte buffer[] = new byte[4096];
    int bytes;

    for (Map.Entry<String, InputStream> pair: fileStreams.entrySet()) {
      logger.debug("Started copying {}", pair.getKey());
      File temp = new File(tempDirectory, pair.getKey());
      final InputStream iStream = pair.getValue();
      outputStream = new FileOutputStream(temp);

      while ((bytes = iStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, bytes);
      }
      iStream.close();
      outputStream.close();

      logger.debug("Finished copying {}", pair.getKey());
    }
  }

  /**
   * <p> Method for calling OS to load native libraries stored in java.library.path </p>
   * @param libraryNames
   */
  private static void loadLibraries(String[] libraryNames) {
    for (String lib: libraryNames) {
      System.loadLibrary(lib);
      logger.info("Successfully loaded {} library", lib);
    }
  }

  /**
   * <p>Method returns proper subfolder of a native library</p>
   * @param arch - architecture of the computer
   * @param osname - name of the OS (formatted)
   * @return appropriate folder with native libraries
   * @throws MSDKException - if system parameters were not determined
   */
  private static String getSubfolder(String arch, String osname) throws MSDKException {
    String subfolder;
    if (osname.contains("linux")) {
      subfolder = "linux";
    } else if (osname.contains("mac")) {
      if (arch.equals("32"))
        throw new MSDKException("There are no native libraries for x32 mac systems");
      subfolder = "mac";
    } else if (osname.contains("windows")) {
      subfolder = "windows";
    } else
      throw new MSDKException("Could not determine the system parameters properly");

    return subfolder;
  }

  /**
   * <p>Method returns architecture of the computer</p>
   * @return formatted architecture
   * @throws MSDKException - if any
   */
  private static String getArch() throws  MSDKException {
    String arch = System.getProperty("os.arch");
    if (arch == null)
      throw new MSDKException("Can not identify os.arch property");

    return arch.toLowerCase().endsWith("64") ? "64" : "32";
  }

  /**
   * <p>Method returns formatted OS name</p>
   * @return OS name
   * @throws MSDKException - if any
   */
  public static String getOsName() throws MSDKException {
    String osname = System.getProperty("os.name");
    if (osname == null)
      throw new MSDKException("Can not identify os.name property");

    return osname.toLowerCase();
  }

  /**
   * <p>Method loads files inside jar from specified folder</p>
   * @param jar\ - JarFile object
   * @param folder - appropriate resource folder (i.e. /glpk-4.60/mac64/)
   * @return map of Filenames & InputStreams
   * @throws IOException if jarFile was specified wrong
   */
  private static Map<String,InputStream> getJarStreams(JarFile jar, String folder) throws IOException {
    TreeMap<String, InputStream> jarStreams = new TreeMap<>();
    Set<JarEntry> entriesSet = new HashSet<>();

    String name;
    JarEntry entry;

    /* Get ALL entries in .jar file */
    final Enumeration<JarEntry> entries = jar.entries();
    int items = 0;
    while(entries.hasMoreElements()) {
      entry = entries.nextElement();
      name = entry.getName();
      /* Find the folder with required pattern i.e. /glpk-4.60/mac64/ */
      if (name.contains(folder)) {
        /* Process ONLY found directory and ignore other entries*/
        if (!entry.isDirectory()) {
          entriesSet.add(entry);
        }
      }
    }

    /* Transform JarEntries into pairs of Filename & InputStream to that file */
    for (final JarEntry libFile: entriesSet) {
      String filename = libFile.getName();
      filename = filename.substring(filename.lastIndexOf('/') + 1);
      InputStream iStream = jar.getInputStream(libFile);
      jarStreams.put(filename, iStream);
    }

    return jarStreams;
  }

  /**
   * <p>Method loads files from specified folder </p>
   * @param folder - folder with native libraries
   * @return map of Filenames & InputStreams
   * @throws FileNotFoundException - if any
   */
  private static Map<String,InputStream> getIdeStreams(String folder) throws FileNotFoundException {
    Map<String, InputStream> ideStreams = new TreeMap<>();
    File files[] = (new File(folder)).listFiles();
    for (File lib: files) {
      String libName = lib.getName();
      InputStream iStream = new FileInputStream(lib);
      ideStreams.put(libName, iStream);
    }

    return ideStreams;
  }
}