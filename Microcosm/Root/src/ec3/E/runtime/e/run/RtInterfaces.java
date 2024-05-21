package ec.e.run;

import java.io.IOException;
import ec.util.ReadOnlyHashtable;

public interface RtDecoder
{
    static public final int RECIPE_DELAY = 0;
    static public final int HASH_DELAY = 1;
    static public final int NUM_DELAY = 2;

    /**
     * Decodes an object graph, for use from the outside.
     */
    Object decodeGraph() throws IOException;

    /**
     * Decodes an object within an object graph.  For use while
     * decoding an object graph.  
     */
    Object decodeObject() throws IOException;

    /**
     * For use while decoding to postpone initialization actions until
     * after other initializations have occurred. 
     */
    public void delay(int delayCategory, Runnable thunk);

    void replaceObjectInTable (Object old, Object obj);
    void insertObjectInTable (int index, Object obj); 

    void readFully (byte b[], int off, int len) throws IOException;
    void readFully (byte b[]) throws IOException;

    boolean readBoolean () throws IOException;
    byte readByte () throws IOException;
    //int readUnsignedByte () throws IOException;
    short readShort () throws IOException;
    //int readUnsignedShort () throws IOException;
    char readChar () throws IOException;
    int readInt () throws IOException;
    long readLong () throws IOException;
    float readFloat () throws IOException;
    double readDouble () throws IOException;
    String readUTF () throws IOException;
    RtExceptionEnv getKeeper();
}

public interface RtEncoder
{
    /**
     * Encodes an object graph.  For use from the outside.
     */
    void encodeGraph(Object obj) throws IOException;

    /**
     * Encodes an object within an object graph.  For use while
     * encoding an object graph.
     */
    void encodeObject(Object obj) throws IOException;

    byte[] getBytes();
    
    void write (byte b[], int off, int len) throws IOException;
    void write (byte b[]) throws IOException;
    
    void writeBoolean (boolean v) throws IOException;
    void writeByte (int v) throws IOException;
    void writeShort (int v) throws IOException;
    void writeChar (int v) throws IOException;
    void writeInt (int v) throws IOException;
    void writeLong (long v) throws IOException;
    void writeFloat (float v) throws IOException;
    void writeDouble (double v) throws IOException;
    void writeBytes (String s) throws IOException;
    void writeChars (String s) throws IOException;
    void writeUTF (String str) throws IOException;
    ReadOnlyHashtable getProperties();
    RtExceptionEnv getKeeper();
}

public interface RtDecodeable
{
    Object decode (RtDecoder coder) throws IOException;
}

public interface RtEncodeable 
{
    void encode (RtEncoder coder) throws IOException;
    String classNameToEncode (RtEncoder encoder);
}

public interface RtCodeable extends RtDecodeable, RtEncodeable 
{
}

public interface RtUniquelyCodeable
{
}

public interface RtDelegatingEncodeable
{
    Object delegateToEncode();
}

public interface RtDoNotEncode 
{
}

public interface RtQueueTimer
{
    public void putNow(RtQObj obj);
    public long timeInQueue(RtQObj obj);
}

//ABS 971215 Start

public interface RtDelegatingSerializable {
    /**
     *  @return object to put in StateOutputStream.
     */
    public Object delegateToSerialize();
}

/**
* Tag interface to indicate that the class will maintaintained for
* state oriented saving & upgrade.
*/

public interface RtStateUpgradeable {
}

//ABS 971215 End

public interface RtCodeableThrowable
{
    void encodeThrowableState(RtEncoder stream) throws IOException;
    void decodeThrowableState(RtDecoder stream) throws IOException;
}   
