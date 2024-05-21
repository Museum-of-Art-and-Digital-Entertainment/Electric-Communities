/*
	byte[] utilities
*/

package ec.util;

public class ByteArray {

public static byte[] concat(byte[] a, byte[] b) {
    byte[] c = new byte[a.length + b.length];
    for(int i = 0; i < a.length; i++) {
      c[i] = a[i];
    }
    for(int i = 0; i < b.length; i++) {
      c[a.length + i] = b[i];
    }
    return c;
  }

}
