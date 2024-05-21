// TextBag.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

class TextBag extends Object implements Clipboard {
    String text;

    synchronized public void setText(String text) {
        this.text = text;
    }

    synchronized public String text() {
        return text;
    }
}
