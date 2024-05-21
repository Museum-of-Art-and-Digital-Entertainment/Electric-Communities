/*
 * @(#)HtmlDocWriter.java   1.22 98/03/18
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import sun.tools.javadoc.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.MessageFormat;


/**
* Class for the Html Format Code Generation specific to JavaDoc.
* This Class contains methods related to the Html Code Generation which
* are used by the Sub-Classes: PackageIndexWriter, PackageWriter,
* ClassWriter which are Standard Doclets.
* Super-Class is HtmlWriter.
*
* @since JDK1.2
* @author Atul M Dambalkar
* @author Robert Field
*/

public class HtmlDocWriter extends HtmlWriter {

    protected final String htmlFilename;

    /**
     * All the user given options on the command line.
     */
    public static Configuration configuration;

    private static ResourceBundle messageRB;

    /**
     * Constructor. Initializes the destination file name through1 the super
     * class HtmlWriter.A
     *
     * @param filename String file name.
     */
    public HtmlDocWriter(String filename) throws IOException {
        super(configuration.destDirName + filename,
              configuration.docencoding);
        htmlFilename = filename;
        notice("doclet.Generating_0", filename);
    }

    /**
     * Print Html Hyper Link.
     *
     * @param link String name of the file.
     * @param where Position of the link in the file.
     * @param tag Tag for the link.
     */
    public void printHyperLink(String link, String where, String tag) {
        print("<a href=\"" + link);
        if (where != null) {
            print("#" + where);
        }
        print("\">" + tag + "</a>");
    }

    /**
     * Print Html Hyper Link, with target frame.
     *
     * @param link String name of the file.
     * @param target Position of the link in the file.
     * @param tag Tag for the link.
     */
    public void printTargetHyperLink(String link, String target, String tag) {
        print("<a href=\"" + link + "\"" + " target=\"" + target + "\">"
              + tag + "</a>");
    }

    /**
     * Print link without positioning in the file.
     */
    public void printHyperLink(String link, String tag) {
        printHyperLink(link, null, tag);
    }

    /**
     * Print Class Link with the generated file name with position.
     */
    public void printClassLink(ClassDoc cd, String where, String tag) {
        if (configuration.linkall || cd.isIncluded()) {
            printHyperLink(cd.qualifiedName() + ".html", where, tag);
        } else {
            print(tag);
        }
    }

    /**
     * Print Class Link with the generated file name without position.
     */
    public void printClassLink(ClassDoc cd, String tag) {
        printClassLink(cd, null, tag);
    }

    /**
     * Print Class link.
     */
    public void printClassLink(ClassDoc cd) {
        printClassLink(cd, cd.isIncluded()? cd.name(): cd.qualifiedName());
    }

    /**
     * Print Class link, with tag as qualified name.
     */
    public void printQualifiedClassLink(ClassDoc cd) {
        printClassLink(cd, cd.qualifiedName());
    }

    /**
     * Print Class link, with only class name as the link and prefixing
     * plain package name.
     */
    public void printPreQualifiedClassLink(ClassDoc cd) {
        String pkgName = cd.containingPackage().name();
    if (pkgName.length() > 0) {
            print(pkgName);
            print('.');
        }
        printClassLink(cd, cd.name());
    }

    /**
     * Print Class link, with target frame.
     */
    public void printTargetClassLink(ClassDoc cd, String target) {
        printTargetHyperLink(cd.qualifiedName() + ".html",
                             target, cd.name());
    }

    /**
     * Print class link.
     */
    public void printMemberHyperLink(String link, String tag) {
        printHyperLink(link, tag);
    }

    /**
     * Print link for individual package file.
     */
    public void printPackageLink(PackageDoc pkg, String linkLabel) {
        String name = "package-" + pkg.name() + ".html";
        if (configuration.frame) {
        printTargetHyperLink(name, "packageFrame", linkLabel);
        } else {
        printHyperLink(name, linkLabel);
        }
    }

    /**
     * Print link for individual package file.
     */
    public void printPackageLink(PackageDoc pkg) {
        printPackageLink(pkg, pkg.name());
    }

    /**
     * Print the html file header.
     *
     * @param title String title for the generated html file.
     */
    public void printHeader(String title) {
        printPartialHeader(title);
        if (configuration.oneOne) {
            body();
        } else {
            body("#FFFFFF");
        }
        anchor("_top_");
        println();
    }

    /**
     * Print some part of the Html file header.
     */
    public void printPartialHeader(String title) {
        println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
        println("<!--NewPage-->");
        html();
        head();
        print("<!-- Generated by javadoc on ");
        println(today());
        println("-->");
        title();
        println(title);
        titleEnd();
        headEnd();
    }

    /**
     * Print user specified header and the footer.
     *
     * @param header boolean if true print the header else footer.
     */
    public void printUserHeaderFooter(boolean header) {
        em();
        if (header) {
            print(configuration.header);
        } else {
            print(configuration.footer);
        }
        emEnd();
    }

    /**
     * Print the appropriate spaces to format the class tree in the class page.
     */
    public String spaces(int len) {
        String space = "";

        for (int i = 0; i < len; i++) {
            space += " ";
        }
        return space;
    }

    /**
     * Print the user specified bottom.
     */
    public void printBottom() {
        hr();
        print(configuration.bottom);  // usually empty
    }

    /**
     * Print the html file footer. If there is a user given footer, use it
     * or else print standard html completion.
     */
    public void printFooter() {
        bodyEnd();
        htmlEnd();
    }

    /**
     * Print the html tag for image with all the parameters.
     */
    public void printImage(String imggif, String imgname,
                             int width, int height) {
        img(imggif, imgname, width, height);
    }

    /**
     * Print the see tags information given the doc comment.
     *
     * @param doc Doc doc
     * @see sun.tools.javadoc.Doc
     */
    protected void printSeeTags(Doc doc) {
       SeeTag[] sees = doc.seeTags();
       if (sees.length > 0) {
       dt();
       bold(getText("doclet.See_Also"));
       dd();
       for (int i = 0; i < sees.length; ++i) {
           SeeTag see = sees[i];
           ClassDoc refClass = see.referencedClass();
           MemberDoc refMem = see.referencedMember();
           String refMemName = see.referencedMemberName();
           if (i > 0) {
           print(", ");
           }
           if (refClass == null) {
           // nothing to link to, just use text
           print(see.text());
           } else if (refMemName == null) {
           // class reference
           printClassLink(refClass);
           } else if (refMem == null) {
           // can't find the member reference
           print(see.text());
           } else if (refMem instanceof ExecutableMemberDoc) {
           // executable member reference
           printClassLink(refClass,
                                  refMem.name()+((ExecutableMemberDoc)refMem).signature(),
                                  refMemName);
           } else {
           // member reference
           printClassLink(refClass, refMemName, refMem.name());
           }
       }
       }
    }


    protected void navGap() {
        space();
        print('|');
        print(' ');
    }

    protected void navLinks(boolean header) {
        table();
        tr();
        td(); tdEnd();
        td(); tdEnd();
        td(); tdEnd();
        td(); tdEnd();
        trEnd();

        tr();
        tdColspan(3);
        if (configuration.packages.length > 0) {
            navLinkContents();
            navGap();
            navLinkPackage();
            navGap();
        }
        navLinkClass();
        navGap();
        if(configuration.createTree) {
            navLinkTree();
            navGap();
        }
        if(!configuration.oneOne && !configuration.noDeprecatedList) {
            navLinkDeprecated();
            navGap();
        }
        if(configuration.createIndex) {
            navLinkIndex();
            navGap();
        }
        if (!configuration.nohelp) {
            navLinkHelp();
        }
        tdEnd();

        tdAlignRowspan("right", 2);
        printUserHeaderFooter(header);
        tdEnd();
        trEnd();

        tr();
        td();
        font("-2");
        navLinkPrevious();
        navGap();
        navLinkNext();
        fontEnd();
        tdEnd();

        td();
        font("-2");
        navShowLists();
        navGap();
        navHideLists();
        fontEnd();
        tdEnd();

        td(); tdEnd();

        trEnd();
        tableEnd();
    }

    /**
     * Print packages contents link
     */
    protected void navLinkContents() {
        if (configuration.frame) {
            printTargetHyperLink("packages.html",
                                 "packageListFrame",
                                 getText("doclet.Contents"));
        } else {
            printHyperLink("packages.html",
                           getText("doclet.Contents"));
        }
    }

    /**
     * Print this package link
     */
    protected void navLinkPackage(PackageDoc pkg) {
        printPackageLink(pkg, getText("doclet.Package"));
    }

    /**
     * Print this package link
     */
    protected void navLinkPackage() {
        printText("doclet.Package");
    }

    /**
     * Print link for previous file.
     *
     * @param next String previous link name
     */
    public void navLinkPrevious(String prev) {
        if (prev != null) {
            printHyperLink(prev, getText("doclet.Previous"));
        } else {
            print(getText("doclet.Previous"));
        }
    }

    /**
     * Print previous item link
     */
    protected void navLinkPrevious() {
        navLinkPrevious(null);
    }

    /**
     * Print link for next file.
     *
     * @param next String next link name
     */
    public void navLinkNext(String next) {
    if (next != null) {
            printHyperLink(next, getText("doclet.Next"));
        } else {
            print(getText("doclet.Next"));
        }
    }

    /**
     * Print next item link
     */
    protected void navLinkNext() {
        navLinkNext(null);
    }

    /**
     * Print show lists switch
     */
    protected void navShowLists(String link) {
        printTargetHyperLink(link, "_top",
                             getText("doclet.SHOW_LISTS"));
    }

    /**
     * Print show lists switch
     */
    protected void navShowLists() {
        navShowLists("frame.html");
    }

    /**
     * Print hide lists switch
     */
    protected void navHideLists(String link) {
        printTargetHyperLink(link, "_top",
                             getText("doclet.HIDE_LISTS"));
    }

    /**
     * Print hide lists switch
     */
    protected void navHideLists() {
        navHideLists(htmlFilename);
    }

    protected void navLinkTree(PackageDoc pkg) {
        printHyperLink("package-tree-" + pkg + ".html", null,
                       getText("doclet.Tree"));
    }

    /**
     * Print class/interface hierarchy link
     */
    protected void navLinkMainTree(String link) {
        if (configuration.frame) {
            printTargetHyperLink("tree.html", "classFrame", link);
        } else {
            printHyperLink("tree.html",
                           configuration.oneOne?
                                   getText("doclet.Class_Hierarchy"):
                                   link);
        }
    }

    /**
     * Print class/interface hierarchy link
     */
    protected void navLinkTree() {
        navLinkMainTree(getText("doclet.Tree"));
    }

    /**
     * Print class page indicator
     */
    protected void navLinkClass() {
        printText("doclet.Class");
    }

    /**
     * Print deprecated API link
     */
    protected void navLinkDeprecated() {
        if (configuration.frame) {
            printTargetHyperLink("deprecatedlist.html",
                                 "classFrame",
                                 getText("doclet.Deprecated"));
        } else {
            printHyperLink("deprecatedlist.html",
                           getText("doclet.Deprecated"));
        }
    }

    /**
     * Print link for generated index file,
     * depending upon the user option.
     */
    protected void navLinkIndex() {
        if (configuration.frame) {
            printTargetHyperLink(!configuration.breakIndex?
                                 "index.html" : "1-index.html",
                                 "classFrame",
                                 getText("doclet.Index"));
        } else {
            printHyperLink(!configuration.breakIndex?
                           "index.html" : "1-index.html",
                           getText("doclet.Index"));
        }
    }

    /**
     * Print help file link, considering the user options.
     */
    protected void navLinkHelp() {
        printHyperLink(configuration.helpfile == null?
                       "help.html":
                       configuration.helpfile,
                       getText("doclet.Help"));
    }


    public void printText(String key) {
        print(getText(key));
    }

    public void printText(String key, String a1) {
        print(getText(key, a1));
    }

    public void boldText(String key) {
        bold(getText(key));
    }

    /**
     * Return the first sentence of a string, where a sentence ends
     * with a period followed be white space.
     */
    public String firstSentence(String s) {
        if (s == null)
            return null;
        int len = s.length();
        boolean period = false;
        for (int i = 0 ; i < len ; i++) {
            switch (s.charAt(i)) {
                case '.':
                    period = true;
                    break;
                case ' ':
                case '\t':
                case '\n':
                    if (period) {
                        return s.substring(0, i);
                    }
                    break;
                default:
                    period = false;
            }
        }
        return s;
    }

    /**
     * Print tag information
     */
    protected void generateTagInfo(Doc doc, String path) {
        Tag[] sinces = doc.tags("since");
        Tag[] sees = doc.seeTags();
        Tag[] authors;
        Tag[] versions;
        if (configuration.showAuthor) {
            authors = doc.tags("author");
        } else {
            authors = new Tag[0];
        }
        if (configuration.showVersion) {
            versions = doc.tags("version");
        } else {
            versions = new Tag[0];
        }
        if (sinces.length > 0
            || sees.length > 0
            || authors.length > 0
            || versions.length > 0 ) {
            dl();
            if (sinces.length > 0) {
                // There is going to be only one Since tag.
                dt();
                boldText("doclet.Since");
                print(' ');
                dd();
                println(sinces[0].text());
                ddEnd();
            }
            if (versions.length > 0) {
                // There is going to be only one Version tag.
                dt();
                boldText("doclet.Version");
                print(' ');
                dd();
                println(versions[0].text());
                ddEnd();
            }
            for (int i = 0; i < authors.length; ++i) {
                dt();
                boldText("doclet.Author");
                print(' ');
                dd();
                println(authors[i].text());
                ddEnd();
            }
            printSeeTags(doc);

            if(doc instanceof ClassDoc && path != null){ //new
               dt(); //new
           boldText("doclet.Source_Code"); //new
               dd();//new
               printHyperLink(path, null, doc.name() + ".java"); //new
               ddEnd(); //new
        }
            dlEnd();
        }
    }

    /**
     * Print the today info., depending upon user option
     */
    public String today() {
        if (configuration.nodate) {
            return "TODAY";
        }
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        return calendar.getTime().toString();
    }

    /**
     * Initialize ResourceBundle
     */
    static void initResource() {
    try {
        messageRB =
        ResourceBundle.getBundle("sun.tools.javadoc.resources.doclets");
    } catch (MissingResourceException e) {
        throw new Error("Fatal: Resource for javadoc doclets is missing");
    }
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     */
    public static String getText(String key) {
    return getText(key, (String)null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public static String getText(String key, String a1) {
    return getText(key, a1, null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public static String getText(String key, String a1, String a2) {
    return getText(key, a1, a2, null);
    }

    /**
     * get and format message string from resource
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public static String getText(String key, String a1, String a2, String a3) {
    if (messageRB == null) {
        initResource();
    }
    try {
        String message = messageRB.getString(key);
        String[] args = new String[3];
        args[0] = a1;
        args[1] = a2;
        args[2] = a3;
        return MessageFormat.format(message, args);
    } catch (MissingResourceException e) {
        throw new Error("Fatal: Resource for javadoc is broken. There is no " + key + " key in resource.");
    }
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     */
    public static void error(String key) {
        Root.printError(getText(key));
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public static void error(String key, String a1) {
        Root.printError(getText(key, a1));
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public static void error(String key, String a1, String a2) {
        Root.printError(getText(key, a1, a2));
    }

    /**
     * Print error message, increment error count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public static void error(String key, String a1, String a2, String a3) {
        Root.printError(getText(key, a1, a2, a3));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     */
    public static void warning(String key) {
        Root.printWarning(getText(key));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public static void warning(String key, String a1) {
        Root.printWarning(getText(key, a1));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public static void warning(String key, String a1, String a2) {
        Root.printWarning(getText(key, a1, a2));
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public static void warning(String key, String a1, String a2, String a3) {
        Root.printWarning(getText(key, a1, a2, a3));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     */
    public static void notice(String key) {
        Root.printNotice(getText(key));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param a1 first argument
     */
    public static void notice(String key, String a1) {
        Root.printNotice(getText(key, a1));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     */
    public static void notice(String key, String a1, String a2) {
        Root.printNotice(getText(key, a1, a2));
    }

    /**
     * Print a message.
     *
     * @param key selects message from resource
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     */
    public static void notice(String key, String a1, String a2, String a3) {
        Root.printNotice(getText(key, a1, a2, a3));
    }

    /**
     * Just sufficient to take care of the 1.1 output format
     * Can be modified further
     */
    public void td(boolean width) {
        if(width == true) {
            println("<td width=\"50%\" align=right>");
        } else {
            println("<td>");
        }
    }

    /**
     * Just sufficient to take care of the 1.1 output format
     * Can be modified further
     */
    public void table() {
        println("<table BORDER=0 WIDTH=100%>");
    }

    public void tableEnd() {
        println("</table>");
    }

    public void tableIndexSummary() {
        println("<table border=\"1\" cellpadding=\"0\" " +
                "cellspacing=\"0\" width=100%>");
    }

    public void tableIndexDetail() {
        println("<table border=\"1\" cellpadding=\"3\" " +
                "cellspacing=\"0\" width=100%>");
    }

    public void tablePackageFrame() {
        println("<table width=\"1000\">");
    }

    public void tdIndex() {
        print("<td align=right valign=top width=1%>");
    }

    public void tableHeaderStart(String color, int span) {
        trBgcolor(color);
        tdColspan(span);
        font("+2");
    }

    public void tableInheritedHeaderStart() {
        tdColspan(2);
    }

    public void tableHeaderStart(String color) {
        tableHeaderStart(color, 2);
    }

    public void tableHeaderStart(int span) {
        tableHeaderStart("#CCCCFF", span);
    }

    public void tableHeaderStart() {
        tableHeaderStart(2);
    }

    public void tableHeaderEnd() {
        fontEnd();
        tdEnd();
        trEnd();
    }

    public void summaryRow(int width) {
         if (width != 0) {
             td("width=" + width + "%");
         } else {
             td();
         }
         //font("-1");
    }

    public void summaryRowEnd() {
         //fontEnd();
         tdEnd();
    }

    public void printIndexHeading(String str) {
        h2();
        print(str);
        h2End();
    }

    public void frameSet(String arg) {
        println("<frameset " + arg + ">");
    }

    public void frameSetEnd() {
        println("</frameset>");
    }

    public void frame(String arg) {
        println("<frame " + arg + ">");
    }

    public void frameEnd() {
        println("</frame>");
    }

    public void td(String str) {
        print("<td " + str + ">");
    }

}
