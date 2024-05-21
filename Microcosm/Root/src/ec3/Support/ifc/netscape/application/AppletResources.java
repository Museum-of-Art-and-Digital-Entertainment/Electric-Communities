// AppletResources.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

import java.net.URL;
import java.io.InputStream;


/** An AppletResources instance locates and manages an Application's
  * resources (images, sounds, and so on). You never access an AppletResources
  * instance directly.  This class will be replaced by a more
  * general resource manager.
  */

class AppletResources {
    Application         _applet;
    URL                 _baseURL;
    String              _subdirectory = "";

    static final String         CONTENTS = ".contents",
                                LANGUAGES = "Languages",
                                INTERFACES_DIRECTORY = "interfaces",
                                IMAGE_DIRECTORY = "images",
                                FONT_DIRECTORY = "fonts",
                                SOUND_DIRECTORY = "sounds";


/* constructors */

    public AppletResources(Application applet, java.net.URL baseURL) {
        _applet = applet;
        _baseURL = baseURL;
    }

/* actions */


    public void setBaseURL(java.net.URL baseURL) {
        _baseURL = baseURL;
        // _contents = null;
    }

    public URL baseURL() {
        return _baseURL;
    }

    public void setSubdirectory(String subdirectoryName) {
        _subdirectory = subdirectoryName;
        if (_subdirectory == null) {
            _subdirectory = "";
        }
    }

    public String subdirectory() {
        return _subdirectory;
    }

    public Vector availableLanguages() {
        return null;
    }

    private java.net.URL _urlFromBaseAndPath(URL baseURL, String path) {
        URL     documentURL;

        try {
            documentURL = new URL(baseURL, path);
        } catch (Exception e) {
            System.err.println("appletResources._urlFromBaseAndPath() - " +
                               e);
            documentURL = null;
        }

        return documentURL;
    }

    public Vector URLsForResource(String resourceName) {
        Vector       urlVector, availableLanguages, languagePreferences;
        String       subDir;
        int          i, count;

        urlVector = new Vector();
        subDir = _subdirectory;
        if (subDir.length() > 0 && subDir.charAt(subDir.length() - 1) != '/') {
            subDir = subDir + "/";
        }

        languagePreferences = _applet.languagePreferences();
        availableLanguages = availableLanguages();

      /* if no language vector provided, have to stat each one */
        if (availableLanguages == null) {
            count = languagePreferences.count();
            for (i = 0; i < count; i++) {
                urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                               (String)languagePreferences.elementAt(i) +
                                     ".pkg/" + subDir + resourceName));
            }
            urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                                                    subDir + resourceName));

            return urlVector;
        }

      /* if language Vector is empty, everything's at the top level */
        if (availableLanguages.isEmpty()) {
            urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                                                    subDir + resourceName));

            return urlVector;
        }

      /* no preferred order, so take the default */
        if (languagePreferences.isEmpty()) {
            count = availableLanguages.count();
            for (i = 0; i < count; i++) {
                urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                            (String)availableLanguages.elementAt(i) + ".pkg/" +
                                     subDir + resourceName));
            }
            urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                                                    subDir + resourceName));

            return urlVector;
        }

      /* try the ones we want first */
        count = languagePreferences.count();
        for (i = 0; i < count; i++) {
            if (availableLanguages.contains(
                                        languagePreferences.elementAt(i))) {
                urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                               (String)languagePreferences.elementAt(i) +
                                     ".pkg/" + subDir + resourceName));
            }
        }

      /* then the rest */
        count = availableLanguages.count();
        for (i = 0; i < count; i++) {
            if (!languagePreferences.contains(
                                        availableLanguages.elementAt(i))) {
                urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                                     (String)availableLanguages.elementAt(i) +
                                     ".pkg/" + subDir + resourceName));
            }
        }

      /* then the default */
        urlVector.addElement(_urlFromBaseAndPath(_baseURL,
                                                subDir + resourceName));

        return urlVector;
    }


/* possible URLs for a given resource name */

    public Vector URLsForInterface(String interfaceName) {
        return URLsForResource(INTERFACES_DIRECTORY + "/" + interfaceName);
    }

    public Vector URLsForImage(String imageName) {
        return URLsForResource(IMAGE_DIRECTORY + "/" + imageName);
    }

    public Vector URLsForFont(String fontName) {
        return URLsForResource(FONT_DIRECTORY + "/" + fontName);
    }

    public Vector URLsForSound(String soundName) {
        return URLsForResource(SOUND_DIRECTORY + "/" + soundName);
    }

    public Vector URLsForResourceOfType(String resourceName, String type) {
        return URLsForResource(type + "/" + resourceName);
    }



  /* convenience methods */

    public InputStream streamForURLs(Vector urlVector) {
  /* awt */
        URL             documentURL;
        InputStream     inputStream;
        int             count, i;

        if (urlVector == null || urlVector.isEmpty()) {
            return null;
        }

        count = urlVector.count();
        for (i = 0; i < count; i++) {
            try {
                documentURL = (URL)urlVector.elementAt(i);
                inputStream = documentURL.openStream();
                return inputStream;
            } catch (Exception e) {
            }
        }

        return null;
    }

    public InputStream streamForInterface(String interfaceName) {
        return streamForURLs(URLsForInterface(interfaceName));
    }

    URL urlForBitmapNamed(String bitmapName) {
        Vector urlVector;

        urlVector = URLsForImage(bitmapName);
        if (urlVector.count() > 0)
            return (URL)urlVector.elementAt(0);

        return null;
    }

    URL urlForSoundNamed(String soundName) {
        Vector urlVector;

        urlVector = URLsForSound(soundName);
        if (urlVector.count() > 0)
            return (URL)urlVector.elementAt(0);

        return null;
    }

    URL urlForFontNamed(String fontName) {
        Vector urlVector;

        urlVector = URLsForFont(fontName);
        if (urlVector.count() > 0)
            return (URL)urlVector.elementAt(0);

        return null;
    }

    public InputStream streamForResourceOfType(String resourceName,
                                               String type) {
        return streamForURLs(URLsForResourceOfType(resourceName, type));
    }
}
