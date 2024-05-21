// Autoscroller.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;


class Autoscroller implements Target {
    private MouseEvent _event;

    public void performCommand(String command, Object data) {
        View    mouseView;

        mouseView = (View)data;
        mouseView.mouseDragged(
            mouseView.rootView().convertEventToView(mouseView, _event));
    }

    public void setEvent(MouseEvent event) {
        _event = event;
    }
}
