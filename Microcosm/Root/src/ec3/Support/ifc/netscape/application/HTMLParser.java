// HTMLParser.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.FilterInputStream;


/** A generic HTML parser. This class provides the HTML
  * parsing functionality without defining how to store HTML.
  * The user provides some information, telling the parser
  * which class should be used for which marker.
  * The parser creates instances of these classes.
  *  @note 1.0 changes
  *  @private
  */
public class HTMLParser extends FilterInputStream {

    /*
     * Special char to unicode
     */
    private static final String specialChars[] = { "lt",  "<",
                                     "gt",   ">",
                                     "amp", "&",
                                     "quot", "\"" ,
                                     "nbsp","\u00a0",
                                     "iexcl","\u00a1",
                                     "cent","\u00a2",
                                     "pound","\u00a3",
                                     "curren","\u00a4",
                                     "yen","\u00a5",
                                     "brvbar","\u00a6",
                                     "sect","\u00a7",
                                     "uml","\u00a8",
                                     "copy","\u00a9",
                                     "ordf","\u00aa",
                                     "laquo","\u00ab",
                                     "not","\u00ac",
                                     "shy","\u00ad",
                                     "reg","\u00ae",
                                     "macr","\u00af",
                                     "deg","\u00b0",
                                     "plusmn","\u00b1",
                                     "sup2","\u00b2",
                                     "sup3","\u00b3",
                                     "acute","\u00b4",
                                     "micro","\u00b5",
                                     "para","\u00b6",
                                     "middot","\u00b7",
                                     "cedil","\u00b8",
                                     "sup1","\u00b9",
                                     "ordm","\u00ba",
                                     "raquo","\u00bb",
                                     "frac14","\u00bc",
                                     "frac12","\u00bd",
                                     "frac34","\u00be",
                                     "iquest","\u00bf",
                                     "Agrave","\u00c0",
                                     "Aacute","\u00c1",
                                     "Acirc","\u00c2",
                                     "Atilde","\u00c3",
                                     "Auml","\u00c4",
                                     "Aring","\u00c5",
                                     "AElig","\u00c6",
                                     "Ccedil","\u00c7",
                                     "Egrave","\u00c8",
                                     "Eacute","\u00c9",
                                     "Ecirc","\u00ca",
                                     "Euml","\u00cb",
                                     "Igrave","\u00cc",
                                     "Iacute","\u00cd",
                                     "Icirc","\u00ce",
                                     "Iuml","\u00cf",
                                     "ETH","\u00d0",
                                     "Ntilde","\u00d1",
                                     "Ograve","\u00d2",
                                     "Oacute","\u00d3",
                                     "Ocirc","\u00d4",
                                     "Otilde","\u00d5",
                                     "Ouml","\u00d6",
                                     "times","\u00d7",
                                     "Oslash","\u00d8",
                                     "Ugrave","\u00d9",
                                     "Uacute","\u00da",
                                     "Ucirc","\u00db",
                                     "Uuml","\u00dc",
                                     "Yacute","\u00dd",
                                     "THORN","\u00de",
                                     "szlig","\u00df",
                                     "agrave","\u00e0",
                                     "aacute","\u00e1",
                                     "acirc","\u00e2",
                                     "atilde","\u00e3",
                                     "auml","\u00e4",
                                     "aring","\u00e5",
                                     "aelig","\u00e6",
                                     "ccedil","\u00e7",
                                     "egrave","\u00e8",
                                     "eacute","\u00e9",
                                     "ecirc","\u00ea",
                                     "euml","\u00eb",
                                     "igrave","\u00ec",
                                     "iacute","\u00ed",
                                     "icirc","\u00ee",
                                     "iuml","\u00ef",
                                     "eth","\u00f0",
                                     "ntilde","\u00f1",
                                     "ograve","\u00f2",
                                     "oacute","\u00f3",
                                     "ocirc","\u00f4",
                                     "otilde","\u00f5",
                                     "ouml","\u00f6",
                                     "divide","\u00f7",
                                     "oslash","\u00f8",
                                     "ugrave","\u00f9",
                                     "uacute","\u00fa",
                                     "ucirc","\u00fb",
                                     "uuml","\u00fc",
                                     "yacute","\u00fd",
                                     "thorn","\u00fe",
                                     "yuml","\u00ff",
                                     "ensp"," ",
                                     "emsp"," ",
                                     "endash","-",
                                     "emdash","-",
                                   /*  "zwnj","\u200c",
                                     "zwj", "\u200d",
                                     "lrm", "\u200e",
                                     "rlm", "\u200f",*/

    };


    private HTMLTokenGenerator tokenGenerator;
    private HTMLParsingRules rules;
    private Class defaultContainerClass = null;
    private Class defaultMarkerClass = null;
    private boolean throwsException = false;

    private boolean appletInitialized = false;
    private FoundationApplet applet;


    /** Constructor */
    public HTMLParser(InputStream in) {
        this(in,new HTMLParsingRules());
    }

    public HTMLParser(InputStream in,HTMLParsingRules rules) {
        super(in);
        this.rules = rules;
        tokenGenerator = new HTMLTokenGenerator(in);
    }

    /** Set whether the parser should raise when some bad HTML is parsed.
     *  if flag is false, bad statement will be just ignored
     *  The default is false.
     */
    public void setThrowsExceptionOnHTMLError(boolean flag) {
        throwsException = flag;
    }

    /** Return whether the parser throw an exception when some bad HTML is parsed */
    public boolean throwsExceptionOnHTMLError() {
        return throwsException;
    }


   /**
     *  Parse the next HTML component
     */
    public HTMLElement nextHTMLElement()
        throws IOException,HTMLParsingException,
        java.lang.InstantiationException,java.lang.IllegalAccessException {
            HTMLElement result;
        while( tokenGenerator.hasMoreTokens()) {
            result =  parseNextHTMLElement(true,true,null);
            if( result != null )
                return result;
        }
        return null;
    }



    /**
     *  Utility to convert String containing attributes to Hashtable
     *  Keys will be converted to upper case.
     */
    public static Hashtable hashtableForAttributeString(String attributesString)
        throws HTMLParsingException {
        Hashtable result = new Hashtable();
        int i,c;
        String key,value;
        FastStringBuffer fb = new FastStringBuffer();
        int offset;

        if( attributesString == null )
            return result;

        c = attributesString.length();
        i = 0;
        while( i < c ) {
            while( i < c && isSpace(attributesString.charAt(i)) )
                i++;
            if( i == c )
                break;
            fb.truncateToLength( 0 );
            offset = parseKeyOrValue( attributesString, i , fb );
            if( offset == 0 ) {
                throw new HTMLParsingException("Error while parsing attributes " +
                                              attributesString,0);
            }

            key = filterKeyOrValue( fb );
            key = key.toUpperCase();
            i += offset;

            if( key.equals(""))
                continue;

            while( i < c && isSpace(attributesString.charAt(i)) )
                i++;

            if( i < c && attributesString.charAt(i) == '=' ) { /* We have a value */
                i++;
                fb.truncateToLength( 0 );
                offset = parseKeyOrValue( attributesString, i, fb );
                value = filterKeyOrValue( fb );
                i += offset;
                result.put(key,value);
            } else { /* Attribute without a value */
                result.put(key,"");
            }
        }
        return result;
    }

   /** Called on syntax error. Throw an exception if HTMLParsingException is
     * enabled. Otherwise does nothing.
     */
    public void reportSyntaxError(String description) throws HTMLParsingException {
        if( throwsException )
            throw new HTMLParsingException( description , tokenGenerator.lineForLastToken());
    }

    /** Convenience to avoid breaking constructor */
    public void setClassForMarker(Class aClass,String aMarker) {
        rules.setClassNameForMarker(aClass.getName(),aMarker);
    }

    private final char unicodeCharForBytes( String bytes ) {
        int i,c;
        String s = bytes;
        if( s.length() > 0 && s.charAt(0) == '#' ) {
            return (char) Integer.parseInt(s.substring(1,s.length()));
        }
        for(i = 0 , c = specialChars.length ; i < c ; i += 2 ) {
            if( specialChars[i].equals( s ))
                return specialChars[i+1].charAt(0);
        }
        return 0;
    }

    private final int convertSpecialCharacter(String s,int startIndex,FastStringBuffer result ){
        int length = s.length();
        char theChar;

        if( (startIndex+1) < length ) {
            int start = startIndex + 1;
            int end = start;
            char ch;

            ch = s.charAt(end);
            while( end < length && ch != ';' && ch != ' ' && ch != '\n' && ch != '\t' ) {
                end++;
                if( end < length )
                    ch = s.charAt(end);
                else
                    ch = 0;
            }

            if( end > start ) {
                String subStr;
                subStr = s.substring(start,start+(end-start));
                theChar = unicodeCharForBytes( subStr );
                if( theChar != 0 && theChar != 8 )
                    result.append( theChar );

                if( end < length && s.charAt(end) == ';')
                    return subStr.length() + 2; /* + 1 for the starting & and the ; */
                else
                    return subStr.length() + 1;
            }
        }
        return 0;
    }

    private final String filterHTMLString(String s,boolean filterSpaces,
                                          boolean allowSpaceForFirstChar) {
        FastStringBuffer sb = new FastStringBuffer();
        int i,c,delta;
        char ch;
        boolean previousCharWasSpace = false;
        boolean nonSpaceCharFound = false;
        for(i=0,c=s.length() ; i < c ; i++) {
            ch = s.charAt(i);

            if(filterSpaces && (ch == ' ' || ch == '\t' || ch == '\n') ) {
                if( !nonSpaceCharFound &&
                    ((allowSpaceForFirstChar && (ch == '\t' || ch == '\n')) ||
                     (!allowSpaceForFirstChar && (ch == '\t' || ch == '\n' || ch == ' '))))
                    continue;
                if( previousCharWasSpace )
                    continue;
                else {
                    previousCharWasSpace = true;
                    sb.append(' ');
                    continue;
                }
            } else if( ch == '&' ) {
                delta = convertSpecialCharacter(s,i,sb );
                if( delta > 0 )
                    i += (delta - 1); /* -1 since i++ will happen before the next iteration */
                previousCharWasSpace = false;
                nonSpaceCharFound = true;
                continue;
            } else if(ch != '\n' && ch != '\t' &&
                      (ch < ' ' || ch > '~') ) /* Should filter these characters */
                continue;
            previousCharWasSpace = false;
            nonSpaceCharFound = true;
            sb.append( ch );
        }
        if( sb.length() > 0 )
            return sb.toString();
        else
            return null;
    }

    private Class classForMarker(String aMarker) {
        String className = rules.classNameForMarker(aMarker);
        if( className != null ) {
            Class c;

            try {
                if (!appletInitialized) {
                    applet = (FoundationApplet) AWTCompatibility.awtApplet();
                    appletInitialized = true;
                }

                if (applet != null)
                    c = applet.classForName(className);
                else
                    c = Class.forName(className);
            } catch(ClassNotFoundException e) {
                System.err.println("" + e);
                c = null;
            }
            return c;
        }
        return null;
    }

    private final HTMLElement parseNextHTMLElement(boolean doFilterStrings,
                                                       boolean allowSpaceAsFirstChar,
                                                       String pMarker)
        throws IOException,HTMLParsingException,
        java.lang.InstantiationException,java.lang.IllegalAccessException {
        int token;
        HTMLElement result = null;
        Class c;
        String marker;
        Hashtable markerRules;

        token = tokenGenerator.nextToken();
        switch( token ) {
        case HTMLTokenGenerator.STRING_TOKEN:
            if((c = classForMarker(HTMLParsingRules.STRING_MARKER_KEY)) != null ) {
                String s = tokenGenerator.stringForLastToken();
                s = filterHTMLString(s,doFilterStrings,allowSpaceAsFirstChar);
                if( s != null ) { /* Filter might remove string with only spaces */
                    result = (HTMLElement) c.newInstance();
                    result.setMarker(HTMLParsingRules.STRING_MARKER_KEY);
                    result.setString( s );
                    return result;
                }
            }
            break;
        case HTMLTokenGenerator.MARKER_BEGIN_TOKEN:
            marker = tokenGenerator.stringForLastToken();
            markerRules = rules.rulesForMarker(marker);
            if( (c = classForMarker(marker)) != null) {
                if( rules.isContainer(markerRules)) {
                    HTMLElement nextChild;
                    Vector beginTerminators = null;
                    Vector endTerminators   = null;
                    Object children[],tmp[];
                    int childrenCount;
                    boolean endMarkerFound = false;
                    boolean notFirstChild = false;
                    result = (HTMLElement) c.newInstance();
                    result.setMarker( marker );
                    result.setAttributes( tokenGenerator.attributesForLastToken());

                    children = new Object[2];
                    childrenCount = 0;
                    if( markerRules != null ) {
                        beginTerminators = (Vector) markerRules.get(
                                                 HTMLParsingRules.BEGIN_TERMINATION_MARKERS_KEY);
                        endTerminators   = (Vector) markerRules.get(
                                                 HTMLParsingRules.END_TERMINATION_MARKERS_KEY);
                    }

                    while( tokenGenerator.hasMoreTokens() ) {
                        token = tokenGenerator.peekNextToken();
                        if( token == HTMLTokenGenerator.MARKER_END_TOKEN ) {
                            String endMarker = tokenGenerator.stringForLastToken();
                            if(marker.equals(endMarker)) {
                                tokenGenerator.nextToken(); /* Remove the token */
                                endMarkerFound = true;
                                break;
                            } else if( endTerminators != null &&
                                       endTerminators.indexOf(endMarker)!=-1) {
                                endMarkerFound=true;
                                break;
                            } else if(classForMarker(endMarker) != null) {
                                /** Unexpected end for a known marker
                                 *  This is an error but we should
                                 *  stop parsing the current marker.
                                 *  to allow the known marker to be
                                 *  closed. This strategy avoid having
                                 *  very deep trees when some closing
                                 *  markers are not in the right scope
                                 */
                                reportSyntaxError("Unexcpected closing " + endMarker +
                                                  " while parsing contents for " + marker );
                                endMarkerFound=true;
                                break;
                            }
                        } else if( token == HTMLTokenGenerator.MARKER_BEGIN_TOKEN &&
                                   beginTerminators != null &&
                                   beginTerminators.indexOf(tokenGenerator.stringForLastToken())
                                   != -1 ) {
                            endMarkerFound = true;
                            break;
                        }
                        /* Should filter strings if the marker requires it or
                         * one of the parent requires it.
                         */
                        if( rules.shouldFilterStringsForChildren(markerRules) &&
                            doFilterStrings==true )
                            nextChild = parseNextHTMLElement(true,notFirstChild,marker);
                        else
                            nextChild = parseNextHTMLElement(false,notFirstChild,marker);
                        notFirstChild = true;
                        if( nextChild == null ) {
                            if( tokenGenerator.hasMoreTokens() == false ) {
                                reportSyntaxError("Unterminated marker " + marker);
                                break;
                            } else
                                continue;
                        } else {
                            children[childrenCount++] = nextChild;
                            if( childrenCount == children.length ) {
                                Object newChildren[] = new Object[children.length * 2];
                                System.arraycopy(children,0,newChildren,0,childrenCount);
                                children = newChildren;
                            }
                        }
                    }

                    if( childrenCount > 0 ) {
                        tmp = new Object[childrenCount];
                        System.arraycopy(children,0,tmp,0,childrenCount);
                        result.setChildren( tmp );
                    } else
                        result.setChildren( null );

                    if(! endMarkerFound ) {
                        reportSyntaxError("No end found for marker " + marker);
                    }
                    return result;
                } else {
                    result = (HTMLElement) c.newInstance();
                    result.setMarker( marker );
                    result.setAttributes( tokenGenerator.attributesForLastToken());
                    return result;
                }
            }
            break;
        case HTMLTokenGenerator.COMMENT_TOKEN:
            if((c = classForMarker(HTMLParsingRules.COMMENT_MARKER_KEY)) != null ) {
                String s = tokenGenerator.stringForLastToken();
                result = (HTMLElement) c.newInstance();
                result.setMarker(HTMLParsingRules.COMMENT_MARKER_KEY);
                result.setString( s );
                return result;
            }
            break;
        case HTMLTokenGenerator.MARKER_END_TOKEN:
            marker = tokenGenerator.stringForLastToken();
            c = classForMarker(marker);
            if( c != null && !rules.shouldIgnoreEnd( rules.rulesForMarker( marker )))   {
                reportSyntaxError("Unexpected closing " + marker +
                            " while parsing contents for marker " + pMarker);
            }
            break;
        default:
            reportSyntaxError("Unexpected statement");
        }
        return null;
    }




   private static boolean isSpace(char c) {
       if( c == ' ' || c == '\t' || c == '\n' )
           return true;
       else
           return false;
   }

   private static int parseKeyOrValue(String source,int index,FastStringBuffer dest) {
       int start,end,length;
       start = index;
       length = source.length();
       char endChar = 0;

       while(start < length && isSpace(source.charAt(start)))
           start++;

       if( start == length )
           return 0;

       end = start;
       if( source.charAt(end) == '\'' ||
           source.charAt(end) == '"' )
           endChar = source.charAt(end);
       do {
           dest.append(source.charAt(end));
           end++;
       } while(end < length &&
             ((endChar == 0 && !isSpace(source.charAt(end)) && source.charAt(end) != '=' ) ||
              (endChar != 0 && source.charAt(end) != endChar)));

       if( end < length && source.charAt(end) == endChar ) {
           dest.append(source.charAt(end));
           end++;
       }
       return end - start;
   }

   /* Remove " or '.
    */
   private static String filterKeyOrValue(FastStringBuffer source) {
       int c = source.length();

       if( c == 0 )
           return "";

       if( source.charAt(0) == '\''  || source.charAt(0) == '"' ) {
           if( c <= 2 )
               return "";
           else
               return source.toString().substring(1,c-1);
       }
       return source.toString();
   }

}




