import java.util.Properties;
import java.util.Hashtable;
import java.util.Random;

import ec.util.Native;

public class Message {
     public long startTime;
     public byte[] buffer; 
     public Hashtable hashtable;
     public int mySize;
     public int myNumber;

     public Message(int size, int num) {
          buffer = new byte[size];
          myNumber = num;
          hashtable = new Hashtable(10);
          hashtable.put("foo", "foo");
          hashtable.put("foo2", new Integer(2));
          hashtable.put("foo3", "foo3");

          startTime = Native.queryTimer();
          mySize = size;
     }
     public String toString() {
         return "" + myNumber;
     }
}
