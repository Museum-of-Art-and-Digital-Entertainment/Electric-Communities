package ec.ui;

import netscape.application.*;
import ec.ifc.app.*;

public class InspectableObjectPopup extends Popup {

  InspectableObjectPopup(int x, int y, int width, int height) {
    super(x,y,width,height);
  }

  InspectableObjectPopup(Rect r) {
    super(r);
  }

  protected void layoutPopupWindow() {

    // The next code line confuses E. Therefore this file is compiled with
    // javac instead of javaec.

    ((IFCInspectorUI)target()).refreshObjectDirectory();
    super.layoutPopupWindow();
  }
}

