package ec.e.serial;

import ec.e.file.EStdio;
import java.util.Vector;
import java.util.Enumeration;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

import ec.util.NestedException;
/**
 * @see ec.e.serial.StateSerializer.java
 */
public class StateUnserializer implements StateObjectReadInterest {

    static private final Trace tr = new Trace("ec.e.serial.StateUnserializer");

         private StateInputStream myIn;
         private int myObjectCount;
    
    /**
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
     * @param in;
     */
    static public StateUnserializer make(StateInputStream in) throws IOException
    {
            int firstbyte = in.read();
            if (firstbyte != Serializer.SERIAL_ID) {
                throw new IOException
                  ("Wrong ID code in Serial stream.  code=" + firstbyte);
            }
            return new StateUnserializer(in);
    }

    public Object decodeGraph() throws IOException, ClassNotFoundException {
             return decodeObject();
    }

    public Object decodeObject() throws IOException, ClassNotFoundException {
        if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.decodeObject");
             return myIn.readObject();
    }
         
         public String readUTF() throws IOException  {
      String retValue = myIn.readUTF();
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.readUTF with String "+retValue);
           return retValue;
         }
         
         public void close() throws IOException  {
           myIn.close();
         }
    
    public int read(byte buf[], int start, int length) throws IOException  {
      int retValue = myIn.read(buf, start, length);
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.read with value "+retValue);
      return retValue;
    }
    
    public String readLine() throws IOException  {
      String retValue = myIn.readLine();
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.readLine with String "+retValue);
      return retValue;
    }
    
    // Implementation of StateObjectReadInterest
    public void objectToBeRead(StateInputStream stream, Object obj)  {
      if (tr.debug && Trace.ON) tr.debugm("StateUnserializer.objectToBeRead with object "+obj);
      myObjectCount++;
      // XXX more here?
    }
    
    public int getObjectCount()  {
      return myObjectCount;
    }
}
