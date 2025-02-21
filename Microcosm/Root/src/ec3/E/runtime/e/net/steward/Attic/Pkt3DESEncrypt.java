// Copyright 1997 Electric Communities. All rights reserved worldwide.
package ec.e.net.steward;

import ec.util.NestedException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import ec.e.net.steward.NetworkSender;
import ec.util.assertion.Assertion;
import ec.util.Native;
import ec.e.run.Trace;
import cryptix.security.DES;

public class Pkt3DESEncrypt {

    private static final Trace tr = new Trace("ec.e.net.steward.Pkt3DESEncrypt");

    private byte[] myIV;
    private byte[] myDESKeys;
    private NetworkSender myOuterSender;

    private final byte[] myPreviousBlock = new byte[8];

    private final byte[] myPad = new byte[8];
    private DES myDes1, myDes2, myDes3;

    public Pkt3DESEncrypt(NetworkSender outerSender, byte[] desKeys, byte[] iv) {
        myOuterSender = outerSender;
        myDESKeys = desKeys;
        myIV = iv;
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
        init();
    }

    public void init() {    //Only needed if flush not used
        System.arraycopy(myIV,0, myPreviousBlock,0, 8);
    }

    public void encrypt(byte[] buffer) {
        encrypt(buffer, 0, buffer.length);
    }

    public void encrypt(byte[] buffer, int off, int len) {
        long startTime = tr.event ? Native.queryTimer() : 0;
        if (0 != (len&7)) {
            Assertion.test(false, "Length must be a multiple of 8, len="+len);
        }
        System.arraycopy(myIV,0, myPreviousBlock,0, 8);
        for (int cursor=off; cursor<off+len; cursor+=8) {
            if (tr.verbose) tr.verbosem("Plaintext=" + eightToHex(buffer, cursor));
            xor(buffer, cursor, myPreviousBlock);   // Do CBC mode
            //Encrypt myCurrentBlock with 3DES Encrypt(Decrypt(Encrypt()))
            myDes1.encrypt(buffer, cursor, buffer, cursor);
            myDes2.decrypt(buffer, cursor, buffer, cursor);
            myDes3.encrypt(buffer, cursor, buffer, cursor);
            if (tr.verbose) tr.verbosem("Cyphertext=" + eightToHex(buffer, cursor));
            System.arraycopy(buffer, cursor, myPreviousBlock,0, 8);
        }
        increment(myIV);
        if (tr.event) tr.eventm("Pkt3DESEncrypt(" + buffer.length + "), time " 
                + (Native.queryTimer() - startTime) + " microseconds");
    }


    private void xor(byte[] inOut, int offset, byte[] in) {
        for (int i=0; i<8; i++) inOut[offset+i] ^= in[i];
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
