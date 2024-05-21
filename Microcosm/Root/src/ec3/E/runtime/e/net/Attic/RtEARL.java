/*
  RtEARL -- Class to represent a human-readible reference to an E-object.
*/
package ec.e.net;

import ec.regexp.RegularExpression;

public class RtEARL {
    private String myURL;
    private String mySearchPath;
    private String myRegistrarId;
    private String myObjectId;
    private String myExpiration;

    public RtEARL(String url) throws EInvalidUrlException {
        myURL = url;
        parseEARL(url);
    }

    public RtEARL(String searchPath, String registrarId, String objectId,
                  String expiration) {
        mySearchPath = searchPath;
        myRegistrarId = registrarId;
        myObjectId = objectId;
        if (expiration == "")
            expiration = null;
        myExpiration = expiration;
        if (expiration != null)
            myURL = "e://" + searchPath + "/" + registrarId + "/" + objectId +
                ":" + expiration;
        else
            myURL = "e://" + searchPath + "/" + registrarId + "/" + objectId;
    }

    public String getSearchPath() {
        return(mySearchPath);
    }

    public String getRegistrarId() {
        return(myRegistrarId);
    }

    public String getObjectId() {
        return(myObjectId);
    }

    public String getExpiration() {
        return(myExpiration);
    }

    public String getURL() {
        return(myURL);
    }

    /*static RegularExpression reEARL = new RegularExpression("^[ \t]*[eE]://\\([^/]*\\)/\\([^/]*\\)/\\([^/:]*\\)\\(:\\([^ \t]*\\)\\)\\{0,1\\}");*/

    static RegularExpression reEARL = new RegularExpression("^[eE]://\\([^/]*\\)/\\([^/]*\\)/\\([^/:]*\\)");

    /* "^[ \t]*[eE]://\\([^/]*\\)/\\([^/:]*\\)\\(:\\([^ \t]*\\)\\)\\{0,1\\}" */
    /* "^[ \t]*[eE]:// ( [^/]* ) / ( [^/:]* ) ( : ( [^ t]* ) ){0,1}" */

    private void parseEARL(String url) throws EInvalidUrlException {
        String path;

        if (reEARL.Match(url)) {
            mySearchPath = reEARL.SubMatch(1);
            myRegistrarId = reEARL.SubMatch(2);
            myObjectId = reEARL.SubMatch(3);
         // myExpiration = reEARL.SubMatch(5);
         // if (myExpiration == "")
            myExpiration = null;
        } else
            throw new EInvalidUrlException(url);
    }

    public static String[] ParseSearchPath(String path) {
        int count;
        int start;
        int end;
        String Path[];

        if (path == null || path.length() == 0)
            return(new String[0]);
        count = 1;
        start = -1;
        while ((start = path.indexOf(';', start+1)) >= 0)
            count++;
        Path = new String[count];
        start = 0;
        for (int i=0; i<count-1; i++) {
            end = path.indexOf(';', start);
            Path[i] = path.substring(start, end);
            start = end + 1;
        }
        Path[count-1] = path.substring(start);
        return(Path);
    }
}

public class EInvalidUrlException extends Exception {
    EInvalidUrlException(String msg) {
        super(msg);
    }
}
