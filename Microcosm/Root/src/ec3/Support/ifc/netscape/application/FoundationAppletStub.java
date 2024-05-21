// FoundationAppletStub.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;
import java.net.*;
import java.applet.*;
import java.io.*;

class FoundationAppletStub implements AppletStub, AppletContext {
    URL documentBase;
    URL codeBase;

    public boolean isActive() {
        return true;
    }

    public URL getDocumentBase() {
        if (documentBase == null) {
            try {
                documentBase = new URL("file:" +
System.getProperty("user.dir").replace(File.separatorChar, '/') + "/");
            } catch (Exception e) {
            }
        }
        return documentBase;
    }

    public URL getCodeBase() {
        if (codeBase == null) {
            try {
                codeBase = new URL("file:" +
System.getProperty("user.dir").replace(File.separatorChar, '/') + "/");
            } catch (Exception e) {
            }
        }
        return codeBase;
    }

    public String getParameter(String name) {
        return null;
    }

    public AppletContext getAppletContext() {
        return this;
    }

    public void appletResize(int width, int height) {
    }

    public AudioClip getAudioClip(URL url) {
        return null;
    }

    public java.awt.Image getImage(URL url) {
        return AWTCompatibility.awtToolkit().getImage(url);
    }

    public Applet getApplet(String name) {
        return null;
    }

    public java.util.Enumeration getApplets() {
        java.util.Vector applets = new java.util.Vector();
        applets.addElement(AWTCompatibility.awtApplet());
        return applets.elements();
    }

    public void showDocument(URL url) {
    }

    public void showDocument(URL url, String target) {
    }

    public void showStatus(String status) {
    }
}
