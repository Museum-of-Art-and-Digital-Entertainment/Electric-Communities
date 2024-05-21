/**
 * ByteArrayURL.java - classes to support creating a URL connection with byte array
 *
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * Dima Nasledov
 *
 * Todo:
 *
 * Future: 
 */

package ec.e.rep.crew;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.MalformedURLException;
import java.util.Hashtable;

import ec.e.rep.steward.RepositoryKeyNotFoundException;

/**
 * This class allows to create a URL connection with the array of bytes
 */
public class ByteArrayURLConnection extends URLConnection {
    private String    myName = null;
    private byte[]    myBytes = null;

    /**
     * Constructor, takes a URL with "bytearray" protocol
     */
    public ByteArrayURLConnection(URL url) throws MalformedURLException
    {
        super(url);

        // strip the suffix of the URL name to use it as a hashtable key
        String urlName = url.toString();

        int limit = urlName.length();

        while (limit > 0) {
            int c = urlName.charAt(limit - 1);

            if ((c == ' ') || (c == '/')) {
                limit--;    //eliminate trailing whitespace or slashes
            }
            else {
                break;
            }
        }

        int start = 0;
        while ((start < limit) && (urlName.charAt(start) <= ' ')) {
            start++;    // eliminate leading whitespace
        }

        if (urlName.regionMatches(true, start, "url:", 0, 4)) {
            start += 4;
        }

        // sanity checking: is it really bytearray protocol?
        int i;
        for (i = start; i < limit; i++) {
            int c = urlName.charAt(i);
            if (c == '/') {
                String protocol = urlName.substring(start, i).toLowerCase();
                if (protocol.equals("bytearray:")) {
                    ++i;
                    break;
                }
                else {
                    throw new MalformedURLException(
                        "ByteArrayURLConnection has bad protocol name");
                }
            }
        }

        myName = urlName.substring(++i, limit);
        myBytes = CrewRepository.getImageBytes(myName);
    }

    /**
     * Does nothing here, dummy implementation of abstract method of the superclass
     */
    public void connect() throws IOException {}

    /**
     * Very important, won't work otherwise
     */
    public String getContentType() {
        if (myName != null) {
            return guessContentTypeFromName(myName);
        }
        else {
            return "content/unknown";
        }
    }

    /**
     * Create an InputStream out of the byte array, very important, won't work otherwise
     */
    public InputStream getInputStream() throws IOException {
        if (myName == null) {
            throw new IOException("ByteArrayURLConnection has no name set");
        }
        
        if (myBytes == null) {
            throw new IOException("ByteArrayURLConnection has no bytes set");
        }

        return new ByteArrayInputStream(myBytes);
    }
}

/**
 * ByteArrayHandler class is needed to create a handler for ByteArrayURLConnection.
 * URL will know how to deal with it with the help of ByteArrayFactory
 */
public class ByteArrayHandler extends URLStreamHandler {
    protected URLConnection openConnection(URL u) throws IOException {
        return new ByteArrayURLConnection(u);
    }
}

/**
 * ByteArrayFactory creates a ByteArrayHandler that *knows* how to deal
 * with "bytearray" protocol.  Important: need to call:
 * URL.setURLStreamHandlerFactory(new ByteArrayFactory()); from main!!!
 */
public class ByteArrayFactory implements URLStreamHandlerFactory {
    static Hashtable handlers = new Hashtable();

    public URLStreamHandler createURLStreamHandler(String protocol) {
        // try cached handlers first
        URLStreamHandler handler = (URLStreamHandler)handlers.get(protocol);

        if ((handler == null) && protocol.equals("bytearray")) {
            handler = new ByteArrayHandler();
        }

        if (handler != null) {
            handlers.put(protocol, handler); // put into cache
        }

        return handler;
    }
}
