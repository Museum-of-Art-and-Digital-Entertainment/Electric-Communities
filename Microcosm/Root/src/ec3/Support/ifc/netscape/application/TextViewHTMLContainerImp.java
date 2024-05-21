// TextViewHTMLContainerImp.java
// By Ned Etcode
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import netscape.util.*;

/** TextViewHTMLContainer's subclass to implement Hn,BLOCKQUOTE,DL,CENTER,PRE,
  * OL,UL,MENU,DIR,ADDRESS,DT,DD,B,STRONG,EM,I,VAR,CITE,TT,CODE,SAMP,KBD,A,LI.
  * This class should be private and is used to parse some HTML in the textview.
  *  It is public to allow getClass() to work
  *  @private
  */
public class TextViewHTMLContainerImp extends TextViewHTMLContainer {

    /** Add a CR if necessary + another CR */
    static String markersStartingWithDoubleCarriageReturn[] =
        {"H1","H2","H3","H4","H5","H6","BLOCKQUOTE","DL"};
    static String markersEndingWithDoubleCarriageReturn[]   =
        {"H1","H2","H3","H4","H5","H6","BLOCKQUOTE","DL"};

    /** Add a CR */
    static String markersStartingWithCarriageReturn[] =
         { "CENTER","PRE","OL","UL","MENU","DIR","ADDRESS","P"};
    static String markersEndingWithCarriageReturn[]   =
        { "CENTER","PRE","ADDRESS","LI","P" };

    static String markersStartingWithCarriageReturnOptionaly[] = { "DT","DD" };

    static final String LIST_CONTEXT = "listctxt";

    private String currentListMarker(Hashtable context) {
        Vector v = (Vector) context.get(LIST_CONTEXT);
        Vector m;

        if( v == null )
            return null;

        m = (Vector) v.lastElement();
        if( m == null || m.count() != 2)
            return null;

        return (String)m.elementAt(0);
    }

    private int levelOfCurrentListMarker(Hashtable context) {
        Vector v = (Vector) context.get(LIST_CONTEXT);
        Vector m;

        if( v == null )
            return 1;
        return v.count();
    }

    private void addListInContext(String aMarker,Hashtable context) {
        Vector v = (Vector) context.get(LIST_CONTEXT);
        Vector m = new Vector();
        m.addElement(aMarker);
        m.addElement("0");
        if( v != null )
            v.addElement(m);
        else {
            v = new Vector();
            v.addElement(m);
            context.put(LIST_CONTEXT,v);
        }
    }

    private void removeLastListFromContext(Hashtable context) {
        Vector v = (Vector) context.get(LIST_CONTEXT);
        Vector m;
        if( v != null && (m = (Vector)v.lastElement()) != null ) {
            v.removeLastElement();
        }
    }

    private void bumpNumberOfListItemProcessed(Hashtable context) {
        Vector v = (Vector) context.get(LIST_CONTEXT);
        Vector m;
        if( v != null && (m = (Vector)v.lastElement()) != null ) {
            String n;
            n = (String) m.elementAt(1);
            n = "" + (Integer.parseInt((String)m.elementAt(1)) + 1);
            m.removeLastElement();
            m.addElement(n);
        }
    }

    private int numberOfListItemProcessed(Hashtable context) {
        Vector v = (Vector) context.get(LIST_CONTEXT);
        Vector m;
        if( v != null && (m = (Vector)v.lastElement()) != null ) {
            return Integer.parseInt((String) m.elementAt(1));
        }
        return 0;
    }


    /**
     * You can override this method to return what string should prefix the container.
     * This method is usualy used to add extra characters like cariage returns. For example,
     * Headers always start with a cariage return. This method for an header should return
     * a cariage return.
     * <b>context</b> is the context
     * <b>lastchar</b> is the last character added to the textView. It is often useful to
     * check if lastChar is '\n' before adding another '\n'
     * The default implementation returns nothing.
     */
     public String prefix(Hashtable context, char lastChar) {
        int i,c;

        if (marker.equals("LI")) {
            String markerType = currentListMarker(context);
            int level;
            if( markerType == null ) /* LI without list ignore */
                return null;
            else {
                FastStringBuffer lfb = new FastStringBuffer();
                level = levelOfCurrentListMarker(context);

                for(i=0;i<level;i++)
                    lfb.append("\t");
                if(markerType.equals("OL"))
                    return lfb.toString()  +
                        (numberOfListItemProcessed(context) + 1) + ". ";
                else
                    return lfb.toString() + "\u00b7 ";
            }
        }
        for(i=0,c=markersStartingWithDoubleCarriageReturn.length ; i < c ; i++)
            if( markersStartingWithDoubleCarriageReturn[i].equals(marker)) {
                if(lastChar != '\n')
                    return "\n\n";
                else
                    return "\n";
            }

        for(i=0,c=markersStartingWithCarriageReturn.length ; i < c ; i++)
            if( markersStartingWithCarriageReturn[i].equals(marker))
              return "\n";

        if( lastChar != '\n') {
          for(i=0,c=markersStartingWithCarriageReturnOptionaly.length ; i < c ; i++ )
            if( markersStartingWithCarriageReturnOptionaly[i].equals(marker))
              return "\n";
        }
        return "";
     }


    /**
     * You can override this method to return what string should suffix the container.
     * This method is usualy used to add extra characters like cariage returns. For example,
     * Headers always end with a cariage return. This method for an header should return
     * a cariage return.
     * <b>context</b> is the context
     * <b>lastchar</b> is the last character added to the textView. It is often useful to
     * check if lastChar is '\n' before adding another '\n'
     * The default implementation returns nothing.
     */
     public String suffix(Hashtable context, char lastChar) {
        int i,c;

        for(i=0,c=markersEndingWithDoubleCarriageReturn.length ; i < c ; i++)
            if( markersEndingWithDoubleCarriageReturn[i].equals(marker)){
                if( lastChar != '\n' )
                    return "\n\n";
                else
                    return "\n";
            }

        for(i=0,c=markersEndingWithCarriageReturn.length ; i < c ; i++)
            if( markersEndingWithCarriageReturn[i].equals(marker)) {
                    return "\n";
            }

        return "";
     }


    /**
     *  Setup the context for children. Override this method and add or change
     *  any key if you want to add some state for children.
     *  The default implementation does nothing.
     */
    public void setupContext(Hashtable context) {
        if( marker.equals("OL") || marker.equals("UL") ||
            marker.equals("DIR") || marker.equals("MENU"))
            addListInContext(marker,context);
        else if( marker.equals("LI") && currentListMarker(context) != null ) {
            bumpNumberOfListItemProcessed(context);
        }
    }


    /**
     *  Cleanup the context. If you have added some state in setupContext,
     *  you should override this method and remove any state added during
     *  setupContext()
     *  The default implementation does nothing
     */
    public void cleanupContext(Hashtable context) {
        if( marker.equals("OL") || marker.equals("UL") ||
            marker.equals("DIR") || marker.equals("MENU"))
            removeLastListFromContext(context);
    }

    public String string(Hashtable context) {
        if( marker.equals("TITLE"))
            return "";
        else
            return super.string(context);
    }
    /**
     *  Compute the TextView attributes for the container contents according to the
     *  context and initial attributes.
     *  Return the new attributes. The default implementation
     *  returns <b> initialAttributes</b>
     *  It is not necessary to allocate a new hashtable.You can just
     *  modify initialAttributes and return it.
     *  textView is the TextView for which the HTML is parsed.
     */
    public Hashtable attributesForContents(Hashtable context,Hashtable initialAttributes,TextView textView) {
        Hashtable newAttr;
        Font defaultFont = Font.defaultFont();

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

        if( marker.equals("H1"))
            newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),
                                                          Font.BOLD, 24));
        else if( marker.equals("H2"))
            newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),
                                                          Font.BOLD, 18));
        else if( marker.equals("H3"))
            newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),
                                                          Font.BOLD, 16));
        else if( marker.equals("H4"))
            newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),
                                                          Font.BOLD, 12));
        else if( marker.equals("H5"))
            newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),
                                                          Font.BOLD,  10));
        else if( marker.equals("H6"))
            newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),
                                                          Font.BOLD,  8));
        else if( marker.equals("B") || marker.equals("STRONG")) {
            Font f = (Font) newAttr.get(TextView.FONT_KEY);
            if( f != null ) {
                if(!f.isBold())
                    newAttr.put(TextView.FONT_KEY, Font.fontNamed( f.name(),
                                   f.style() | Font.BOLD,f.size()));
            } else {
                newAttr.put(TextView.FONT_KEY, Font.fontNamed(defaultFont.name(),Font.BOLD,
                                                        defaultFont.size()));
            }
        } else if( marker.equals("CENTER")) {
            TextParagraphFormat pf = (TextParagraphFormat)
                newAttr.get(TextView.PARAGRAPH_FORMAT_KEY);
            TextParagraphFormat centerFormat;
            if( pf != null )
                centerFormat = (TextParagraphFormat)pf.clone();
             else
                centerFormat = (TextParagraphFormat)
                    ((TextParagraphFormat)initialAttributes.get(
                                        TextView.PARAGRAPH_FORMAT_KEY)).clone();
            centerFormat.setJustification(Graphics.CENTERED);
            newAttr.put( TextView.PARAGRAPH_FORMAT_KEY, centerFormat );
        } else if( marker.equals("BLOCKQUOTE") || marker.equals("DD")) {
            TextParagraphFormat pf = (TextParagraphFormat)
            newAttr.get(TextView.PARAGRAPH_FORMAT_KEY);
            TextParagraphFormat indentFormat;
            if( pf != null )
                indentFormat = (TextParagraphFormat)pf.clone();
             else
                indentFormat = (TextParagraphFormat)
                    ((TextParagraphFormat)initialAttributes.get(
                                TextView.PARAGRAPH_FORMAT_KEY)).clone();
            indentFormat.setLeftMargin(50);
            newAttr.put( TextView.PARAGRAPH_FORMAT_KEY, indentFormat );
        } else if( marker.equals("EM") || marker.equals("I") || marker.equals("ADDRESS") ||
                   marker.equals("VAR") || marker.equals("CITE")) {
            Font f = (Font) newAttr.get(TextView.FONT_KEY);
            if( f != null ) {
                if(!f.isItalic())
                    newAttr.put(TextView.FONT_KEY, Font.fontNamed( f.name(),
                                f.style() | Font.ITALIC,f.size()));
            } else {
                newAttr.put(TextView.FONT_KEY, Font.fontNamed(
                     defaultFont.name(),Font.ITALIC,defaultFont.size()));
            }
        } else if( marker.equals("PRE")) {
            Font f = (Font) newAttr.get(TextView.FONT_KEY);
            TextParagraphFormat defaultFormat = (TextParagraphFormat)
                ((TextParagraphFormat)initialAttributes.get(
                          TextView.PARAGRAPH_FORMAT_KEY)).clone();
            if( f == null )
                f = Font.defaultFont();
            newAttr.put(TextView.FONT_KEY, Font.fontNamed( "Courier", f.style(), f.size()));
            newAttr.put(TextView.PARAGRAPH_FORMAT_KEY, defaultFormat);
        } else if(marker.equals("TT") || marker.equals("CODE") ||
                  marker.equals("SAMP") || marker.equals("KBD")) {
            Font f = (Font) newAttr.get(TextView.FONT_KEY);
            if( f == null )
                f = Font.defaultFont();
            newAttr.put(TextView.FONT_KEY, Font.fontNamed( "Courier", f.style(), f.size()));
        } else if( marker.equals("A")) {
          Hashtable attr;
          String url;
          String name;

          attr = hashtableForHTMLAttributes(attributes);

          if( attr != null ) {
            if( (url = (String)attr.get("HREF")) != null) {
              newAttr.put( TextView.LINK_KEY, url);
            }

            if((name = (String) attr.get("NAME")) != null ) {
              newAttr.put( TextView.LINK_DESTINATION_KEY, name);
            }
          }
        } else if( marker.equals("LI")) {
            TextParagraphFormat pf =(TextParagraphFormat) newAttr.get(
                                      TextView.PARAGRAPH_FORMAT_KEY);
            if( pf == null )
                pf = (TextParagraphFormat)
                    ((TextParagraphFormat)initialAttributes.get(
                                TextView.PARAGRAPH_FORMAT_KEY)).clone();
            pf.setWrapsUnderFirstCharacter(true);
            newAttr.put(TextView.PARAGRAPH_FORMAT_KEY, pf);
        } else if( marker.equals("P")) {
            Hashtable attr = hashtableForHTMLAttributes(attributes);
            String v;
            if((v=(String)attr.get("ALIGN")) != null) {
                TextParagraphFormat f = (TextParagraphFormat)
                    initialAttributes.get(TextView.PARAGRAPH_FORMAT_KEY);
                f = (TextParagraphFormat) f.clone();
                v = v.toUpperCase();
                if(v.equals("LEFT"))
                    f.setJustification(Graphics.LEFT_JUSTIFIED);
                else if(v.equals("CENTER"))
                    f.setJustification(Graphics.CENTERED);
                else if(v.equals("RIGHT"))
                    f.setJustification(Graphics.RIGHT_JUSTIFIED);
                newAttr.put(TextView.PARAGRAPH_FORMAT_KEY,f);
            }
        }
        return newAttr;
    }
}
