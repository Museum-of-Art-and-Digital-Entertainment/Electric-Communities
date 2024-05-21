package ec.e.db;
import java.lang.Class;
import java.lang.Object;

/** This interface contains methods that contextually manage decoding
  objects. */

public interface RtDecodingManager {
    Object decodeObject(Class theClass, RtDecoder coder, int objectId);
    
    /** Method to implement a policy for what to do when a decode() call
      discovers a parameter object in the stream but the
      RtDecodingParameters hashtable is null (i.e. was not given to
      decode()) or does not contain the parameter object in question. In
      both cases this handleMissingParameter() method is called to resolve
      the problem and is expected to return an object to replace the
      parameter object.
      
      @param paramObj - A parameter object that was decoded from the data
      stream but that could not be found in the RtDecodingParameters table,
      if any.
      
      @param parameters - The parameter table given to decode(), or null, if
      none was given.
      
      @return Result - an object that resolves the missing parameter.
      
      @Note It is completely acceptable to throw an RtDecodingException;
      this is in fact the default behavior in RtDecodingmanagerDefault.
      
      */
    
    Object handleMissingParameter(Object paramObj,
                                  RtDecodingParameters parameters)
    throws RtDecodingException;
    
    Object getUniqueObject(int id);
        
    void uniqueImportedObject(Object object, int id);
}
