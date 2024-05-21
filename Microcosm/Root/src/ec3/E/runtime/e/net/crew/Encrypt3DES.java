// Copyright 1997 Electric Communities. All rights reserved worldwide.
package ec.e.net.crew;

import ec.util.NestedException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import ec.e.net.steward.NetworkSender;
import ec.util.assertion.Assertion;
import ec.util.Native;
import cryptix.security.DES;

public class Encrypt3DES {

    private static final Trace tr = new Trace("ec.e.net.Encrypt3DES");

    private byte[] myIV;
    private byte[] myDESKeys;

    private final byte[] myPreviousBlock = new byte[8];

    private final byte[] myPad = new byte[8];
    private DES myDes1, myDes2, myDes3;

    public Encrypt3DES(byte[] desKeys, byte[] iv) {
        myDESKeys = desKeys;
        myIV = iv;
        if (tr.event & Trace.ON) tr.eventm("Starting with IV=" + eightToHex(iv,0));
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

    public byte[] getIV() {
        if (tr.event & Trace.ON) tr.eventm("Returning IV=" + eightToHex(myIV,0));
        return myIV;
    }

    public void encrypt(byte[] buffer) {
        encrypt(buffer, 0, buffer.length);
    }

    public void encrypt(byte[] buffer, int off, int len) {
        long startTime = tr.debug ? Native.queryTimer() : 0;
        if (0 != (len&7)) {
            Assertion.test(false, "Length must be a multiple of 8, len="+len);
        }
        System.arraycopy(myIV,0, myPreviousBlock,0, 8);
        for (int cursor=off; cursor<off+len; cursor+=8) {
            if (tr.verbose && Trace.ON) tr.verbosem("Plaintext=" + eightToHex(buffer, cursor));
            xor(buffer, cursor, myPreviousBlock);   // Do CBC mode
            //Encrypt myCurrentBlock with 3DES Encrypt(Decrypt(Encrypt()))
            myDes1.encrypt(buffer, cursor, buffer, cursor);
            myDes2.decrypt(buffer, cursor, buffer, cursor);
            myDes3.encrypt(buffer, cursor, buffer, cursor);
            if (tr.verbose && Trace.ON) tr.verbosem("Cyphertext=" + eightToHex(buffer, cursor));
            System.arraycopy(buffer, cursor, myPreviousBlock,0, 8);
        }
        increment(myIV);
        if (tr.debug && Trace.ON) tr.debugm("Pkt3DESEncrypt(" + buffer.length + "), time " 
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
