/*
  EARL -- Class to represent a human-readible reference to an E-object.
*/
package ec.e.net;

import ec.util.NestedException;

/**
 * A parsed URL designating an E object.
 */
public class EARL {
    private static final Trace tr = new Trace("ec.e.net.EARL");
    
    private String myURL;
    private String[] mySearchPath;
    private String myRegistrarID;
    private String myObjectID;
    private String myExpiration;

    /**
     * Construct an EARL given the URL string.
     *
     * @param url A URL of the form "e://<searchpath>/<registrarID>/<objectID>"
     */
    public EARL(String url) throws InvalidURLException {
        myURL = url;
        parseEARL(url);
    }

    /**
     * Construct an EARL given the components of the URL.
     *
     * @param searchPath The search path, a semi-colon separated list of domain
     *   names and/or IP addresses.
     * @param registrarID The registrarID for the object, a base-36 encoded
     *   public key fingerprint.
     * @param objectID The objectID for the object, a base-36 encoded Swiss
     *   number.
     * @param expiration The registration expiration date
     */
    public EARL(String searchPath[], String registrarID, String objectID,
                String expiration) {
        mySearchPath = searchPath;
        myRegistrarID = registrarID;
        myObjectID = objectID;
        if (expiration != null && expiration.equals("")) {
            expiration = null;
        }
        myExpiration = expiration;
        String flattenedSearchPath = flattenSearchPath(mySearchPath);
        myURL = "e://" + flattenedSearchPath + "/" + registrarID + "/" + objectID;
        if (expiration != null) {
            myURL += ":" + expiration;
        }
    }

    /**
     * Return the expiration date string for this EARL.
     */
    public String expiration() {
        return myExpiration;
    }

    /**
     * Return the objectID string for this EARL.
     */
    public String objectID() {
        return myObjectID;
    }

    /**
     * Return the registrarID string for this EARL.
     */
    public String registrarID() {
        return myRegistrarID;
    }

    /**
     * Return the search path for this EARL.
     *
     * @returns An array of strings, one element for each search path member
     */
    public String[] searchPath() {
        return mySearchPath;
    }

    /**
     * Return the fully-composed URL string for this EARL.
     */
    public String url() {
        return myURL;
    }

    /** A regular expression for parsing an EARL URL string. */
    /*
    static private RegularExpression reEARL;//would be final if compiler let us
    static {
        String protocolPattern    = "^[eE]:";
        String searchPathPattern  = "\\([^/]*\\)";
        String registrarIDPattern = "\\([^/]*\\)";
        String objectIDPattern    = "\\([^/:]*\\)";
        /* XXX This version does not do expiration dates */
    /*
        reEARL = new RegularExpression(protocolPattern    + "//" +
                                       searchPathPattern  + "/"  +
                                       registrarIDPattern + "/"  +
                                       objectIDPattern);
    }
    */

    /**
     * Parse a URL into its constituent elements, saving them as our own.
     *
     * @param url The URL string to be parsed.
     */
    private void parseEARL(String url) throws InvalidURLException {
        if (tr.debug && Trace.ON) tr.$("parseEarl(" + url + ")");
        if (url == null) {
            throw new InvalidURLException("null URL");
        }
        if (!url.startsWith("e://") && !url.startsWith("E://")) {
            throw new InvalidURLException("URL "+url+" does not start with e://");
        }
        String rest = url.substring(4);
        int i = rest.indexOf('/');
        if (i<1) {
            throw new InvalidURLException("URL "+url+" does not have a search path");
        }
        String path = rest.substring(0, i);
        rest = rest.substring(i+1);
        mySearchPath = parseSearchPath(path); 

        i = rest.indexOf('/');
        if (i<1) {
            throw new InvalidURLException("URL "+url+" does not have a registrarID");
        }
        myRegistrarID = rest.substring(0, i);
        rest = rest.substring(i+1);

        i = rest.indexOf('/');
        if (i<0) i = rest.indexOf(':');
        if (i<0) i = rest.length();
        if (i<1) {
            throw new InvalidURLException("URL "+url+" does not have an objectID");
        }
        myObjectID = rest.substring(0, i);

        myExpiration  = ""; // myExpiration  = reEARL.SubMatch(5);
        if (myExpiration.equals("")) {
            myExpiration = null;
        }
        if (tr.debug && Trace.ON) {
            for (i=0; i<mySearchPath.length; i++) {
                tr.debugm("mySearchPath["+i+"]="+mySearchPath[i]);
            }
            tr.debugm("myRegistrarID="+myRegistrarID+" myObjectID="+myObjectID);
        }
    }

    /**
     * Parse a (semicolon-separated) search path into its constituent elements
     *
     * @param path The search path to be parsed
     * @returns An array of Strings containing the elements of the search path
     */
    static public String[] parseSearchPath(String path) {
        int count;
        int start;
        int end;
        String result[];

        if (tr.debug && Trace.ON) tr.debugm("parsing " + path);
        if (path == null || path.length() == 0)
            return new String[0];
        count = 1;
        start = -1;
        while ((start = path.indexOf(';', start+1)) >= 0)
            count++;
        result = new String[count];
        start = 0;
        for (int i=0; i<count-1; i++) {
            end = path.indexOf(';', start);
            result[i] = path.substring(start, end);
            if (tr.debug && Trace.ON) tr.debugm("found " + result[i]);
            start = end + 1;
        }
        result[count-1] = path.substring(start);
        if (tr.debug && Trace.ON) tr.debugm("found " + result[count-1]);
        return result;
    }

    String flattenSearchPath(String path[]) {
        String ret = "" ;
        if (path == null || path.length == 0) {
            return ret; // could be reasonable to throw an exception here...
        }
        
        int i = 0;
        while (i < path.length - 1) {
            ret += path[i++] + ";" ;
        }
        ret += path[i] ;
        return ret;
    }
}

public class InvalidURLException extends NestedException {
    InvalidURLException(String msg) {
        super(msg);
    }
    InvalidURLException(String msg, Throwable t) {
        super(msg, t);
    }
}

