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

import com.google.common.io.Files;
import io.github.msdk.MSDKException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * <p> Class NativeLibraryLoader </p>
 * This class allows to dynamically load native libraries from .jar files with updating java.library.path variable
 */
public class NativeLibraryLoader {
  private NativeLibraryLoader() {}
  private static Path getResourcePath(String resource) throws MSDKException {
    final URL url = NativeLibraryLoader.class.getClassLoader().getResource(resource);
    try {
      return Paths.get(url.toURI()).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new MSDKException(e);
    }
  }

  /**
   * Public method for external usage, loads specified `libs` in `folder`
   * The folder structure is strict
   *  folder
   *    -w64
   *      --lib1
   *      --lib2
   *    -w32
   *    -x86_64-linux-gnu"
   *
   * @param folder - specify the name of the library to be loaded (example - glpk_4_60)
   * @param libs - array of exact names of libraries (without extensions)
   * @throws MSDKException if any
   * @throws IOException if any
   */
  public static void loadLibraryFromJar(String folder, String[] libs) throws MSDKException, IOException {
    String suffix = "", realPath;
    String arch = System.getProperty("os.arch").toLowerCase().endsWith("64") ? "w64" : "w32";

    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      suffix = ".dll";
    } else if (arch.contains("linux")) { // TODO: Not sure about MACs and what happens in case of linux
      suffix = ".so";
      arch = "x86_64-linux-gnu";
    }

    // TODO: get resource folder
    realPath = getResourcePath(folder) + "/" + arch + "/";

    // Make new java.library.path
    File temporaryDir = createLibraryPath();
    // Load native libraries
    for (String libname: libs) {
      moveLibrary(temporaryDir, realPath, libname, suffix);
    }
  }

  /**
   * Method for updating the java.library.path with a new temp folder for native libraries
   * System.setProperty(path) does not make any sense because JVM sets it during initialization
   * Altering library path requires additional code
   * @return temporary folder file
   * @throws MSDKException if any
   */
  private static File createLibraryPath() throws MSDKException {
    File tempDir = Files.createTempDir();
    tempDir.deleteOnExit();

    String absPath = tempDir.getAbsolutePath();
    String oldProperty = System.getProperty("java.library.path");
    String newPath = oldProperty + ";" + absPath;

    try {
//    setLibraryPath(newPath);
      addLibraryPath(absPath);
    } catch (Exception e) {
      throw new MSDKException(e);  //Catches NoSuchFieldException & IllegalAccessException
    }

    return tempDir;
  }

  /**
   * Sets the java library path to the specified path
   * Unsets sys_paths first and reinitializes it a try of library load
   * @param path the new library path
   * @throws Exception if any
   */
  private static void setLibraryPath(String path) throws NoSuchFieldException, IllegalAccessException {
    System.setProperty("java.library.path", path);

    final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
    sysPathsField.setAccessible(true);
    sysPathsField.set(null, null);
  }

  /**
   * Adds the specified path to the java library path w/o reinitialization
   *
   * @param pathToAdd the path to add
   * @throws Exception if any
   */
  private static void addLibraryPath(String pathToAdd) throws NoSuchFieldException, IllegalAccessException{
    final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
    usrPathsField.setAccessible(true);

    //get array of paths
    final String[] paths = (String[])usrPathsField.get(null);

    //check if the path to add is already present
    for(String path : paths) {
      if(path.equals(pathToAdd)) {
        return;
      }
    }

    //add the new path
    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
    newPaths[newPaths.length-1] = pathToAdd;
    usrPathsField.set(null, newPaths);
  }


  /**
   * Moves library from .jar to temporary folder and loads it
   * Temp folder previously should be added to java.library.path
   * @param tempDirectory - directory to store file at
   * @param realFolder - directory to copy file from
   * @param name - name of the library
   * @param suffix - file extension (.so, .dll, etc)
   * @throws IOException if any
   * @throws MSDKException if any
   */
  private static void moveLibrary(File tempDirectory, String realFolder, String name, String suffix) throws IOException, MSDKException {
    String path;
    path = realFolder + "/" + name + suffix;
    String fname = name + suffix;

    File f = new File(tempDirectory, fname);

    if (!tempDirectory.exists()) {
      throw new FileNotFoundException("File " + tempDirectory.getAbsolutePath() + " does not exist");
    } else {
        Files.copy(new File(path), f);
        System.load(f.getAbsolutePath());
    }
  }
}