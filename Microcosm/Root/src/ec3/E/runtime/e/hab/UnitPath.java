package ec.e.hab;
import java.util.Enumeration;
import ec.e.util.ArrayEnumeration;

/**
  A "pre-digested" pathname. Pathnames are represented externally by Java-style
  FQN strings (e.g., "foo.bar.baz") and internally by UnitPath objects.
*/
public class UnitPath {

    /**
    * The actual path elements
    */
    private String path[];

    /**
    * Construct a UnitPath by parsing an FQN string.
    *
    * @param pathString The FQN string to be parsed.
    */
    public UnitPath(String pathString) {
        int length = countPathElems(pathString);
        path = new String[length];
        for (int i=0; i<length; ++i) {
            path[i] = pathStringHead(pathString);
            pathString = pathStringTail(pathString);
        }
    }

    /**
    * Construct a UnitPath from a pre-parsed FQN string.
    *
    * @param path An array of path elements.
    */
    public UnitPath(String path[]) {
        this.path = path;
    }

    /**
    * Construct a UnitPath by concatenating an FQN string to be parsed onto
    * an earlier UnitPath.
    *
    * @param basePath Another UnitPath to base upon
    * @param pathString An FQN string to parse and concatenate.
    */
    public UnitPath(UnitPath basePath, String pathString) {
        int length = basePath.path.length + countPathElems(pathString);;
        path = new String[length];
        for (int i=0; i<basePath.path.length; ++i)
            path[i] = basePath.path[i];
        for (int i=basePath.path.length; i<length; ++i) {
            path[i] = pathStringHead(pathString);
            pathString = pathStringTail(pathString);
        }
    }

    /**
    * Return a parent path for this path, or null if there is no parent.
    */
    public UnitPath parent() {
        if (path.length == 1) {
            return(null);
        } else {
            String newPath[] = new String[path.length - 1];
            System.arraycopy(path, 0, newPath, 0, newPath.length);
            return(new UnitPath(newPath));
        }
    }

    /**
    * Return the last element in the pathname of the path we are holding onto.
    */
    public String childName() {
        return(path[path.length - 1]);
    }

    /**
    * Enumerate the elements of the path.
    *
    * @return An Enumeration over the path elements.
    */
    public Enumeration elements() {
        return(new ArrayEnumeration(path));
    }

    /**
    * Produce a nicely readable String representation of the path.
    *
    * @return An FQN String, suitable for printing.
    */
    public String toString() {
        Enumeration elems = this.elements();
        String result = (String) elems.nextElement();
        while (elems.hasMoreElements())
            result += "." + (String) elems.nextElement();
        return(result);
    }

    /**
    * Extract the first element from a path string. This is an internal method
    * for parsing path strings.
    *
    * @param pathString An FQN path string.
    * @return The first element in the path.
    */
    private String pathStringHead(String pathString) {
        int delim = pathString.indexOf('.');
        if (delim == -1)
            return(pathString);
        else
            return(pathString.substring(0, delim));
    }

    /**
    * Extract the tail elements from a path string. This is an internal method
    * for parsing path strings.
    *
    * @param pathString An FQN path string.
    * @return The path string stripped of its first element.
    */
    private String pathStringTail(String pathString) {
        int delim = pathString.indexOf('.');
        if (delim == -1)
            return("");
        else
            return(pathString.substring(delim + 1));
    }

    /**
    * Count the elements in a path string. This is internal method for parsing
    * path strings.
    *
    * @param pathString An FQN path string.
    * @return The number of elements in the path string.
    */
    private int countPathElems(String pathString) {
        int count = 1;
        for (int i=0; i<pathString.length(); ++i) {
            char c = pathString.charAt(i);
            if (!Character.isLowerCase(c) && !Character.isUpperCase(c) &&
                    !Character.isDigit(c)) {
                if (c != '.' || i == 0 || i == pathString.length() - 1)
                    throw new HaberdasherException("bad unit path string \"" +
                                                   pathString + "\"");
                ++count;
            }
        }
        return(count);
    }
}
