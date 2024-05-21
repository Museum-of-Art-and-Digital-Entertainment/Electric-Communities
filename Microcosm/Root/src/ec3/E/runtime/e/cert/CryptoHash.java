package ec.cert;

import ec.util.NestedException;
import java.util.Hashtable;
import java.security.MessageDigest;  // java-1.1
import java.security.NoSuchAlgorithmException;  // java-1.1
// import ec.crypt.MD5;                // ecrypt
// import ec.ssl.MD5MessageDigest;  // SSL
import java.io.Serializable;

/* Copyright 1997 Electric Communities. All rights reserved.
 * By KJD 970605
 */

/**

 * The main reason this is not GUEST is that System.loadLibrary is not
 * available to GUESTs. If we move that load elsewhere we could make this
 * GUEST if we wanted to.

 */

public class CryptoHash extends Object 
                implements Serializable, RtStateUpgradeable {

    private static CryptoHash theNullCryptoHash;
    private byte[] myHash;

    // There's more than one of these but we provide one instance
    // that can be freely used in comparisons etc.

    /**
      
     * Constructor to return a CryptoHash given a byte array
     * representing an object. These byte arrays are typically created
     * using a seralizer.

     * @param bytes untrusted nullOK - A byte array. However, if given
     * as null, then we construct a special object, known as a
     * nullCryptoHash object.

     */

    public CryptoHash(byte[] bytes) {
        this(bytes,false);
    }

    /**
      
     * Constructor to create a CryptoHash given a byte array
     * that is the actual hash code.

     * @param bytes untrusted nullOK - A byte array, the hash code to use.

     * XXX This is likely a security hole since anyone can create a
     * CryptoHash given nothing but the data bytes of the hash. This
     * allows them to create a DataHolder for this object, and they
     * can then cash in the dataholder for the object itself.

     */

    public CryptoHash(byte[] bytes, boolean bytesAreTheHash) {
        if (bytes == null) {
            myHash = new byte[4];
            for (int i = 0; i < 4; i++) myHash[i] = 0;
        } else {
            if (bytesAreTheHash) {
                // Copy the bytes. Don't trust the caller to not modify them later.
                myHash = new byte[bytes.length];
                System.arraycopy(bytes,0,myHash,0,bytes.length);
            } else
                myHash = computeCryptoHashForBytes(bytes);
        }
    }

    /**

     * Create a CryptoHash object with the CryptoHash value taken from
     * a String. This is intended for testing purposes only.

     */

      public CryptoHash(String s) {
          if (s == null) {
              myHash = new byte[4];
              for (int i = 0; i < 4; i++) myHash[i] = 0;
              return;
          }
          myHash = ec.util.HexStringUtils.hexStringToByteArray(s);
      }
    
    static {
          // System.loadLibrary("ecrypt");
          theNullCryptoHash = new CryptoHash((byte[])null);
    }
    
    /**

     * provide package-wide access to bytes - used by equals()

     */

    public byte[] getHashBytes() {
        return myHash;
    }

    /**

     * Provide free access to copies of our hashcode bytes.  If we
     * allowed others a reference to our bytes, they could smash them,
     * which would have been a security leak.

     */

    public byte[] getCopyOfHashBytes() {
        byte[] copy = new byte[myHash.length];
        System.arraycopy(myHash,0,copy,0,myHash.length);
        return copy;
    }

    /**

     * Given a cryptohash byte array, compute and return a hashcode
     * (an int) for it.  Since we are dealing with cryptographic
     * hashes we know we can just grab four bytes at the start of the
     * array and use that as a hashcode with *excellent* distribution.

     */

    // Java prevents us from making an endian-dependent mistake here
    // since we cannot cast the array contents to an integer.

    public int hashCode() {
        return  (myHash[0] << 24) + (myHash[1] << 16) + (myHash[2] << 8) + myHash[3];
    }

    /**

     * Our equality predicate.  We are equal to any other CryptoHash
     * object with the same CryptoHash contents, byte for byte.

     * @param object untrusted nullOk - something to compare ourselves to.

     */

    public boolean equals(Object object) {
        if (! (object instanceof CryptoHash)) return false;
        CryptoHash other = (CryptoHash) object;
        
        byte[] otherHash = other.getHashBytes();
        if (myHash[0] != otherHash[0]) return false; // Optimize for 99.61% of all bad cases
        int myLength = myHash.length;
        if (myLength != otherHash.length) return false;
        for (int i = 1; i< myLength; i++) {
            if (myHash[i] != otherHash[i]) return false;
        }
        return true;
    }

    /**

     * Return the singleton instance of a CryptoHash that we use to
     * represent null data.

     */

    public static CryptoHash nullCryptoHash() {
        return theNullCryptoHash;
    }

    /**

     * Returns a printable string - We show enough of the bytes to
     * give us a semi-unique hint of the contents. Note that the
     * result ignores all but the first four (4) bytes.  This number
     * is hardcoded into into byteArrayToAbbreviatedHexString and
     * above in the constructor when given a null argument.

     */

    public String toString() {
        return ec.util.HexStringUtils.byteArrayToAbbreviatedHexString("CRH#", myHash, "...");
    }

    /**

     * Return a string of hex characters based on the contents of the
     * entire hash.

     */

    public String asHex() {
        return ec.util.HexStringUtils.byteArrayToHexString(myHash);
    }

    /**

     * Given a byte array, compute and return a cryptohash for it.  We
     * make this available as a static method since it's generally
     * useful and conveys no authority.

     */

    public static byte[] computeCryptoHashForBytes(byte[] dataBytes) {

        // MD5MessageDigest md5 = new MD5MessageDigest();   // SSL
        // byte[] key = md5.digest(keyBytes);               // SSL

        // MD5 md5 = new MD5();                                // ecrypt
        // md5.update(dataBytes);                              // ecrypt
        // return md5.make_digest();                           // ecrypt

        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); // java-1.1
            return md.digest(dataBytes);                         // java-1.1
        }
        catch (NoSuchAlgorithmException e) {
            throw new NestedException("Can't get MD5 crypto routines", e);
        }
    }

    /**

     * main() - for testing.

     */


    public static void main(String[] argv) {

        Hashtable ht = new Hashtable(10);
        byte[] data1 = new byte[10];
        byte[] data2 = new byte[10];

        for (int i = 0; i < 10; i++) {
            data1[i] = (byte)i;
            data2[i] = (byte)i;
        }

        CryptoHash h1 = new CryptoHash(data1);
        CryptoHash h2 = new CryptoHash(data1);

        if (h1.hashCode() != h2.hashCode()) System.out.println("FAILURE - hashcodes are not equal");
        if (! h1.equals(h2)) System.out.println("FAILURE - CryptoHashes are not equal");

        ht.put(h1,data1);
        Object result = ht.get(h2);
        if (result == null) System.out.println("FAILURE - Hashtable lookup does not work");
    }
}
