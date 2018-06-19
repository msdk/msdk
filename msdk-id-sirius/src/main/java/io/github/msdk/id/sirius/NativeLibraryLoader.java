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
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p> Class NativeLibraryLoader </p> This class allows to dynamically load native libraries from
 * .jar files with updating java.library.path variable
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

  private static InputStream getResourceStream(String resource) {
    return NativeLibraryLoader.class.getResourceAsStream(resource);
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

    String realPath;
    String arch = getArch();
    String osname = getOsName();
    String subfolder = getSubfolder(arch, osname);

    logger.info("OS type = {} and OS arch = {}", subfolder, arch);

    // Make new java.library.path
    File temporaryDir = createLibraryPath();

    folder += "/" + subfolder + arch + "/";

    final File jarFile = new File(NativeLibraryLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    final Map<String, InputStream> libraries = new TreeMap<>();

    if(jarFile.isFile()) {  // Run with JAR file
      Set<JarEntry> entriesSet = new HashSet<>();

      final JarFile jar = new JarFile(jarFile);
      String name;
      JarEntry entry;

      final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
      while(entries.hasMoreElements()) {
        entry = entries.nextElement();
        name = entry.getName();
        if (name.contains(folder)) { //filter according to the path
          do {
            if (!entry.isDirectory()) entriesSet.add(entry);
            entry = entries.nextElement();
            name = entry.getName();
          } while (name.contains(folder));
          break;
        }
      }

      for (final JarEntry libFile: entriesSet) {
        String filename = libFile.getName();
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        InputStream iStream = jar.getInputStream(libFile);
        libraries.put(filename, iStream);
      }

      moveLibraries(libraries, temporaryDir, libs);
      jar.close();
    } else { // Run with IDE
      realPath = getResourcePath(folder).toString();
      // Load native libraries
      moveLibraries(temporaryDir, realPath, libs);
    }
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
   * <p>Method moves library from .jar to temporary folder and loads it</p>
   * Temp folder previously should be added to java.library.path
   *
   * @param tempDirectory - directory to store file at
   * @param realFolder - directory to copy file from
   * @throws IOException if any
   * @throws MSDKException if any
   */
  private static void moveLibraries(File tempDirectory, String realFolder, String[] libs)
      throws IOException, MSDKException {

    File[] files = (new File(realFolder)).listFiles();
    File temp;

    if (!tempDirectory.exists()) {
      throw new FileNotFoundException("File " + tempDirectory.getAbsolutePath() + " does not exist");
    } else {
      // Copy files
      for (File f: files) {
        if (!f.isDirectory()) {
          temp = new File(tempDirectory, f.getName());
          Files.copy(f, temp);
        }
      }

      loadLibraries(libs);
    }
  }

  private static void moveLibraries(Map<String, InputStream> fileStreams, File tempDirectory, String[] libNames) throws FileNotFoundException, IOException {
    if (tempDirectory == null || !tempDirectory.exists())
      throw new FileNotFoundException("Temporary directory was not created");

    OutputStream outputStream;
    byte buffer[] = new byte[4096];
    int bytes;

    for (Map.Entry<String, InputStream> pair: fileStreams.entrySet()) {
      File temp = new File(tempDirectory, pair.getKey());
      final InputStream iStream = pair.getValue();
      outputStream = new FileOutputStream(temp);

      while ((bytes = iStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, bytes);
      }
      iStream.close();
      outputStream.close();
    }

    loadLibraries(libNames);
  }

  private static void loadLibraries(String[] libraryNames) {
    for (String lib: libraryNames) {
      System.loadLibrary(lib);
      logger.info("Successfully loaded {} library", lib);
    }
  }

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

  private static String getArch() throws  MSDKException {
    String arch = System.getProperty("os.arch");
    if (arch == null)
      throw new MSDKException("Can not identify os.arch property");

    return arch.toLowerCase().endsWith("64") ? "64" : "32";
  }

  private static String getOsName() throws MSDKException {
    String osname = System.getProperty("os.name");
    if (osname == null)
      throw new MSDKException("Can not identify os.name property");

    return osname.toLowerCase();
  }
}