package ec.ui;


import netscape.application.*;


/**
 * Hack to get round problem that E programs crash if they access static final
 * string constants in Java code.
 * We want access to the attribute names in the IFC TextView class, and they
 * are static final string constants. To avoid a crash we wrap the constants
 * in methods.
 * This file should only be compiled with javac, not ecomp. If you use ecomp
 * the wrapper methods will crash!
 */
public class IFCTextViewConstants {

  public static java.lang.String getCaretColorKey() {
    return TextView.CARET_COLOR_KEY;
  }


  public static java.lang.String getFontKey() {
    return TextView.FONT_KEY;
  }


  public static java.lang.String getLinkColorKey() {
    return TextView.LINK_COLOR_KEY;
  }


  public static java.lang.String getLinkKey() {
    return TextView.LINK_KEY;
  }


  public static java.lang.String getParagraphFormatKey() {
    return TextView.PARAGRAPH_FORMAT_KEY;
  }


  public static java.lang.String getPressedLinkColorKey() {
    return TextView.PRESSED_LINK_COLOR_KEY;
  }


  public static java.lang.String getTextAttachmentKey() {
    return TextView.TEXT_ATTACHMENT_KEY;
  }


  public static java.lang.String getTextAttachmentString() {
    return TextView.TEXT_ATTACHMENT_STRING;
  }


  public static java.lang.String getTextColorKey() {
    return TextView.TEXT_COLOR_KEY;
  }

}
