package ec.security;

import cryptix.security.CipherFeedback;
import cryptix.security.DES;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import java.security.KeyException;
// JAY import java.security.IllegalBlockSizeException;

public class Cipher
{
    public static final int UNINITIALIZED = 0;
    public static final int ENCRYPT = 1;
    public static final int DECRYPT = 2;

    public static Cipher getInstance(String algorithm) throws NoSuchAlgorithmException {
        if (!algorithm.startsWith("DES")) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        return new DESCipher();
    }

    public int getState() {
        throw new Error("must be overrided");
    }

    public int blockSize() {
        throw new Error("must be overrided");
    }

    public void initEncrypt(Key key) throws KeyException {
        throw new Error("must be overrided");
    }

    public void initDecrypt(Key key) throws KeyException {
        throw new Error("must be overrided");
    }

    public int crypt(byte in[], int inOff, int inLen, byte out[], int outOff) /* JAY throws IllegalBlockSizeException */ {
        throw new Error("must be overrided");
    }

}

class DESCipher extends Cipher
{
    private int myState;
    private CipherFeedback myCFB;

    DESCipher() {
        myState = Cipher.UNINITIALIZED;
    }

    public int getState() {
        return myState;
    }

    public int blockSize() {
        return DES.BLOCK_LENGTH;
    }

    private void init(Key key) {
        DESKey dkey = (DESKey)key;
        byte bkey[] = dkey.getBytes();
        byte ks[] = new byte[DES.KEY_LENGTH];
        byte iv[] = new byte[DES.BLOCK_LENGTH];
        System.arraycopy(bkey, 0, ks, 0, DES.KEY_LENGTH);
        System.arraycopy(bkey, DES.KEY_LENGTH, iv, 0, DES.BLOCK_LENGTH);
        myCFB = new CipherFeedback(new DES(ks), iv);
    }

    public void initEncrypt(Key key) throws KeyException {
        init(key);
        myState = Cipher.ENCRYPT;
    }

    public void initDecrypt(Key key) throws KeyException {
        init(key);
        myState = Cipher.DECRYPT;
    }

    public int crypt(byte in[], int inOff, int inLen, byte out[], int outOff) /* JAY throws IllegalBlockSizeException */ {
        if (myState == Cipher.ENCRYPT) {
            myCFB.encrypt(in, inOff, out, outOff, inLen);
        }
        else if (myState == Cipher.DECRYPT) {
            myCFB.decrypt(in, inOff, out, outOff, inLen);
        }
        else {
            throw new Error("uninitialized Cipher");
        }
        return inLen;
    }
}
