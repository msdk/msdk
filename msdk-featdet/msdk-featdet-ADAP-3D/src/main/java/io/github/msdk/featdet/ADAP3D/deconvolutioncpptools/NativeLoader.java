/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.featdet.ADAP3D.deconvolutioncpptools;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.io.File;
import java.io.FileOutputStream;
/**
 *
 * @author Unknown -> from stack overflow user maba. http://stackoverflow.com/questions/12036607/bundle-native-dependencies-in-runnable-jar-with-maven
 * 
 * Modified by Owen Myers to include mac os x
 * Modified by Dharak Shah to include in MSDK
 */
public class NativeLoader {
    public static final Logger LOG = Logger.getLogger(NativeLoader.class.getName());

    public NativeLoader() {
        LOG.log(Level.INFO,"in constructor of NativeLoader");
    }

    public void loadLibrary(String library) {
        try {
            String libraryString = saveLibrary(library);
            LOG.log(Level.INFO,String.format("Library String: %s", libraryString));
            System.load(libraryString);
        } catch (IOException e) {
            LOG.log(Level.WARNING,String.format("Could not find library %s as resource, trying fallback lookup through System.loadLibrary", library));
            System.loadLibrary(library);
        }
    }


    private String getOSSpecificLibraryName(String library, boolean includePath) {
        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();
        String name;
        String path;

        if (osName.startsWith("win")) {
            if (osArch.equalsIgnoreCase("x86")) {
                name = library + ".dll";
                path = "win-x86/";
            } else {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            }
        } else if (osName.startsWith("linux")) {
            if (osArch.equalsIgnoreCase("amd64")) {
                name = "lib" + library + ".so";
                path = "linux-x86_64/";
            } else if (osArch.equalsIgnoreCase("ia64")) {
                name = "lib" + library + ".so";
                path = "linux-ia64/";
            } else if (osArch.equalsIgnoreCase("i386")) {
                name = "lib" + library + ".so";
                path = "linux-x86/";
            } else {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            }
        }
        else if (osName.startsWith("mac os x")){
            if (osArch.equalsIgnoreCase("i386")) {
                name = "lib" + library + ".so";
                path = "macosx-x86_64/";
            } else if (osArch.equalsIgnoreCase("ppc")) {
                name = "lib" + library + ".so";
                path = "macosx-x86_64/";
            } else if (osArch.equalsIgnoreCase("x86_64")) {
                name = "lib" + library + ".so";
                path = "macosx-x86_64/";
            } else {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            }
        }
            
        else {
            throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
        }

        return includePath ? path + name : name;
    }

    private String saveLibrary(String library) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            String libraryName = getOSSpecificLibraryName(library, true);
            //in = this.getClass().getClassLoader().getResourceAsStream("lib/" + libraryName);
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream("lib/" + libraryName);
            String tmpDirName = System.getProperty("java.io.tmpdir");
            File tmpDir = new File(tmpDirName);
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }
            File file = File.createTempFile(library + "-", ".tmp", tmpDir);
            // Clean up the file when exiting
            file.deleteOnExit();
            out = new FileOutputStream(file);

            int cnt;
            byte buf[] = new byte[16 * 1024];
            // copy until done.
            while ((cnt = in.read(buf)) >= 1) {
                out.write(buf, 0, cnt);
            }
            LOG.log(Level.INFO,String.format("Saved libfile: %s", file.getAbsoluteFile()));
            return file.getAbsolutePath();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
