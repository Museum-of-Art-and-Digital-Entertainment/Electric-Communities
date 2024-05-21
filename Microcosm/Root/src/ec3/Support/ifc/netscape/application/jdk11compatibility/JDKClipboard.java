// JDKClipboard.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application.jdk11compatibility;

import netscape.application.*;
import java.awt.Toolkit;
import java.awt.datatransfer.*;

public class JDKClipboard implements netscape.application.Clipboard {

    public JDKClipboard() throws InstantiationException{
        try {
            AWTCompatibility.awtToolkit().getSystemClipboard();
        } catch (NoSuchMethodError e) {
            throw new InstantiationException("Wrong AWT version");
        }
    }

    synchronized public void setText(String text) {
        java.awt.datatransfer.Clipboard clipboard = AWTCompatibility.awtToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(text);

        clipboard.setContents(stringSelection, null);
    }

    synchronized public String text() {
        java.awt.datatransfer.Clipboard clipboard = AWTCompatibility.awtToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);
        String text = null;

        try {
            text = (String)transferable.getTransferData(DataFlavor.stringFlavor);
            text = TextView.stringWithoutCarriageReturns(text);
        } catch (Exception e) {
        }
        return text;
    }
}
