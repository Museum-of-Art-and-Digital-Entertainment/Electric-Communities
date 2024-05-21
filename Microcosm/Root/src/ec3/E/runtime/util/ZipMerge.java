package ec.util;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class ZipMerge 
{
    public static final int BUFLEN = 65536;
    
    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("usage: java ec.util.ZipMerge files.zip to.zip merge.zip result.zip");
            System.exit(1);
        }
        try {
            String resultfile = args[args.length-1] ;
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(resultfile));
            byte buf[] = new byte[BUFLEN];
        
            for (int i=0; i<args.length-1; i++) {
                String srcfile = args[i];
                System.out.println("copying " + srcfile + " into " + resultfile);
                ZipFile in = new ZipFile(srcfile);
                Enumeration en = in.entries();
                while (en.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry)en.nextElement();
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
            out.finish();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
