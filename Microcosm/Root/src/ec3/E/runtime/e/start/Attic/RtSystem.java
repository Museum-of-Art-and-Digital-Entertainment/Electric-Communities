package ec.e.start;

import java.io.InputStream;
import java.io.PrintStream;

public class RtSystem {
    public static InputStream in = System.in;

    public static PrintStream out = System.out;
  
    public static PrintStream err = System.err;
    
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    public static void arraycopy(Object src,
                                    int src_position,
                                    Object dst,
                                    int dst_position,
                                     int length) {
         System.arraycopy(src, src_position, dst, dst_position, length);
     }
    public static void gc() {
        System.gc();
    }
    
    public static void runFinalization() {
        System.runFinalization();
    }
}
