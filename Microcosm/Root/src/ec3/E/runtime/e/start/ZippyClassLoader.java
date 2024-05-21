package ec.e.run;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

class ZippyClassLoader extends ClassLoader
{
  Hashtable cache = new Hashtable(3000);
  String zipFileName;
  Hashtable bytesCache = new Hashtable(8000);

  //static final boolean debug = true;
  static final boolean debug = false;

    public ZippyClassLoader(String zipFileName) 
    {
        this.zipFileName = zipFileName;
    }

  public int loadCacheWithClassBytes() throws IOException {
    if (debug) System.out.println("Loading classes from zip "+zipFileName);
         
    ZipInputStream theStream = new ZipInputStream(
                                  new BufferedInputStream(
                                                                  new FileInputStream(
                                                                      new File(zipFileName))));
    try {
      ZipEntry entry = null;
      int length = 0;
      while ((entry = theStream.getNextEntry()) != null) {
        length = (int) entry.getSize();
        if (!entry.isDirectory()) {
          // Put in hashtable!
          bytesCache.put(entry.getName(), getBytesToEOF(theStream));
        }
        theStream.closeEntry();
      }
    } finally {
      theStream.close();
            theStream = null;
    }
    if (debug) System.out.println("Done loading from zip");
    return bytesCache.size();
  }

  public void preLoadClasses(Vector classesToLoad)  {
    for(Enumeration e=classesToLoad.elements(); e.hasMoreElements(); )  {
      try { 
        String aString = (String) e.nextElement();
        // Load the class with this classloader
        this.loadClass(aString);
      } catch (Exception except) {
        // Ignore if any error unless we want spam
        if (debug)  {
          System.out.println("Could not load class '"+classesToLoad+"'");
          except.printStackTrace();
        }
      }
    }
  }

  private byte[] getBytesToEOF(InputStream is) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      byte[] buf = new byte[1024];
      int len;
      while ((len = is.read(buf, 0, buf.length)) != -1) {
          out.write(buf, 0, len);
      }
      return out.toByteArray();
  }


  private byte[] getClassData(String className) {
      byte result[] = (byte []) bytesCache.remove(className);
      return result;
  }

    private byte[] readBytes(String className)
    {
        try {
            String classPath = className.replace('.','/') + ".class";
            return getClassData(classPath);
        } catch (Exception e) {
            return null;
        }
    }

  public Class loadTheClass(String name, boolean resolve)  {
    return loadClass(name, resolve);
  }

    public Class loadClass(String className)
    {
        return loadClass(className, true);
    }

    public Class loadClass(String className, boolean resolve)
    {
        if (isSystemClass(className)) {
            try {
                return findSystemClass(className);
            }
            catch (ClassNotFoundException e) {
                return null;
            }
        }

        if (debug) System.out.println("Attempting to load: "+className);

        Class cls = (Class) cache.get(className);

        if (cls == null) {
            byte data[] = readBytes(className);
            if (data != null) {
                if (debug) System.out.println("\tFound in cache!  Length of class is "+data.length);
                cls = defineClass(className, data);
            } else {
                try {
                    if (debug) System.out.println("\tNot found in cache.  Passing to null classloader");
                    return findSystemClass(className);
                }
                catch (ClassNotFoundException e) {
                    return null;
                }
            }
            if (cls == null) {
                return null;
            }
        }

        /*
         * If need be, fully resolve the class before returning.
         */

        if (resolve) {
            resolveClass(cls);
        }
        return cls;
    }

    protected synchronized Class defineClass(String className, byte[] data)
    {
        Class cls = (Class) cache.get(className);
        if (cls != null) return cls;
        cls = defineClass(className, data, 0, data.length);
        if (cls != null) {
            cache.put(className, cls);
        }
        // XXX The following is a bogus work around for the java 1.1.X bug
        // having to do with the incompatibility between non-null classloaders
        // and native code.  See http://dugite.ee.uwa.edu.au/~gareth/jni_fix.html
        // for more info.
        Object[] signers = new Object[1];
    signers[0] = cls;
    setSigners(cls, signers);
    // end XXX
        return cls;
    }

    protected boolean isSystemClass(String className)
    {
        if (className.startsWith("java.")) {
            return true;
        }

        return false;
    }

}
