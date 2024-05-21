
/*
    Conversion Utilities
*/

package ec.util;

import java.util.Vector;
import java.util.Enumeration;

public class Convert {

/* Usefull functions for converting ints and longs into
   byte arrays and back.
*/
        public static byte[] convertToBytes(long inLong) {
        byte outv[] = new byte[8];
                outv[0] = (byte) ((inLong >>> 56) & 0xFF);
                outv[1] = (byte) ((inLong >>> 48) & 0xFF);
                outv[2] = (byte) ((inLong >>> 40) & 0xFF);
                outv[3] = (byte) ((inLong >>> 32) & 0xFF);
                outv[4] = (byte) ((inLong >>> 24) & 0xFF);
                outv[5] = (byte) ((inLong >>> 16) & 0xFF);
                outv[6] = (byte) ((inLong >>>  8) & 0xFF);
                outv[7] = (byte) (inLong & 0xFF);
                return(outv);
        }

        public static byte[] convertToBytes(int inInt) {
        byte outv[] = new byte[4];
                outv[0] = (byte) ((inInt >>> 24) & 0xFF);
                outv[1] = (byte) ((inInt >>> 16) & 0xFF);
                outv[2] = (byte) ((inInt >>> 8) & 0xFF);
                outv[3] = (byte) (inInt & 0xFF);
                return(outv);
        }

    public static long convertToLong(byte inv[]) {
    return (((long)inv[0] << 56) & ((long)0xFF << 56)) |
           (((long)inv[1] << 48) & ((long)0xFF << 48)) |
           (((long)inv[2] << 40) & ((long)0xFF << 40)) |
           (((long)inv[3] << 32) & ((long)0xFF << 32)) |
           (((long)inv[4] << 24) & ((long)0xFF << 24)) |
           (((long)inv[5] << 16) & ((long)0xFF << 16)) |
           (((long)inv[6] <<  8) & ((long)0xFF <<  8)) |
           (((long)inv[7] <<  0) & ((long)0xFF <<  0));

    }

    public static int convertToInt(byte inv[]) {
    return ((inv[0]<< 24) & (0xFF << 24)) |
           ((inv[1]<< 16) & (0xFF << 16)) |
           ((inv[2]<<  8) & (0xFF <<  8)) |
           ((inv[3]<<  0) & (0xFF <<  0));

    }

    public static byte[] StringToByteArray(String s) {
        char[] c = s.toCharArray();
        byte[] b = new byte[c.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) c[i];
        }
        return b;
    }

    private static final char[] hexdigits = { '0', '1', '2', '3', '4',
        '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String ByteArrayToHexString(byte[] b) {
        char[] c = new char[b.length * 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            int nybble;
            nybble = (b[i] >> 4) & 0xF;
            c[j++] = hexdigits[nybble];
            nybble = b[i] & 0xF;
            c[j++] = hexdigits[nybble];
        }
        return new String(c);
    }

  // HexStringToByteArray currently assumes input is a HexString without check
    public static byte[] HexStringToByteArray(String s) {
        char[] c = s.toCharArray();
        byte[] b = new byte[c.length/2];
        int i,j;
        for (i = 0, j = 0; i < b.length; i++, j +=2) {
            // System.out.println("c[j],c[j+1] = " + c[j] + " " + c[j+1]);
            char m = (char)(Convert.HexCharToOffset(c[j]) & 0xF);
            char n = (char)(m << 4);
            char o = (char) (n & 0xF0);
            char r = (char)( Convert.HexCharToOffset(c[j+1]) & 0xF);
            char t = (char)( r & 0xF);
          b[i] = (byte)(o+t);
            // System.out.println("m = " + (int)m + " n =  " + (int)n + " o = " + (int)o
          //                                         + " r =  " + (int)r + " t = " + (int)t + " b[] = " + b[i]);
        }
        return b;
    }

    // HexCharToOffset currently assumes input is a HexChar without check
    // FIX caller must mask
    private static int HexCharToOffset(int c) {
        if (c <= '9')
            return (char)(c - '0');
        else
            return (char)(c - '0' - 7); // adjust to reflect character set ordering
    }

    public static String EscapeChar(String s, char c) {
          StringBuffer s_out = new StringBuffer(s.length() + 16);
          char tc;
          for(int i = 0; i < s.length(); i++) {
            tc = s.charAt(i);
            if (tc == '\\') {
              s_out.append('\\');
              s_out.append('\\');
            } else if (tc == c) {
              s_out.append('\\');
              s_out.append(c);
            } else {
              s_out.append(tc);
            }
          }
          return new String(s_out);
    }

        public static char[] expn_char (char[] in, int grow) {
        char[] tchar = new char[in.length+grow];

        System.arraycopy(in,0,tchar,0,in.length);
        return tchar;
        }
}
