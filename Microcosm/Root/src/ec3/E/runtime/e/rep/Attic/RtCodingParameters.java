package ec.e.rep;

import ec.util.PEHashtable;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;

/** An RtEncodingParameters object is a collection of Objects (as
  keys) and token objects (typically Strings) as values. Encode()
  and repository.put() accept these as an extra argument and when
  they encode objects, any object that can be found in the
  parameters collection gets replaced by its corresponding token,
  effectively pruning the object graph at that point.
  */

public class RtEncodingParameters {
    private PEHashtable htab;
    
    /** Creates a hashtable-like container for encoding parameters of
      default size (20). Use this as a parameters argument to
      RtStandardEncoder() and other Encoder constructors. */
    
    public RtEncodingParameters() {
        htab = new PEHashtable(20);
    }
    
    /** Creates a hashtable-like container for encoding parameters of
      a given size. Use this as a parameters argument to
      RtStandardEncoder() and other Encoder constructors. */
    
    public RtEncodingParameters(int size) {
        htab = new PEHashtable(size);
    }
    
    /** Works just like for Hashtable instances */
    public Object put(Object key, Object value) throws NullPointerException {
        return(htab.put(key,value));
    }
    
    /** Works just like for Hashtable instances */
    public Object get(Object key) {
        return(htab.get(key));
    }
    
    /** Works just like for Hashtable instances */
    public int size() {
        return(htab.size());
    }
    
    /** Works just like for Hashtable instances */
    public Enumeration keys() {
        return(htab.keys());
    }
    
    /** Works just like for Hashtable instances */
    public Enumeration elements() {
        return(htab.elements());
    }
    
    /** Works just like for Hashtable instances */
    public Object remove(Object key) {
        return(htab.remove(key));
    }
    
    /** Works just like for Hashtable instances */
    public boolean isEmpty() {
        return(htab.isEmpty());
    }
}

/** RtDecodingParameters tables are used as an argument to
  RtStandardDecoder() and other Decoder constructors. Whenever that
  decoder encounters a parameter object in the stream, it will look
  up the parameter iin the RtDecodingParameters table and if found,
  will substitute (return) the object in the table as the result of
  the decode. <p>
  
  Note that you normally want to switch keys/values are around
  compared to RtEncodingParameters - Your values when encoding
  become keys when decoding and vice versa. */

public class RtDecodingParameters {
    
    private Hashtable htab;
    
    /** Creates a hashtable-like container for decoding parameters of
      default size (20).
      */
    
    public RtDecodingParameters() {
        htab = new Hashtable(20);
    }
    
    /** Creates a hashtable-like container for encoding parameters of a
      given size. Use this as a parameters argument to RtStandardDecoder()
      and other Decoder constructors. */
    
    public RtDecodingParameters(int size) {
        htab = new Hashtable(size);
    }
    
    /** Works just like for Hashtable instances */
    public Object put(Object key, Object value) throws NullPointerException {
        return(htab.put(key,value));
    }
    
    /** Works just like for Hashtable instances */
    public Object get(Object key) {
        return(htab.get(key));
    }
    
    /** Works just like for Hashtable instances */
    public int size() {
        return(htab.size());
    }
    
    /** Works just like for Hashtable instances */
    public Enumeration keys() {
        return(htab.keys());
    }
    
    /** Works just like for Hashtable instances */
    public Enumeration elements() {
        return(htab.elements());
    }
    
    /** Works just like for Hashtable instances */
    public Object remove(Object key) {
        return(htab.remove(key));
    }
    
    /** Works just like for Hashtable instances */
    public boolean isEmpty() {
        return(htab.isEmpty());
    }
}
