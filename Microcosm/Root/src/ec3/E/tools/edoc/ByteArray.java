/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */
package ec.edoc;

/** This class provides a fairly neat abstraction of the outgoing byte stream
 *  from which a class file is constructed.  It allows the byte array to be 
 *  built in memory & output at a later date. This is no longer necessary 
 *  after the Great Const Pool reorganisation, as we no longer need to modify 
 *  anything within it but there's no great loss in using this still.
 */
class ByteArray {

    private int index = 0; // current end of contents
    private byte[] ba;
    private int size;
    private java.io.ByteArrayOutputStream b;
    private java.io.DataOutputStream d;

    private boolean debugging;

    private void dprint(String s) {
        if (debugging) {
            System.out.print(s);
            System.err.flush();
        }
    }

    ByteArray() {
        b = new java.io.ByteArrayOutputStream();
        d = new java.io.DataOutputStream(b);
        size = 1024;
        ba = new byte[size];
    }

    ByteArray(boolean debugging) {
        this.debugging = debugging;
        b = new java.io.ByteArrayOutputStream();
        d = new java.io.DataOutputStream(b);
        size = 1024;
        ba = new byte[size];
    }

    private void expand() {
        byte[] tmp = ba;
        ba = new byte[(size *= 2)];
        System.arraycopy(tmp, 0, ba, 0, tmp.length);
    }

    /** reserves a number of bytes without initialising them. 
     *  returns the index to them */
    int reserve(int n) {
        int ret = index;
        if ((index + n) > size) {
            this.expand();
        }
        index += n;
        return ret;
    }

    void add(byte[] new_ba) {
        int len = new_ba.length;
        if ((index + len) > size) {
            this.expand();
        }
        System.arraycopy(new_ba, 0, ba, index, len);
        index += len;
    }

    /** This method replaces the contents of the array at the specified location
     *  this is used to modify pointers, hence only required for u2 types.
     *  It dosn't modify the index. (Hence you might get this written over if 
     *  you replaceu2 somewhere past the index pointer)
     */
    void replaceu2(int i, int location) {

        if ((location + 2) > size) {
            this.expand();
        }
    
        b.reset();
        try {
            d.writeShort(i);
        } catch (java.io.IOException e) {
            throw new Error("Ouch - failed to write to my own memory!");
        }
    
        byte[] tmp = b.toByteArray();
        System.arraycopy(tmp, 0, ba, location, 2);
    }

    /** the writeUTF call (hence this method) automatically writes out a prefix
     *  declaring the number of bytes in the the written string. This complies
     *  with the required format for class files.
     */
    void addString(String s) {
        dprint("   |  "+s+"\n");
        b.reset();
        try {
            d.writeUTF(s);
        } catch (java.io.IOException e) {
            throw new Error("Ouch - failed to write to my own memory!");
        }
        this.add(b.toByteArray());
    }

    void addu1(int i) {
        dprint("1  |"+i+"\n");
        b.reset();
        try {
            d.writeByte(i);
        } catch (java.io.IOException e) {
            throw new Error("Ouch - failed to write to my own memory!");
        }
        this.add(b.toByteArray());
    }

    void addu2(int i) {
        dprint(" 2 |"+i+"\n");
        b.reset();
        try {
            d.writeShort(i);
        } catch (java.io.IOException e) {
            throw new Error("Ouch - failed to write to my own memory!");
        }
        this.add(b.toByteArray());
    }

    void addu4(int i) {
        dprint("  4|"+i+"\n");
        b.reset();
        try {
            d.writeInt(i);
        } catch (java.io.IOException e) {
            throw new Error("Ouch - failed to write to my own memory!");
        }
        this.add(b.toByteArray());
    }
    
    byte[] getContents() {
        byte[] tmp = new byte[index];
        System.arraycopy(ba, 0, tmp, 0, index);
        return tmp;
    }

    int getIndex() {
        return index;
    }
}
