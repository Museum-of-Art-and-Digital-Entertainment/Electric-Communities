package ec.e.file;

/**
* Common interface for directory entries that may be examined for file info.
*/
public interface EFileInfo {
    /**
    * Length in bytes of this file.
    */
    long length();

    /**
    * Determines the time that the file represented by this object was
    * last modified. As per the Java class library spec, the return value is
    * system-dependent and should only be used to compare with other values
    * returned by lastModified(). It should not be interpreted as an absolute
    * time.
    *
    * @return the "time" the file specified by this object was last modified
    */
    long lastModified();
}
