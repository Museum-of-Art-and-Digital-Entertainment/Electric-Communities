// TextViewHTMLMarkerImp.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;
import java.net.*;

/**
  *  TextViewHTMLMarker's subclass to implement IMG,HR and BR
  *  @private
  */
public class TextViewHTMLMarkerImp extends TextViewHTMLMarker {

    TextAttachment textAttachmentCache;

    /**
     *  Compute the TextView attributes for the marker itself (the string
     *  returned by string() according to  context and initial attributes.
     *  Return the new attributes. The default implementation
     *  returns <b> initialAttributes</b>
     *  It is not necessary to allocate a new hashtable.You can just
     *  modify initialAttributes and return it.
     *  textView is the TextView for which the HTML is parsed.
     */
    public Hashtable attributesForMarker(Hashtable context,Hashtable initialAttributes,TextView textView) {
        if( marker.equals("HR") || marker.equals("IMG")) {
            Hashtable newAttr;
            String align;
            TextAttachment attachment;
            Range range;

            if( initialAttributes != null && initialAttributes.count() > 0) {
                Enumeration enumeration;
                Object key;

                newAttr = (Hashtable) TextView.hashtablePool.allocateObject();
                enumeration = initialAttributes.keys();
                while(enumeration.hasMoreElements() ) {
                    key = enumeration.nextElement();
                    newAttr.put(key,initialAttributes.get(key));
                }
            } else
                newAttr = (Hashtable)TextView.hashtablePool.allocateObject();

            attachment = textAttachment(textView.baseURL(),textView);
            newAttr.put(TextView.TEXT_ATTACHMENT_KEY, attachment);

            if( marker.equals("IMG")) {
                Hashtable attr;

                attr = hashtableForHTMLAttributes(attributes);

                if( attr != null && (align = (String) attr.get("ALIGN")) != null ) {
                    int baselineOffset=0;
                    if( align.equals("TOP") ) {
                        int ascent = 0;
                        FontMetrics fm;
                        Font currentFont = (Font)newAttr.get(TextView.FONT_KEY);

                        if( currentFont == null )
                            currentFont = (Font)textView.defaultAttributes().get(TextView.FONT_KEY);

                        if( currentFont != null ) {
                            fm = new FontMetrics( currentFont );
                            ascent = fm.ascent();
                        }

                        baselineOffset = attachment.height() - ascent;
                    } else if( align.equals("MIDDLE"))
                        baselineOffset = attachment.height() / 2;
                    newAttr.put(TextView.TEXT_ATTACHMENT_BASELINE_OFFSET_KEY,
                                new Integer(baselineOffset));
                }
            }

            return newAttr;
        }
        return initialAttributes;
    }

    /**
     * You can override this method to return what string should prefix the marker.
     * This method is usualy used to add extra characters like cariage returns.
     * <b>context</b> is the context
     * <b>lastchar</b> is the last character added to the textView. It is often useful to
     * check if lastChar is '\n' before adding another '\n'
     * The default implementation returns nothing.
     */
     public String prefix(Hashtable context, char lastChar) {
         if( marker.equals("HR"))
             return "\n\n";
         else
             return "";
     }

    public String string(Hashtable context) {
        if( marker.equals("BR") )
            return "\n";
        else
            return TextView.TEXT_ATTACHMENT_STRING;
    }

    private TextAttachment textAttachment(URL baseURL,TextView textView) {
        if( textAttachmentCache == null ) {
            if( marker.equals("HR"))
                textAttachmentCache = new HRTextAttachment();
            else if( marker.equals("IMG")) {
                Hashtable attr;
                String  hrefUrlStr;
                URL imageUrl;
                Bitmap bm;
                int width = -1,height = -1;
                String str;

                attr = hashtableForHTMLAttributes(attributes);

                if( attr != null && (hrefUrlStr = (String)attr.get("SRC")) != null ) {

                    try {
                        str = (String) attr.get("WIDTH");
                        if( str != null )
                            width = Integer.parseInt(str);
                        str = (String) attr.get("HEIGHT");
                        if( str != null )
                            height = Integer.parseInt(str);
                    } catch( NumberFormatException e ) {
                        width = height = -1;
                    }

                    try {
                        imageUrl = new URL( baseURL, hrefUrlStr );

                        if( false /*width != -1 && height != -1*/ ) {
                            bm = Bitmap.bitmapFromURL( imageUrl );
                            bm.setLoadsIncrementally(true);
                            bm.setUpdateTarget(textView);
                            bm.setUpdateCommand("refreshBitmap");
                            bm.loadData();
                            textAttachmentCache = new ImageAttachment(bm,width,height);
                        } else {
                            if((bm = Bitmap.bitmapFromURL( imageUrl )) != null)
                                bm.loadData();

                            if( bm != null && bm.isValid()) {
                                textAttachmentCache = new ImageAttachment(bm);
                            }
                        }
                    } catch( MalformedURLException e) {
                            System.err.println("Malformed URL " + hrefUrlStr );
                    }
                }

                /* Default image */
                if( textAttachmentCache == null ) {
                    textAttachmentCache = new BrokenImageAttachment();
                }
            }
        }
        return textAttachmentCache;
    }
}



