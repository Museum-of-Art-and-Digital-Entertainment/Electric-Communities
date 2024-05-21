package ec.e.serial;

import ec.e.file.EStdio;
import java.io.IOException;

public class StateSerializer implements StateObjectWriteInterest {

    static private final Trace tr = new Trace("ec.e.serial.StateSerializer");

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
     * @param out;
     */
    static public StateSerializer make(StateOutputStream out)
        throws IOException
    {
        out.write(SERIAL_ID);
        return new StateSerializer(out);
    }

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
        // XXX get count of objects written and return
        return getObjectCount();
    }

    public void writeUTF(String aString) throws IOException {
      if (tr.debug && Trace.ON) tr.debugm("StateSerializer.writeUTF with String "+aString);
      myOut.writeUTF(aString);
    }
    
    public void write(byte buf[], int start, int length) throws IOException {
      if (tr.debug && Trace.ON) tr.debugm("StateSerializer.write with byte array "+buf);
      myOut.write(buf, start, length);
    }
    
    public void flush() throws IOException  {
      myOut.flush();
    }
    
    public void close() throws IOException  {
      myOut.close();
    }
    
    // Implementation of StateObjectWriteInterest
    public void objectToBeWritten(StateOutputStream stream, Object obj)  {
      if (tr.debug && Trace.ON) tr.debugm("StateSerializer.objectToBeWritten with object "+obj);
      myObjectCount++;
      // XXX more here?
    }
    
    public int getObjectCount()  {
      return myObjectCount;
    }
}
