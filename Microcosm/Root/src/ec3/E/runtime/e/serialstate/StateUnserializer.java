package ec.e.serialstate;

import ec.e.file.EStdio;
import java.util.Vector;
import java.util.Enumeration;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import ec.util.NestedException;
/**
 * @see ec.e.serialstate.StateSerializer.java
 */
public class StateUnserializer implements StateObjectReadInterest {

    static private final Trace tr = new Trace("ec.e.serialstate.StateUnserializer");

    private StateInputStream myIn;
    private int myObjectCount;
    
    /**
     * The new StateUnserializer will read 'in', using 'maker' as the
     * source of authority for making various objects.
     */
    /*package*/ StateUnserializer(StateInputStream in) {
        myIn = in;
        myObjectCount = 0;
        myIn.setMyInterest(this);
    }

    /**
     * Create an StateUnserializer, used instead of a constructor.
     *
     * @param in the StateInputStream to read for input
     * @exception IOException thrown if some problem reading the first byte of
     * stream
     */
    static public StateUnserializer make(StateInputStream in) throws IOException
    {
      int firstbyte = in.read();
      if (firstbyte != StateSerializer.SERIAL_ID) {
          throw new IOException
            ("Wrong ID code in Serial stream.  code=" + firstbyte);
      }
      return new StateUnserializer(in);
    }

    /**
     * Decode the entire graph of objects from input stream
     * @return Object that is the result of the read
     * @exception IOException thrown if some problem reading object
     * @exception ClassNotFoundException if some class is needed for
     * deserialization that cannot actually be read
     */
    public Object decodeGraph() throws IOException, ClassNotFoundException {
             return decodeObject();
    }        
 
    /**
     * Decode the entire graph of objects from input stream
     * @return Object that is the result of the read
     * @exception IOException thrown if some problem reading object
     * @exception ClassNotFoundException if some class is needed for
     * deserialization that cannot actually be read
     */
    public Object decodeObject() throws IOException, ClassNotFoundException {
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.decodeObject");
      return myIn.readObject();
    }
        
    /**
     * Read a string from our input stream
     *
     * @return String read from input stream
     * @exception IOException thrown if some problem reading
     */ 
    public String readUTF() throws IOException  {
      String retValue = myIn.readUTF();
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.readUTF with String "+retValue);
      return retValue;
    }
    
    /**
     * Close our input stream
     *
     * @exception IOException thrown if some problem closing our input stream
     */
    public void close() throws IOException  {
      myIn.close();
    }
    
    /**
     * Read a byte array from our input stream
     *
     * @param buf the byte [] buffer to read the data into
     * @param start the spot in the buffer start putting the data read
     * @param length the number of bytes to read from the stream
     * @return int number of bytes actually read
     * @exception IOException thrown if some problem reading
     * @see InputStream#read
     */
    public int read(byte buf[], int start, int length) throws IOException  {
      int retValue = myIn.read(buf, start, length);
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.read with value "+retValue);
      return retValue;
    }
    
    /**
     * Read a single line of data from the input stream
     *
     * @return String read from input stream
     * @exception IOException thrown if some problem reading
     * @see InputStream#readLine
     */
    public String readLine() throws IOException  {
      String retValue = myIn.readLine();
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.readLine with String "+retValue);
      return retValue;
    }
    
    // Implementation of StateObjectReadInterest interface
    /**
     * This method is called by the StateInputStream when a given object
     * is actually read from the input stream given as first parameter
     *
     * @param stream the StateInputStream that is calling this method on us
     * @param obj the Object that was read from input stream
     */
    public void objectToBeRead(StateInputStream stream, Object obj)  {
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.objectToBeRead with object "+obj);
      myObjectCount++;
    }
    
   /**
    * Return our count of objects read from the input stream
    *
    * @return int count of number of objects known to have been read from
    * our input stream
    */
    public int getObjectCount()  {
      return myObjectCount;
    }
}
