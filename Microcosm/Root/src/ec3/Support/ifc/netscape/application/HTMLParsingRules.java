// HTMLParsingRules.java
// By Ned Etcode
// Copyright 1995, 1996, 1997 Netscape Communications Corp. All rights reserved.

package netscape.application;

import netscape.util.*;

/** Instance of this class describe the rules used to parse some html.
  *
  */
public class HTMLParsingRules implements Codable {
    private Hashtable rules=null;
    private String    defaultContainerClassName;
    private String    defaultMarkerClassName;

    static final String RULES_KEY = "rules";
    static final String DEFAULT_CONTAINER_CLASS_NAME_KEY = "defaultContainerClass";
    static final String DEFAULT_MARKER_CLASS_NAME_KEY    = "defaultMarkerClass";

    /*
     * HTML Default rules
     */
    private  static final String HTMLDefaultRules =
        "{" +
        "  LI = { BeginTermination = ( LI ); EndTermination = (OL,UL,DIR,MENU); };   " +
        "  IMG= { IsContainer=false; };                                     " +
        "  DD = { BeginTermination = (DT,DD); EndTermination = (DL,A); };     " +
        "  DT = { BeginTermination = (DT,DD); EndTermination = (DL,A); };     " +
        "   P = { IsContainer=true; BeginTermination=(P,OL,UL,DIR,MENU,PRE,H1,H2,H3,H4,H5,H6);  }; " +
        "  BR = { IsContainer=false; };                                     " +
        "  HR = { IsContainer=false; };                                     " +
        "  PRE= { ShouldRetainFormatting=true; };                              " +
        "    A= { IsContainer=true; BeginTermination = (A); };              " +
        "}";

    /** This is not a real HTML marker, however this marker is used
      * to mean "a String"
      */
    public static final String STRING_MARKER_KEY  = "IFCSTRING";

    /** This is not a real HTML marker, however this marker is used
      * to mean "a Comment"
     */
    public static final String COMMENT_MARKER_KEY = "IFCCOMMENT";

    /**
      * The following keys are used to define how to parse HTML markers.
      * the default value of the "rule database" is:'
      *
      *  {
      *    LI = { BeginTermination = ( LI ); EndTermination = (OL,UL); };
      *    IMG= { IsContainer=false; };
      *    DD = { BeginTermination = (DT,DD); EndTermination = (DL); };
      *    DT = { BeginTermination = (DT,DD); EndTermination = (DL); };
      *     P = { BeginTermination = (P,H1,H2,H3,H4,H6,TABLE);
      *          EndTermination = (BODY,HTML);};
      *    BR = { IsContainer=false; };
      *    HR = { IsContainer=false; };
      *    PRE= { ShouldRetainFormatting=true; };
      *      A= { IsContainer=true; BeginTermination = (A); };
      *  };
      */


    /** The class that should be used to represent an HTML component
      * There is no default value although it is possible to set a default class
      * for a String, a Container or a marker
      */
    public static final String REPRESENTATION_KEY              = "Representation";

    /** This parameter is a list of markers. If the parser finds this marker in
      * a begin form (<FOO>)while parsing the HTML component, it will assume the
      * end of the component.
      */
    public static final String BEGIN_TERMINATION_MARKERS_KEY   = "BeginTermination";

    /** This parameter is a list of markers. If the parser finds this marker in
      * an ending form (</FOO>) while parsing the HTML component, it will assume
      * the end of the component
      */
    public static final String END_TERMINATION_MARKERS_KEY     = "EndTermination";


    /** This parameter defines whether the html component is a container or not.
      * default value is true
      */
    public static final String IS_CONTAINER_KEY                     = "IsContainer";

    /** This parameter defines whether the html component requires the strings
      * inside itself to be formated or not.
      * default value is false
      */
    public static final String SHOULD_RETAIN_FORMATTING_KEY       = "ShouldRetainFormatting";

    /** If true, the end of marker should be ignored. This is currently used
      * for </P>. Default value is false
      */
    public static final String SHOULD_IGNORE_END_KEY           = "ShouldIgnoreEnd";

    public HTMLParsingRules() {
        rules = (Hashtable) Deserializer.deserializeObject( HTMLDefaultRules );
        if( rules == null )
            throw new InconsistencyException("HTMLParsingRules: Cannot deserialize default rules");
    }

    /** Set the rules for a given marker. You can use this API to teach the
      * parser how unsupported markers behave.
      * Possible keys are:
      *    REPRESENTATION_KEY:       (Class)  the class that should be used to represent the marker
      *    BEGIN_TERMINATION_MARKERS_KEY:  (Vector) list of marker that terminate the marker
      *                                             when they are beginning. <FOO>
      *    END_TERMINATION_MARKERS_KEY:    (Vector) list of marker that terminate the marker
      *                                             when they are ending. </FOO>
      *    IS_CONTAINER_KEY:              (String) "true" if the marker is a container. "false"
      *                                            otherwise
      *    SHOULD_RETAIN_FORMATTING_KEY (String) if "true", the parser does not remove \n ' ' and '\t'
      *                                       from the data inside the container. This is useful
      *                                       for markers like PRE
      */
    public void setRulesForMarker(Hashtable markerRules,String marker) {
        rules.put( marker,markerRules );
    }


    /** Return the current rules for aMarker */
    public Hashtable rulesForMarker(String aMarker) {
        return (Hashtable) rules.get(aMarker);
    }

    /** Convenience to set a single rule for a marker */
    public void setRuleForMarker(String rule,Object value,String marker) {
        Hashtable h = rulesForMarker(marker);
        if( h == null )
            h = new Hashtable();
        h.put(rule,value);
        setRulesForMarker(h,marker);
    }

    /** Convenience to define the class that should be used to store an HTML
      * component with the marker aMarker. If aMarker is STRING_MARKER_KEY or
      * COMMENT_MARKER_KEY, this method will define which class should be used
      * to store a String or comments.  aClass should be a subclass of
      * TextViewHTMLElement.
      */
    public void setClassNameForMarker(String className,String aMarker) {
        Hashtable r = rulesForMarker(aMarker);
        if( r == null )
            r = new Hashtable();
        r.put(REPRESENTATION_KEY,className);
        setRulesForMarker( r, aMarker );
    }

    /** Return the name of the class that will be used to store a component with
      * the marker aMarker if no specific class has been affected to the marker,
      * this method will try the default classes. If no default exists, return
      * null
      */
    public String classNameForMarker(String aMarker) {
        Hashtable r = rulesForMarker(aMarker);
        if( r != null ) {
            String result = (String) r.get(REPRESENTATION_KEY);
            if( result == null ) {
                if( isContainer(r) )
                    result = defaultContainerClassName;
                else
                    result = defaultMarkerClassName;
            }
            return result;
        } else {
            boolean isContainer;
            Hashtable markerRules = rulesForMarker( aMarker );
            if( markerRules != null ) {
                isContainer = isContainer( markerRules );
            } else
                isContainer = true;

            if( isContainer && defaultContainerClassName != null )
                return defaultContainerClassName;
            else if (!isContainer && defaultMarkerClassName != null )
                return defaultMarkerClassName;
        }
        return null;
    }

    /** Set the name of the default class to be use to store container
      * components This class is used if no other class has been specified by
      * using setClassNameForMarker() or setRulesForMarker()
      */
    public void setDefaultContainerClassName(String aClassName) {
        defaultContainerClassName = aClassName;
    }

    /** Return the default container class */
    public String defaultContainerClassName() {
        return defaultContainerClassName;
    }

    /** Set the name of the default class to be use to store markers components
      * This class is used if no other class has been specified by using
      * setClassForMarker() or setRulesForMarker()
      */
    public void setDefaultMarkerClassName(String aClassName) {
        defaultMarkerClassName = aClassName;
    }

    /** Return the default marker class */
    public String defaultMarkerClassName() {
        return defaultMarkerClassName;
    }

    /** Set the name of the class that should be used to store a String
      * The class should be a subclass of TextViewHTMLElement.
      */
    public void setStringClassName(String className) {
        setClassNameForMarker(className, STRING_MARKER_KEY );
    }

    /** Return the class that is used to store a String */
    public String classNameForString() {
        return classNameForMarker( STRING_MARKER_KEY );
    }

    /** Set the name of the class that should be used to store a comment
      * The class should be a subclass of TextViewHTMLElement.
      */
    public void setClassNameForComment(String className) {
        setClassNameForMarker(className, COMMENT_MARKER_KEY );
    }

    /** Return the class name that is used to store a comment */
    public String classNameForComment() {
        return classNameForMarker( COMMENT_MARKER_KEY );
    }

    boolean shouldIgnoreEnd(Hashtable markerRules) {
        if( markerRules == null )
            return false;
        if( markerRules.get(SHOULD_IGNORE_END_KEY) != null &&
            ((((String)markerRules.get(SHOULD_IGNORE_END_KEY))).toUpperCase()).equals("TRUE"))
            return true;
        else
            return false;
    }

    boolean isContainer(Hashtable markerRules) {
        if( markerRules == null )
            return true;
        if( markerRules.get(IS_CONTAINER_KEY) != null &&
            ((((String)markerRules.get(IS_CONTAINER_KEY))).toUpperCase()).equals("FALSE"))
            return false;
        else
            return true;
    }


    boolean shouldFilterStringsForChildren(Hashtable markerRules) {
        if( markerRules == null )
            return true;
        if( markerRules.get(SHOULD_RETAIN_FORMATTING_KEY) != null  &&
            (((String)markerRules.get(SHOULD_RETAIN_FORMATTING_KEY)).toUpperCase()).equals("TRUE"))
            return false;
        else
            return true;
    }

    /* archiving */


    /** Describes the HTMLParsingRules class's information.
      * @see Codable#describeClassInfo
      */
    public void describeClassInfo(ClassInfo info) {
        info.addClass("netscape.application.HTMLParsingRules", 1);
        info.addField(RULES_KEY, OBJECT_TYPE);
        info.addField(DEFAULT_CONTAINER_CLASS_NAME_KEY, OBJECT_TYPE);
        info.addField(DEFAULT_MARKER_CLASS_NAME_KEY, OBJECT_TYPE);
    }

    /** Encodes the HTMLParsingRules instance.
      * @see Codable#encode
      */
    public void encode(Encoder encoder) throws CodingException {
        encoder.encodeObject(RULES_KEY,rules);
        encoder.encodeObject(DEFAULT_CONTAINER_CLASS_NAME_KEY,defaultContainerClassName);
        encoder.encodeObject(DEFAULT_MARKER_CLASS_NAME_KEY,defaultMarkerClassName);
    }

    /** Decodes the HTMLParsingRules instance.
      * @see Codable#decode
      */
    public void decode(Decoder decoder) throws CodingException {
        rules = (Hashtable) decoder.decodeObject(RULES_KEY);
        defaultContainerClassName = (String) decoder.decodeObject(DEFAULT_CONTAINER_CLASS_NAME_KEY);
        defaultMarkerClassName    = (String) decoder.decodeObject(DEFAULT_MARKER_CLASS_NAME_KEY);
    }

    /** Finishes the HTMLParsingRules instance decoding.
      * @see Codable#finishDecoding
      */
    public void finishDecoding() throws CodingException {
    }
}

