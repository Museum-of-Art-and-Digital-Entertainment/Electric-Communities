// KeyboardArrow.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;


/** A private subclass of internal window for the keyboard arrow **/

class KeyboardArrow extends InternalWindow {
    Image image;
    View  view;

    public KeyboardArrow() {
        super(Window.BLANK_TYPE,0,0,0,0);
        setTransparent(true);
        setLayer(IGNORE_WINDOW_CLIPVIEW_LAYER+10);
        setCanBecomeMain(false);
    }

    public void setImage(Image anImage) {
        image = anImage;
        if(image != null) {
            sizeTo(image.width(),image.height());
        } else {
            sizeTo(0,0);
        }
    }

    public void drawView(Graphics g) {
        if(image!=null)
            image.drawAt(g,0,0);
    }

    public boolean mouseDown(MouseEvent event) {
        return false;
    }

    void setView(View aView) {
        view = aView;
    }

    View view() {
        return view;
    }

    public boolean canBecomeSelectedView() {
        return false;
    }
}
