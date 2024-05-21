package ec.util;

public class HexStringUtils {

    private static final String hexChars = "0123456789ABCDEF";

    /**

     * Compute a hexadecimal string based on the contents of a byte array.

     */


    public static String byteArrayToHexString(byte[] byteArray) {

        StringBuffer buf = new StringBuffer(300);
        for (int i = 0; i < byteArray.length; i++) {
            int x = byteArray[i];
            if (x < 0) x += 256;
            buf.append(hexChars.charAt(x / 16));
            buf.append(hexChars.charAt(x % 16));
        }
        return buf.toString();
    }

    /**

     * Specialized version of the above for CryptoHash.toString()

     * prefix must be a string (but may be empty).
     * byteArray is the byte array to dump out.
     * postfix may be null.

     */

    public static String byteArrayToAbbreviatedHexString(String prefix, byte[] byteArray, String postfix) {
        StringBuffer buf = new StringBuffer(prefix);

        int limit = 4;
        if (limit > byteArray.length) limit = byteArray.length;

        for (int i = 0; i < limit; i++) {
            int x = byteArray[i];
            if (x < 0) x += 256;
            buf.append(hexChars.charAt(x / 16));
            buf.append(hexChars.charAt(x % 16));
        }
        if (postfix != null) buf.append(postfix);
        return buf.toString();
    }

    public static String byteArrayToReadableHexString(byte msg[]) {
        return byteArrayToReadableHexString(msg, 0, msg.length);
    }
    
    public static String byteArrayToReadableHexString(byte msg[], int off, int len) {
        StringBuffer msgString = new StringBuffer(len*59+2);
        msgString.append("\n");
        
        for (int line = 0; line < len; line+=16) {
            // put out the hex offset of the line
            msgString.append(Character.forDigit((line >> 12) & 15, 16));
            msgString.append(Character.forDigit((line >> 8) & 15, 16));
            msgString.append(Character.forDigit((line >> 4) & 15, 16));
            msgString.append(Character.forDigit(line & 15, 16));
            msgString.append(" ");
            // First put out the Hex
            for (int i=0; i<16; i++) {
                if (0 == (i&3)) {   // Space off each group of 4 bytes
                    msgString.append(" ");
                }
                if (line+i < len) {
                    byte b = msg[line+i+off];
                    msgString.append(Character.forDigit((b >> 4) & 15, 16));
                    msgString.append(Character.forDigit(b & 15, 16));
                } else {
                    msgString.append("  ");
                }
            }
            msgString.append(" ");

            // Now put out the character form
            for (int i=0; i<16; i++) {
                if (line+i < len) {
                    byte b = msg[line+i+off];
                    msgString.append( (b < ' ' || b >= 0x7f) ? '.' : (char)b );
                }
            }
            msgString.append("\n"); // End of line of up to 16 bytes
        }        
        return msgString.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.startsWith("0x") || (s.startsWith("0X"))) s = s.substring(2);
        if (s.length() % 2 != 0) s = "0" + s;

        int len = s.length() / 2;
        byte[] result = new byte[len];
        for (int i=0; i<= len; i++) {
            String byteHex = s.substring(2*i,2*i+2); // Extract two chars from string
            result[i] = Byte.parseByte(byteHex,16); // parse them as a hex byte
        }
        return result;
    }
}
