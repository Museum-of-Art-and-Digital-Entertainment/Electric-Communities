package ec.e.db;

/** Objects that implement the RtEncodingManager interface are
  declaring they will handle various encoding policy decisions. */

public interface RtEncodingManager {
    
    /** Overrides the class name for an object.
      
      By returning a non-null String to be used as a class name,
      willEncodeObjectAsClass() can claim an object is of a different
      class than it really is. if the manager does this, then it will
      subsequently be asked to actually encode the object using
      encodeObject() below. The trivial default encoder returns null,
      deferring to normal encode behavior. */
    
    String willEncodeObjectAsClass(Object obj);
    
    /** Overrides encoding an object.
      
      If the class name string returned from willEncodeObjectAsClass()
      is non-null, then the manager will be asked to encode the object
      using this method.  In this trivial default encoding manager,
      this method is not called. */
    
    void encodeObject(Object obj, RtEncoder coder);
    
    /** Overrides a type identifier for an object.
      
      If this method returns a nonzero integer then that number becomes
      a type identifier to be used for this (kind of) object. This
      manager returns 0, which instructs the encoder to use its own
      mechanisms. */
    
    int idForUniqueExportedObject(Object object);
    
    /** Returns an integer to use as an identifier for an object.
      
      If you returned a nonzero identifier from
      idForUniqueExportedObject() above, then this method gets called,
      requesting an object identifier (an integer) for this object. You
      can use this (e.g.) to maintain your own unique object
      identification scheme for objects of that type, or cause all
      objects of this type to decode as a known canonical (shared)
      instance. */
    
    int uniqueExportedObject(Object object);
    
    /** Callback to encoding manager whenever a class is
      encountered for the first time.
      
      This method gives the encoding manager a chance to keep track of
      all class types.  The trivial default encoder relies on the
      regular encoding mechanism and does nothing. */
    
    void noteNewClass(String className, int classCode);
}
