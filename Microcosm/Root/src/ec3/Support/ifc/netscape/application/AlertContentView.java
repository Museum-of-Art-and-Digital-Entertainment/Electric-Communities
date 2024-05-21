// AlertContentView.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** Alert private class.
  */

class AlertContentView extends View implements Target {
    Alert alert;

    public AlertContentView(Alert anAlert,int x, int y, int w, int h) {
        super(x,y,w,h);
        alert = anAlert;
    }

    public void drawView(Graphics g) {
        g.setColor(Color.lightGray);
        g.fillRect(g.clipRect());
    }

    public void performCommand(String command, Object data) {
        if (Alert.DEFAULT_ACTION.equals(command))
            alert.setResult(Alert.DEFAULT_OPTION);
        else if (Alert.SECOND_ACTION.equals(command))
            alert.setResult(Alert.SECOND_OPTION);
        else if (Alert.THIRD_ACTION.equals(command))
            alert.setResult(Alert.THIRD_OPTION);
        else
            throw new NoSuchMethodError("unknown command: " + command);

        alert.hide();
    }
}

