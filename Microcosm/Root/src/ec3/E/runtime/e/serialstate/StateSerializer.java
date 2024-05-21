package ec.e.serialstate;

import ec.e.file.EStdio;
import java.io.IOException;

public class StateSerializer implements StateObjectWriteInterest {

    static private final Trace tr = new Trace("ec.e.serialstate.StateSerializer");

    static final int SERIAL_ID = 255;
         
    private StateOutputStream myOut = null;
    private int myObjectCount;
    
    /**
     * The new Serializer will output onto 'out', using 'maker' to
     * determine what ObjOpener to use for which object.
     */
    /*package*/ StateSerializer(StateOutputStream out) {
        myOut = out;
        myObjectCount = 0;
        out.setMyInterest(this);
    }

    /**
     * Create a StateSerializer
     *
     * @param out the StateOutputStream to use for this StateSerializer
     * @exception IOException thrown if some problem reading first byte
     * from stream
     */
    static public StateSerializer make(StateOutputStream out)
        throws IOException
    {
        out.write(SERIAL_ID);
        return new StateSerializer(out);
    }

    /**  
     * Write entire graph of objects rooted by obj
     *
     * @param obj root of Object graph to encode
     * @return int the number of objects encoded
     */
    public int encodeGraph(Object obj) throws IOException {
       return encodeObject(obj);
    }

    /**
     * Encode 'obj'.
     *
     * @param obj nullOk;
     * @return int count of number of objects serialized
     */
    public int encodeObject(Object obj) throws IOException {
        // Just do it
        if (tr.debug && Trace.ON) tr.debugm("StateSerializer.encodeObject with object "+obj);
        myOut.writeObject(obj);
        return getObjectCount();
    }

    /**
     * Write given string to output stream
     *
     * @param aString the String to write
     * @exception IOException thrown if some problem writing to stream
     */
    public void writeUTF(String aString) throws IOException {
      if (tr.debug && Trace.ON) tr.debugm("StateSerializer.writeUTF with String "+aString);
      myOut.writeUTF(aString);
    }
    
    /**
     * Write a buffer of data to output stream
     *
     * @param buf the buffer of data to write
     * @param start the index within buf to indicate where to begin writing
     * @param length the number of bytes to write
     * @exception IOException thrown if some problem writing
     * @see OutputStream#write
     */
    public void write(byte buf[], int start, int length) throws IOException {
      if (tr.debug && Trace.ON) tr.debugm("StateSerializer.write with byte array "+buf);
      myOut.write(buf, start, length);
    }
    
    /**
     * Flush the output stream
     *
     * @exception IOException thrown if some problem
     * @see OutputStream#write
     */
    public void flush() throws IOException  {
      myOut.flush();
    }
    
    /**
     * Close the output stream
     *
     * @exception IOException thrown if some problem
     */
    public void close() throws IOException  {
      myOut.close();
    }
    
    // Implementation of StateObjectWriteInterest
    /**
     * Method that is called by StateOutputStream when a given object
     * is written to the output stream
     * @param stream the StateOutputStream where the Object is being written
     * @param obj the Object being written
     */
    public void objectToBeWritten(StateOutputStream stream, Object obj)  {
      if (tr.debug && Trace.ON) tr.debugm("StateSerializer.objectToBeWritten with object "+obj);
      myObjectCount++;
    }
    
    /**
     * Returns the number of objects written so far
     *
     * @return int count of number of objects written to stream
     */
    public int getObjectCount()  {
      return myObjectCount;
    }
}
