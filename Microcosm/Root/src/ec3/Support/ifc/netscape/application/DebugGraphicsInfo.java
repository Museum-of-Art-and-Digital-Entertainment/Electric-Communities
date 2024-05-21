// DebugGraphicsInfo.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

class DebugGraphicsInfo {
    ExternalWindow       debugWindow;
    Color                flashColor = Color.red;
    int                  flashTime = 100;
    int                  flashCount = 2;
    Hashtable            viewToDebug;
    java.io.PrintStream  stream = System.out;

    void setViewDebug(View view, int debug) {
        if (viewToDebug == null) {
            viewToDebug = new Hashtable();
        }
        viewToDebug.put(view, new Integer(debug));
    }

    int viewDebug(View view) {
        if (viewToDebug == null) {
            return 0;
        } else {
            Integer integer = (Integer)viewToDebug.get(view);

            return integer == null ? 0 : integer.intValue();
        }
    }

    void log(String string) {
        stream.println(string);
    }
}


