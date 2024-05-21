// Copyright 1997 Electric Communities. All rights reserved worldwide.
package ec.e.net.steward;

import ec.util.NestedException;
import java.security.NoSuchAlgorithmException;
import ec.util.assertion.Assertion;
import ec.util.Native;
import ec.e.run.Trace;
import cryptix.security.DES;

public class Pkt3DESDecrypt {

    private static final Trace tr = new Trace("ec.e.net.steward.Pkt3DESDecrypt");

    private byte[] myIV;
    private byte[] myDESKeys;

    private byte[] myPreviousBlock = new byte[8];
    private byte[] myCurrentBlock = new byte[8];

    private DES myDes1, myDes2, myDes3;

    private final byte[] myPad = new byte[8];

    public Pkt3DESDecrypt(byte[] desKeys, byte[] iv) {
        myDESKeys = desKeys;
        myIV = iv;
        System.arraycopy(myIV,0, myPreviousBlock,0, 8);
        try {
            byte[] key = new byte[8];
            System.arraycopy(myDESKeys, 0, key, 0, 8);
            myDes1 = new DES(key);
            System.arraycopy(myDESKeys, 8, key, 0, 8);
            myDes2 = new DES(key);
            System.arraycopy(myDESKeys, 16, key, 0, 8);
            myDes3 = new DES(key);
        }
        catch (Exception e) {
            tr.errorm("Problem initializing DES keys", e);
            throw new NestedException("Problem initializing DES keys", e);
        }
    }

    public void reset() {   
        increment(myIV);
        System.arraycopy(myIV,0, myPreviousBlock,0, 8);
    }

    public void decrypt(byte[] buffer) {
        decrypt(buffer, 0, buffer.length);
    }

    public void decrypt(byte[] buffer, int off, int len) {
        
        long startTime = tr.event ? Native.queryTimer() : 0;
        Assertion.test(0==(len&0x7), 
                       "Buffer length not a multiple of 8, len = "
                       + len);

        for (int cursor=off; cursor<off+len; cursor+=8) {
            //Save current block's cyphertext for CBC mode
            if (tr.verbose) tr.verbosem("Cyphertext=" + eightToHex(buffer,cursor));
            System.arraycopy(buffer, cursor, myCurrentBlock, 0, 8);
            //Decrypt a block - 3DES Decrypt(Encrypt(Decrypt()))
            myDes3.decrypt(buffer, cursor, buffer, cursor);
            myDes2.encrypt(buffer, cursor, buffer, cursor);
            myDes1.decrypt(buffer, cursor, buffer, cursor);
            xor8(buffer, cursor, myPreviousBlock);  // Do CBC mode
            if (tr.verbose) tr.verbosem("Plaintext=" + eightToHex(buffer,cursor));
            System.arraycopy(myCurrentBlock,0, myPreviousBlock,0, 8);
        }
        if (tr.event) tr.eventm("Pkt3DESDecrypt(" + buffer.length + "), time " 
                + (Native.queryTimer() - startTime) + " microseconds");
    }

    private void xor8(byte[] inOut, int off, byte[] in) {
        for (int i=0; i<8; i++) inOut[off+i] ^= in[i];
    }

    private void increment(byte[] value) { 
        for (int i=value.length-1; i>=0; i--) {
            byte v = (value[i] += 1);
            if (0 != v) break;
        }
    }

    private static String eightToHex(byte[] b, int offset) {
        String ret = "";
        for (int i=offset; i<offset+8; i++) {
            ret += "0123456789abcdef".charAt((b[i]>>4)&0xf);
            ret += "0123456789abcdef".charAt(b[i]&0xf);
        }
        return ret;
    }
}
