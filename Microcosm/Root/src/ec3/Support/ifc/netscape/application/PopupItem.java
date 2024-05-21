// PopupItem.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;

import netscape.util.*;

/** ListItem subclass representing a single entry in the ListView maintained by
  * a Popup.
  * @see ListView
  */

public class PopupItem extends ListItem {
    Popup   popup;

    final static String       POPUP_KEY = "popup";


    /** Constructs an empty PopupItem.
      */
    public PopupItem() {
        super();
    }

    /** Sets the Popup that maintains this PopupItem.
      */
    public void setPopup(Popup aPopup) {
        popup = aPopup;
    }

    /** Returns the Popup that maintains this PopupItem.
      * @see #setPopup
      */
    public Popup popup() {
        return popup;
    }

    /** Called by ListView to draw the PopupItem.
      */
    public void drawInRect(Graphics g, Rect boundsRect) {
        int        width, height;
        Image      popupImage;
        PopupItem  selectedItem;

        super.drawInRect(g, boundsRect);

        if (popup != null) {
            selectedItem = (PopupItem)popup.selectedItem();
            if (selectedItem.equals(this)) {
                popupImage = popup.popupImage();

                if (popupImage != null) {
                    width = popupImage.width();
                    height = popupImage.height();
                } else {
                    width = height = 0;
                }

                if (selected) {
                    g.setColor(selectedColor);
                } else {
                    g.setColor(listView.backgroundColor());
                }

                g.fillRect(boundsRect.x + boundsRect.width - width - 4,
                           boundsRect.y, width + 4, boundsRect.height);

                if (popupImage != null) {
                    popupImage.drawAt(g, boundsRect.x + boundsRect.width -
                                      width - 2,
                                      boundsRect.y +
                                      (boundsRect.height - height)/2);
                }
            }
        }
    }

    /** Describes the PopupItem class' information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        super.describeClassInfo(info);
        info.addClass("netscape.application.PopupItem", 1);
        info.addField(POPUP_KEY, OBJECT_TYPE);
    }

    /** Archives the PopupItem instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        super.encode(encoder);
        encoder.encodeObject(POPUP_KEY, popup);
    }

    /** Unarchives the PopupItem instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        super.decode(decoder);
        popup = (Popup)decoder.decodeObject(POPUP_KEY);
    }
}
