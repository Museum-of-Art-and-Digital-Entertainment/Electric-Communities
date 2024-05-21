package ec.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.CRC32;

public class Zip
{
    public static final int BUFLEN = 65536;
    public static byte buf[];
    public static final boolean debug = false;
    public static Hashtable dirsSeen = new Hashtable();
    public static Hashtable filesSeen = new Hashtable();
    public static Hashtable myInclusionList = null;
    public static Hashtable myExclusionList = null;
    public static boolean ignoreDuplicates = false;
    public static boolean warnDuplicates = true; // XXX change default to false and add -w to cosm1/start/GNUmakefile
    
    public static void main(String args[]) {
        if (args.length < 2) {
            usage();
        }
        try {
            String resultfile = args[args.length-1] ;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(resultfile));
            buf = new byte[BUFLEN];

            int startSrc = 0;
            while (args[startSrc].startsWith("-")) {
                if (args[startSrc].startsWith("-f")) {
                    String filterFileName = args[startSrc + 1];
                    File filterFile = new File(filterFileName);
                    FileReader fis = new FileReader(filterFile);
                    BufferedReader br = new BufferedReader(fis);
                    try {
                        String className = null;
                        while ((className=br.readLine()) != null) {
                            if (myInclusionList == null) {
                                myInclusionList = new Hashtable();
                            }
                            if (!className.endsWith(".class")) {
                                className = className.replace('.','/') + ".class";
                            }
                            myInclusionList.put(className, className);
                        }
                    } catch (java.io.IOException ioe) {
                        System.err.println ("Probem with inclusion file: " + 
                                            filterFileName + " " + ioe);
                    }
                    startSrc = startSrc + 2;
                }
                else if (args[startSrc].startsWith("-x")) {
                    String exclusionFileName = args[startSrc + 1];
                    File exclusionFile = new File(exclusionFileName);
                    FileReader fis = new FileReader(exclusionFile);
                    BufferedReader br = new BufferedReader(fis);
                    try {
                        String className = null;
                        while ((className=br.readLine()) != null) {
                            if (myExclusionList == null) {
                                myExclusionList = new Hashtable();
                            }
                            if (!className.endsWith(".class")) {
                                className = className.replace('.','/') + ".class";
                            }
                            myExclusionList.put(className, className);
                        }
                    } catch (java.io.IOException ioe) {
                        System.err.println ("Probem with exclusion file: " + 
                                            exclusionFileName + " " + ioe);
                    }
                    startSrc = startSrc + 2;
                }
                else if (args[startSrc].startsWith("-i")) {
                    ignoreDuplicates = true;
                    startSrc++;
                }
                else if (args[startSrc].startsWith("-w")) {
                    warnDuplicates = true;
                    startSrc++;
                }
                else {
                    System.err.println("Unknown option: " + args[startSrc]);
                    usage();
                }
            }
            
            for (int i=startSrc; i<args.length-1; i++) {
                String srcfile = args[i];
                File src = null;
                try {
                    src = new File(srcfile);
                } catch (java.io.FileNotFoundException e) {
                    System.err.println("WARNING: cannot open " + srcfile);
                    continue;
                }
                if (!src.exists()) {
                    System.err.println("WARNING: cannot open " + srcfile);
                    continue;
                }
                if (src.isDirectory()) {
                    zipDirectory(src, "", out, srcfile);
                }
                else if (srcfile.endsWith(".zip")) {
                    zipZipFile(src, out);
                }
                else {
                    zipFile(src, srcfile, out, "command line argument");
                }
            }
            out.finish();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void usage() {
        System.err.println("usage: java ec.util.Zip [-f inclusionFilterFile] "+
                           "[-x exclusionFilterFile] [-i] files "+
                           "directories/ compressed.zip result.zip");
        System.exit(1);
    }

    public static void zipDirectory(File dir, String relativePath, 
                                    ZipOutputStream out, String argument) 
      throws IOException {
        if (debug) System.out.println("zipDirectory(" + relativePath + "/)");

        if (relativePath.length() > 0) {
            relativePath = relativePath + "/" ;
            if (dirsSeen.get(relativePath) == null) {
                dirsSeen.put(relativePath, relativePath);
                ZipEntry ent = new ZipEntry(relativePath);
                ent.setMethod(ZipEntry.STORED);
                ent.setSize(0);
                ent.setCrc(0);
                out.putNextEntry(ent);
                out.closeEntry();
            }
        }
        
        String entries[] = dir.list() ;
        for (int i=0; i<entries.length; i++) {
            File entry = new File(dir, entries[i]);
            if (entry.isDirectory()) {
                zipDirectory(entry, relativePath + entries[i], out, argument);
            }
            else {
                zipFile(entry, relativePath + entries[i], out, argument);
            }
        }
    }
    
    // XXX this does not build directory entries for parents
    public static void zipFile(File src, String name, 
                               ZipOutputStream out, String argument) 
      throws IOException {
        if ((myInclusionList == null) || (myInclusionList.containsKey(name))) {
          if ((myExclusionList == null) || (!myExclusionList.containsKey(name))) {
            if (debug) System.out.println("zipFile(" + name + ")");
            if (filesSeen.containsKey(name)) {
                if (ignoreDuplicates) {
                    return;
                }
                if (warnDuplicates) {
                    System.err.println("WARNING: duplicate zipfile entry " + 
                                       name + " in " + filesSeen.get(name) + 
                                       " and " + argument);
                }
                else {
                    throw new Error("duplicate zipfile entry " + name + 
                                    " in " + filesSeen.get(name) + 
                                    " and " + argument);
                }
            }
            filesSeen.put(name, argument);
            FileInputStream is = new FileInputStream(src);
            byte buf1[] = new byte[(int)src.length()];
            int nread=is.read(buf1);
            if (nread != src.length()) {
                throw new IOException("incorrect file size: wanted " + 
                                      src.length() + " got " + nread);
            }
            CRC32 crc = new CRC32();
            crc.reset();
            crc.update(buf1);
            ZipEntry ent = new ZipEntry(name);
            ent.setMethod(ZipEntry.STORED);
            ent.setSize(src.length());
            ent.setCrc(crc.getValue());
            out.putNextEntry(ent);
            out.write(buf1, 0, nread);
            is.close();
            out.closeEntry();
          }
        }
    }
    
    public static void zipZipFile(File srcfile, ZipOutputStream out) throws IOException {
        if (debug) System.out.println("zipZipFile(" + srcfile + ")");
        String srcName = srcfile.getName();
        ZipFile in = new ZipFile(srcfile);
        Enumeration en = in.entries();
        while (en.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)en.nextElement();
            String eName = entry.getName();
            if (entry.isDirectory()) {
                if (dirsSeen.containsKey(eName)) {
                    continue;
                }
                dirsSeen.put(eName, srcName);
            }
            if (!filesSeen.containsKey(eName)) {
                if ((myInclusionList == null) ||
                    (myInclusionList.containsKey(eName))) {
                  if ((myExclusionList == null) || 
                      (!myExclusionList.containsKey(eName))) {
                    filesSeen.put(eName, srcName);
                    InputStream is = in.getInputStream(entry);
                    out.putNextEntry(entry);
                    int nread;
                    while ((nread=is.read(buf)) > 0) {
                        out.write(buf, 0, nread);
                    }
                    is.close();
                    out.closeEntry();
                  }
                }
            }
            else if (!ignoreDuplicates) {
                if (warnDuplicates) {
                    System.err.println("WARNING: duplicate zipfile entry " + eName + " in " + filesSeen.get(eName) + " and " + srcName);
                }
                else {
                    throw new Error("duplicate zipfile entry " + eName + " in " + filesSeen.get(eName) + " and " + srcName);
                }
            }
        }
    }
}
