/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, April 97
 */
package ec.edoc;

import java.util.Vector;
import java.io.PrintStream;
import java.util.Enumeration;


/** This class is used by edoc to greate an HTML file for documentation.
 *  It writes a given class' documentation to a given printstream.
 */
class HTMLGenerator {

    /** This field is set true if we should print out bullets as images */
    private boolean myImages = false;

    /** this method is used to declare whether bullets should be produced
     *  as image links, or as text */
    void images(boolean doWeWantImages) {
        myImages = doWeWantImages;
    }

    static final String INSTANCE_VAR_COLOUR = "magenta";
    static final String STATIC_VAR_COLOUR = "blue";
    static final String CONSTRUCTOR_COLOUR = "yellow";
    static final String INSTANCE_METHOD_COLOUR = "red";
    static final String STATIC_METHOD_COLOUR = "green";

    /** This method prints out a standard preamble used to start all
     *  html files. It includes both the required headers, and a bar
     *  of links to the vorious index files.
     *  @param p, nullFatal; The PrintStream to output to.
     *  @param title, nullFatal; A string containing the title of the file */
    private void printHTMLPreamble(PrintStream p, String title) {
        p.println("<html>");

        p.println("<head>");
        p.println("<a name=\"_top_\"></a>");
        p.println("<title>" + title + "</title>");
        p.println("</head>");

        p.println("<body>");

        p.print("<pre>");
        p.print("<a href=\"Packages.html\">Packages</a>   ");
        p.print("<a href=\"Classes.html\">Class List</a>   ");
        p.print("<a href=\"Tree.html\">Class Hierarchy</a>   ");
        p.print("<a href=\"Index.html\">Index</a>");
        p.print("</pre>");

        p.println("<hr>");
        p.println("<h1>" + title + "</h1>");
        p.println("<pre>\n</pre>");
    }

    /** This method returns an html fragment for a link to a subsection
     *  of a file. Used in the index to link to appropriate bits of files.
     *  @param file, nullFatal; the name of the file to link to
     *  @param section, nullFatal; the section (anchor) within the file which
     *   should be linked to.
     *  @param text, nullFatal; the text which should appear to the user for
     *   this link.
     *  @return nullFatal; the appropriate html fragment.
     */
    private static String getIndexLink(String file,
                                       String section,
                                         String text) {
        return "<a href=\""+file+".html#"+section+"\">"
            +text+ "</a>";
    }

    /** This method prints out an index file containing each element of index
     *  in order. (Which should thus be sorted)
     *  @param p, nullFatal; The PrintStream to output to.
     *  @param index, nullFatal; Vector(IndexEntry) the items to be indexed.
     *  @see ec.edoc.IndexEntry.html
     */
    void outputIndex(PrintStream p, Vector index) {

        printHTMLPreamble(p, "Index");

        p.println("<h2>Key</h2>");
        p.println("<dl compact>");
        p.println("<dt>" + Bullet(STATIC_VAR_COLOUR, myImages));
        p.println("<dd> Static variable");
        p.println("<dt>" + Bullet(INSTANCE_VAR_COLOUR, myImages));
        p.println("<dd> Instance variable");
        p.println("<dt>" + Bullet(CONSTRUCTOR_COLOUR, myImages));
        p.println("<dd> Constructor");
        p.println("<dt>" + Bullet(STATIC_METHOD_COLOUR, myImages));
        p.println("<dd> Static method");
        p.println("<dt>" + Bullet(INSTANCE_METHOD_COLOUR, myImages));
        p.println("<dd> Instance method");
        p.println("</dl>");


        /* Loop over all elements of index until we get to Nil */
        String last = null;
        Enumeration e = index.elements();
        while (e.hasMoreElements()) {

            IndexEntry ie = (IndexEntry) e.nextElement();

            if (last == null) {
                last = ie.name();
                p.println("<strong>"+ last + "</strong><blockquote>");
            } else if (!last.equals(ie.name())) {
                last = ie.name();
                p.println("</blockquote><strong>"+last+"</strong><blockquote>");
            }

            //System.out.println(ie.name() + ie.tag());
            p.println("<p><code>");

            /* Print out a bullet of appropriate colour */
            if (ie.isConstructor()) {
                p.print(Bullet(CONSTRUCTOR_COLOUR, myImages));
            } else if (ie.isField()) {
                if ((ie.mods() & Info.STATIC) != 0) {
                    p.print(Bullet(STATIC_VAR_COLOUR, myImages));
                } else {
                    p.print(Bullet(INSTANCE_VAR_COLOUR, myImages));
                }
            } else {
                if ((ie.mods() & Info.STATIC) != 0) {
                    p.print(Bullet(STATIC_METHOD_COLOUR, myImages));
                } else {
                    p.print(Bullet(INSTANCE_METHOD_COLOUR, myImages));
                }
            }

            if (ie.isField()) {

                p.println(getIndexLink(ie.parent(), ie.name(), ie.name()));

            } else {
                /* Get types */
                Vector v = (TypeTable.getExternalMethodTypes(ie.type()));
                Enumeration types = v.elements();

                if (ie.isConstructor()) {
                    /* throw away return Type */
                    types.nextElement();
                } else {
                    /* print return type */
                    p.print(getCheckedTypeLink(
                        (String) types.nextElement()) + " ");
                }
                /* print name / link */
                p.print(getIndexLink(ie.parent(),ie.name()+ie.type(),ie.name())
                        + "(");
                while (types.hasMoreElements()) {
                    p.print(getCheckedTypeLink((String) types.nextElement()));
                    if (types.hasMoreElements()) {
                        p.print(", ");
                    }
                }
                p.print(")");
            }

            p.print("  in " + getShortLink(ie.parent()));
            p.println("</code>");
        }
        p.println("</blockquote>");
        p.println("<hr></body></html>");
    }

    /** This method output a class hierarchy tree.
     *  @param p, nullFatal; The PrintStream to output to.
     *  @param classes, nullFatal; Vector(IndexEntry) a collection of
     *   classes to be output.
     *  @see ec.edoc.IndexEntry.html
     */
    void outputTree(PrintStream p, Vector classes) {

        //System.out.println("Printing Tree");

        printHTMLPreamble(p, "Tree");

        /* first we add all classes (IndexEntryies) into a hashtable,
         * indexed by their parents. If they don't extend anything else,
         * then they extend Object. Thus we can retrieve all of our roots
         * by looking up Object */
        java.util.Hashtable h = new java.util.Hashtable(2 * classes.size() + 1);
        for (Enumeration e = classes.elements(); e.hasMoreElements(); ) {
            IndexEntry ie = (IndexEntry) e.nextElement();

            String parent = ie.parent();
            String name = ie.name();

            if (parent == null) {
                parent = "java.lang.Object";
            }

            Vector v = (Vector) h.get(parent);
            if (v != null) {
                v.addElement(name);
            } else {
                //System.out.println("Adding " + parent);
                /* parent is not in the table yet */
                v = new Vector();
                v.addElement(name);
                h.put(parent, v);
            }
        }

        printTree(p, h, "java.lang.Object");

        //p.println("<hr><applet code=\"Graph.class\" width=100% height=90%>");
        //p.print("<param name=edges value=\"");

        //for (Enumeration e = classes.elements(); e.hasMoreElements(); ) {
        //    IndexEntry ie = (IndexEntry) e.nextElement();

        //  if (ie.parent().equals("java.lang.Object")) {
        //      continue;
        //  }

        //   p.print(ie.parent() + "-" + ie.name());

        //  if (e.hasMoreElements()){
        //      p.println("/30,");
        //  } else {
        //      p.println("\"></applet>");
        //  }
        //}

        p.println("<hr></body></html>");
    }

    /** This is a private helper function for outputTree (because it recurses).
     *  @param p, nullFatal; The PrintStream to output to.
     *  @param h, nullFatal; Hashtable(String,Vector(String))
     *   which maps classes to a list of their direct subclasses.
     *  @param entry, nullFatal; which entry in 'h' should have its
     *   subclasses output.
     *  @see #outputClasses
     */
    private void printTree(PrintStream p, java.util.Hashtable h, String entry) {

        //System.out.println("Recursively printing " + entry);

        Vector v = (Vector) h.get(entry);
        if (v == null) {
            return;
        }
        p.println("<ul>");
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
            String s = (String) e.nextElement();
            p.println("<li>" + s.substring(0, s.lastIndexOf('.') + 1) +
                getShortLink(s));
            printTree(p, h, s);
        }
        p.println("</ul>");
    }


    /** This method outputs a flat list of classes.
     *  @param p, nullFatal; The PrintStream to output to.
     *  @param classes, nullFatal; Vector(IndexEntry) a collection of
     *   classes to be output.
     *  @see ec.edoc.IndexEntry.html
     */
    void outputClasses(PrintStream p, Vector classes) {

        printHTMLPreamble(p, "Classes");

        /* Loop over all elements of classes until we get to Nil */
        String last = null;
        Enumeration e = classes.elements();
        while (e.hasMoreElements()) {

            IndexEntry ie = (IndexEntry) e.nextElement();

            if (last == null) {
                last = ie.name().substring(0, ie.name().lastIndexOf('.')+1);
                p.println("<h3>Package "+ getAnchor(last) +last + ":</h3><ul>");
            } else if (!last.equals(
                    ie.name().substring(0, ie.name().lastIndexOf('.')+1))) {
                last = ie.name().substring(0, ie.name().lastIndexOf('.')+1);
                p.println("</ul><h3>Package " + getAnchor(last) + last +
                    ":</h3><ul>");
            }

            //System.out.println(ie.name() + ie.tag());
            p.println("<li><code>");

            p.println(getShortLink(ie.name()));

            p.println("</code>");
        }
        p.println("</ul>");
        p.println("<hr></body></html>");
    }

    /** This method outputs a flat list of the packages to which some classes
     *  belong.
     *  @note this assumes that classes has been sorted so that all classes
     *   within a package are in a contiguous block in 'classes'
     *  @param p, nullFatal; The PrintStream to output to.
     *  @param classes, nullFatal; Vector(IndexEntry) a collection of
     *   classes from which to output packages.
     *  @see ec.edoc.IndexEntry.html
     */
    void outputPackages(PrintStream p, Vector classes) {

        printHTMLPreamble(p, "Packages");

        p.print("<ul>");

        /* Loop over all elements of classes until we get to Nil */
        String last = null;
        Enumeration e = classes.elements();
        while (e.hasMoreElements()) {

            IndexEntry ie = (IndexEntry) e.nextElement();

            if (last == null) {
                last = ie.name().substring(0, ie.name().lastIndexOf('.')+1);
                p.println("<li><a href=\"Classes.html#"+ last +
                    "\">" + last + "</a>");
            } else if (!last.equals(
                    ie.name().substring(0, ie.name().lastIndexOf('.')+1))) {
                last = ie.name().substring(0, ie.name().lastIndexOf('.')+1);
                p.println("<li><a href=\"Classes.html#"+ last +
                    "\">" + last + "</a>");
            }
        }
        p.println("</ul>");
        p.println("<hr></body></html>");
    }

    /** This method is used to print the documentation for a class to a
     *  file (PrintStream), and to generate information for an index into
     *  an index file (typically .index).
     *  If either p or indexFile is null, then the corresponding file is not
     *  written.
     *
     *  @param p, nullOk; The PrintStream to output documentation to.
     *   if p is null, documentation is not written.
     *  @param indexFile, nullOk; The file to output index information to.
     *   if indexFile is null, index info is not written.
     *  @param cii, nullFatal; The class or interface to be processed.
     *  @see ec.edoc.IndexEntry.html
     */
    void outputClass(PrintStream p,
                     java.io.FileOutputStream indexFile,
                     ClassInterfaceInfo cii) {


        /* First of all we write out the index file for this class. */

        ByteArray ba = new ByteArray();

        if (indexFile == null) {
            System.out.println("null indexFile, not writing inedex");
        }


        if (indexFile != null) {

            /* We put the class itself out first so that
             * (a) it can go in the index (eventually)
             * (b) we can build a Package.html
             */
            ( new IndexEntry((ClassInterfaceInfo) cii) ).dump(ba);

            try {
                if (cii.fields() != null) {
                    for (int i = cii.fields().size(); i-- > 0; ) {
                        (new IndexEntry(
                            (FieldInfo) cii.fields().elementAt(i))
                        ).dump(ba);
                    }
                }
            } catch (MemberNotSupportedException e) {
                /* then we tried to get members from something that cannot
                   have that type of member, eg. fields from an EInterface */
            }
            try {
                if (cii.methods() != null) {
                    for (int i = cii.methods().size(); i-- > 0; ) {
                        (new IndexEntry(
                            (MethodInfo) cii.methods().elementAt(i))
                        ).dump(ba);
                    }
                }
            } catch (MemberNotSupportedException e) {
                /* then we tried to get members from something that cannot
                   have that type of member, eg. fields from an EInterface */
            }
            try {
                if (cii.emethods() != null) {
                    for (int i = cii.emethods().size(); i-- > 0; ) {
                        (new IndexEntry(
                            (EMethodInfo) cii.emethods().elementAt(i))
                        ).dump(ba);
                    }
                }
            } catch (MemberNotSupportedException e) {
                /* then we tried to get members from something that cannot
                   have that type of member, eg. fields from an EInterface */
            }
            try {
                if (cii.constructors() != null) {
                    for (int i = cii.constructors().size(); i-- > 0; ) {
                        (new IndexEntry(
                            (ConstructorInfo) cii.constructors().elementAt(i))
                        ).dump(ba);
                    }
                }
            } catch (MemberNotSupportedException e) {
                /* then we tried to get members from something that cannot
                   have that type of member, eg. fields from an EInterface */
            }

            /* We now create a Nil index entry, to mark the end of file. */
            new IndexEntry().dump(ba);

            try {
                indexFile.write(ba.getContents());
            } catch (java.io.IOException e) {
                System.err.println("Error writing to index file. " +
                    "File may be incomplete and / or invalid");
            }
        }


        /* Having added all the index information, we now spit out the doc
         * file, or return if no doc required. */
        if (p == null) {
            return;
        }

        String classTitle = null;
        String classKeyword = null;

        if (cii instanceof JavaClassInfo) {
            classTitle = "Class";
            classKeyword = "class ";
        } else if (cii instanceof EClassInfo) {
            classTitle = "E Class";
            classKeyword = "eclass ";
        } else if (cii instanceof EInterfaceInfo) {
            classTitle = "E Interface";
            classKeyword = "einterface ";
        } else if (cii instanceof JavaInterfaceInfo) {
            classTitle = "Interface";
            classKeyword = "interface ";
        }

        printHTMLPreamble(p, classTitle + " " + cii.name());

        // Print out all the class info stuff
        p.print("<dl><dd><code>");

        p.print(Info.getModifierString(
            cii.modifiers() & (Info.PUBLIC | Info.ABSTRACT | Info.FINAL)));

        p.print(classKeyword);

        p.print(cii.name());

        if (cii.getExtends() != null) {
            p.print(" extends " + getLongLink(cii.getExtends()));
        }

        if (cii.getImplements() != null && cii.getImplements().size() > 0) {
            p.print(" implements ");

            Enumeration ile = cii.getImplements().elements();
            while (ile.hasMoreElements()) {
                p.print(getLongLink((String)ile.nextElement()));
                if (ile.hasMoreElements()) {
                    p.print(", ");
                }
            }
        }

        p.print("</code><pre>\n</pre>");


        p.println(cii.comment().description());
        p.println("</dl><hr>");

        String[] headings = new String[4];
        String[] staticColours = new String[4];
        String[] instanceColours = new String[4];
        Enumeration[] contents = new Enumeration[4];

        /*fields*/
        headings[0] = "<h2><a name=\"variables\">Variables</a></h2>";
        try {
            contents[0] = (cii.fields() == null) ? null :
                                                   cii.fields().elements();
        } catch (MemberNotSupportedException e) {
            contents[0] = null;
        }
        staticColours[0] = STATIC_VAR_COLOUR;
        instanceColours[0] = INSTANCE_VAR_COLOUR;

        /*constructors*/
        headings[1] = "<h2><a name=\"constructors\">Constructors</a></h2>";
        try {
        contents[1] = (cii.constructors() == null) ? null :
                            cii.constructors().elements();
        } catch (MemberNotSupportedException e) {
            contents[1] = null;
        }
        staticColours[1] = CONSTRUCTOR_COLOUR;
        instanceColours[1] = CONSTRUCTOR_COLOUR;

        /*emethods*/
        headings[2] = "<h2><a name=\"emethods\">E Methods</a></h2>";
        try {
        contents[2] = (cii.emethods() == null) ? null :
                            cii.emethods().elements();
        } catch (MemberNotSupportedException e) {
            contents[2] = null;
        }
        staticColours[2] = STATIC_METHOD_COLOUR;
        instanceColours[2] = INSTANCE_METHOD_COLOUR;

        /*methods*/
        headings[3] = "<h2><a name=\"methods\">Methods</a></h2>";
        try {
        contents[3] = (cii.methods() == null) ? null :
                            cii.methods().elements();
        } catch (MemberNotSupportedException e) {
            contents[3] = null;
        }
        staticColours[3] = STATIC_METHOD_COLOUR;
        instanceColours[3] = INSTANCE_METHOD_COLOUR;


        for (int i = 0; i < 4; i++) {
            if (contents[i] == null || !contents[i].hasMoreElements()) {
                continue;
            }
            p.println(headings[i]);

            while (contents[i] != null && contents[i].hasMoreElements()) {
                MemberInfo mi = (MemberInfo) contents[i].nextElement();
                p.println("<p><code>");
                if ((mi.modifiers() & Info.STATIC) != 0) {
                    p.print(Bullet(staticColours[0], myImages));
                } else {
                    p.print(Bullet(instanceColours[i], myImages));
                }
                p.print(getAnchor(mi.name()));
                if (mi instanceof MethodInfo) {
                    p.print(getAnchor(mi.name() +
                        ((MethodInfo)mi).internalType()));
                }
                p.println(buildString(mi));
                p.println("</code>");
                if (mi.comment().description() != null) {
                    p.print("<blockquote>");
                    p.print(mi.comment().description());
                    dealWithCommands(p,
                                     mi.comment().getAtCommands(),
                                     cii.typeTable());
                    p.println("</blockquote>");
                }
            }

            p.println("<pre>\n</pre>");
        }

        p.println("<hr></body>");
        p.println("</html>");

    }

    /** this method deals with the AtCommands which might have been
     *  attached to an Info.
     *
     *  @param p, nullFatal; the destination for the HTML output
     *  @param e, nullFatal; the AtCommands to process. Assume e is not
     *   null since it most likely came from Comment.getAtCommands Actually
     *   since that's where it's supposed to have come from, we assume that
     *   it's an Enumeration of AtCommands.
     *  @param t, nullFatal; a TypeTable used to check unqualified type
     *   names when building, for instance, AtSee or AtThrows commands.
     *  @see Comment#getAtCommands
     */
    private void dealWithCommands(PrintStream p, Enumeration e,
            TypeTable typetable) {

        Vector sees = null;
        Vector params = null;
        Vector returns = null;
        Vector exceptions = null;

        while (e.hasMoreElements()) {
            AtCommand command = (AtCommand) e.nextElement();
            if (command instanceof AtParam) {
                if (params == null) {
                    params = new Vector();
                }
                params.addElement(command);
            } else if (command instanceof AtReturns) {
                if (returns == null) {
                    returns = new Vector();
                }
                returns.addElement(command);
            } else if (command instanceof AtSee) {
                if (sees == null) {
                    sees = new Vector();
                }
                sees.addElement(command);
            } else if (command instanceof AtThrows) {
                if (exceptions == null) {
                    exceptions = new Vector();
                }
                exceptions.addElement(command);
            }
            // otherwise we ignore it...
        }

        if (params != null) {
            p.print("<p><strong>Parameters:</strong><ul>");
            e = params.elements();
            while (e.hasMoreElements()) {
                AtParam param = (AtParam) e.nextElement();
                p.print("<li><strong>" + param.name()+"</strong> ");
                p.print(param.description()+"</p>");
                Boolean test = param.trust();
                if (test == Boolean.TRUE) {
                    p.print(" is trusted");
                } else if (test == Boolean.FALSE) {
                    p.print(" is suspect");
                } else { /* test == null */
                    p.print(" has unspecifed trust constraints");
                }
                p.print(" and ");
                test = param.nullability();
                if (test == Boolean.TRUE) {
                    p.print(" may be null");
                } else if (test == Boolean.FALSE) {
                    p.print(" must not be null");
                } else { /* test == null */
                    p.print(" has unspecifed constraints on null");
                }
            }
            p.println("</ul>");
        }
        if (returns != null) {
            p.print("<p><strong>Returns:</strong><ul>");
            e = returns.elements();
            AtReturns returnz = (AtReturns) e.nextElement();
            p.print("<li>"+returnz.description()+"<p>");
            p.print("The return value");
            Boolean test = returnz.trust();
            if (test == Boolean.TRUE) {
                p.print(" is trusted");
            } else if (test == Boolean.FALSE) {
                p.print(" is suspect");
            } else { /* test == null */
                p.print(" has unspecifed trust constraints");
            }
            p.print(" and ");
            test = returnz.nullability();
            if (test == Boolean.TRUE) {
                p.print(" might be null");
            } else if (test == Boolean.FALSE) {
                p.print(" will not be null");
            } else { /* test == null */
                p.print(" has unspecifed constraints on null");
            }
            if (e.hasMoreElements()) {
                System.err.println("Warning: Current Doc Comment has" +
                    "more than one @returns command.");
            }
            p.println("</ul>");
        }
        if (exceptions != null) {
            p.print("<p><strong>Throws:</strong><ul>");
            e = exceptions.elements();
            while (e.hasMoreElements()) {
                AtThrows throwz = (AtThrows) e.nextElement();
                p.print("<li>" + getTypeCheckedShortLink(typetable,
                                                         throwz.name())+"\n");
                p.print(throwz.description());
            }
            p.println("</ul>");
        }
        if (sees != null) {
            p.print("<p><strong>See Also:</strong><ul>");
            e = sees.elements();
            while (e.hasMoreElements()) {
                AtSee see = (AtSee) e.nextElement();
                p.print("<li>" + getTypeCheckedShortLink(typetable,
                                                         see.link()) +"\n");
                p.print(see.description());
            }
            p.println("</ul>");
        }
    }

    private static String Bullet(String colourName, boolean images) {
        //System.out.println("Bullet");

        if (images) {  // XXX Images exist
            return ("<img src=\"images/" + colourName + "-ball.gif\"> ");
        } else {
            return ("<font color=" + colourName + ">==> </font>");
        }
    }


    private static String getShortName(String FQname) {
        //System.out.println("getShortName");
        /* lastIndexOf returns -1 if not found. helpful :) */
        return FQname.substring(FQname.lastIndexOf('.') + 1);
    }

    private static String getTypeCheckedShortLink(TypeTable tt, String s) {
        //System.out.println("getTypeCheckedShortName");
        /* lastIndexOf returns -1 if not found. helpful :) */

        //System.out.println("Getting link to \""+s+"\"");

        String section = null;

        int hashpos = s.lastIndexOf('#');

        if (hashpos != -1) {
            section = s.substring(hashpos+1);
            s = s.substring(0, hashpos);
            //System.out.println("linking to section "+section);
        }

        if (s.endsWith(".html")) {
            s = s.substring(0, s.length() - 5);
            //System.out.println("check "+s);
        }

        //System.out.println("@"+s);
        if (s.lastIndexOf('.') != -1) {
            //System.out.println("is FQ");
            /* then we have a fully qualified type. */
        } else {
            String fq = tt.get(s);
            //System.out.println("fq is "+ fq);
            if (fq != null) {
                s = fq;
            }
        }

        String link;
        if (section == null) {
            link = "<a href=\"" +s+ ".html\">" +getShortName(s)+ "</a>";
        } else {
            link = "<a href=\"" +s+ ".html#"+section+"\">"
                    +getShortName(s)+"."+section+ "</a>";
        }

        //System.out.println(s);

        return link;
    }

    /** This method Takes a FQ type name, and returns either the text for that
     *  typename, or an html fragment to link to the appropriate html file
     *  if this is a class type */
    /* Slightly kludgy. I guess this info should probably go into TypeTable*/
    /* First we check if this is an array - in which case 'tmp' is left
     * holding the name of the array type.
     * if it's a primitve type, we return the value including any []
     * otherwise it's a class type, so we build a link without [] and return
     * that, with a short name including []
     */
    private static String getCheckedTypeLink(String FQname) {
        String tmp = FQname;
        while (tmp.endsWith("[]")) {
            tmp = tmp.substring(0, tmp.length() - 2);
        }
        if (tmp.equals("void")) {
            return "void";
        }
        if (tmp.equals("boolean")) {
            return FQname;
        }
        if (tmp.equals("short")) {
            return FQname;
        }
        if (tmp.equals("long")) {
            return FQname;
        }
        if (tmp.equals("int")) {
            return FQname;
        }
        if (tmp.equals("byte")) {
            return FQname;
        }
        if (tmp.equals("char")) {
            return FQname;
        }
        if (tmp.equals("double")) {
            return FQname;
        }
        if (tmp.equals("float")) {
            return FQname;
        }
        if (tmp.equals("char")) {
            return FQname;
        }
        if (tmp.equals("efalse")) {
            return FQname;
        }
        if (tmp.equals("etrue")) {
            return FQname;
        }
        return "<a href=\"" +tmp+ ".html\">" +getShortName(FQname)+ "</a>";
    }

    /** This method returns a string which is the short (readable) name of
     *  the type html linked to the full type def. */
    private static String getShortLink(String FQname) {
        //System.out.println("getShortLink");
        return "<a href=\"" +FQname+ ".html\">" +getShortName(FQname)+ "</a>";
    }
    private static String getLongLink(String FQname) {
        //System.out.println("getLongLink");
        return "<a href=\"" +FQname+ ".html\">" +FQname+ "</a>";
    }

    /** This method returns an anchor to link to */
    private static String getAnchor(String s) {
        //System.out.println("getAnchor");

        return "<a name=\"" + s + "\"></a>";
    }

    /** buildString is used to print out information about each member of
     *  the class to a string, which can then be stored etc.
     */
    private String buildString(MemberInfo mi) {
        //System.out.println("buildString");

        StringBuffer sb = new StringBuffer();

        // Print Modifiers
        if (mi.modifiers() != 0) {
            sb.append(mi.getModifierString());
        }

        // Print keyword || [return] Type as required
        if (mi instanceof FieldInfo) {
            sb.append(getCheckedTypeLink(mi.type()) + " ");
        } else if (mi instanceof EMethodInfo) {
            sb.append("emethod");
        }

        sb.append(mi.name());

        // Print out formal parameters as required
        // Everything except fields can have them - eforall should only have one
        // XXX this should perhaps be somewhere else, in MethodInfo?
        if (mi instanceof MethodInfo) {
            MethodInfo Mi = (MethodInfo) mi;
            sb.append("(");

            if ((Mi.parameterNames() != null) && (Mi.parameterTypes() != null)) {
                Enumeration en = Mi.parameterNames().elements();
                Enumeration et = Mi.parameterTypes().elements();
                while (et.hasMoreElements() && en.hasMoreElements()) {
                    sb.append(getCheckedTypeLink((String)et.nextElement()));
                    sb.append(" " + en.nextElement());
                    if (et.hasMoreElements() && en.hasMoreElements()) {
                        sb.append(", ");
                    }
                }
            }

            sb.append(")");
        }

        // Print throws clauses
        if ((mi instanceof JavaMethodInfo) || (mi instanceof ConstructorInfo)
                && (((MethodInfo)mi).getThrows() != null)) {
            sb.append(" throws ");

            Enumeration tle = ((MethodInfo)mi).getThrows().elements();
            while (tle.hasMoreElements()) {
                sb.append(getShortLink((String)tle.nextElement()));
                if (tle.hasMoreElements()) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }
}
