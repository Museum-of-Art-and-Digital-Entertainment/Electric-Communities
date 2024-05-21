 // Copyright 1997 Electric Communities. All rights reserved worldwide.

package ec.tools.upgrade;

import java.io.InputStream;

/**
 * Describe an element of a path.
 */

public class PathElement {

    /**
     * The directory/zip/jar file which contains the file.
     */
    public final String path;

    /**
     * The file name within the directory/zip/jar file.
     */
    public final String file;

    /**
     * An input stream opened on file.  N.B. The caller should close this
     * stream to avoid file exceptions when certain implementations of the
     * Java Virtual Machine run out of file handles.
     */
    public final InputStream data;
    
    public PathElement(String pathElement, String fileElement, 
                       InputStream inputStream) {
        path = pathElement;
        file = fileElement;
        data = inputStream;
    }

    public String toString() {
        return "PathElement for path: "+path+" file: "+file;
    }
}
        