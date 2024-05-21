/*
 * @(#)HtmlWriter.java  1.14 98/03/18
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
import java.lang.*;
import java.util.*;

/**
* Class for the Html Format Code Generation.
* Initilizes PrintWriter with FileWriter, so that to use all the print
* related methods to generate the code to the named File through FileWriter.
*
* @since JDK1.2
* @author Atul M Dambalkar
*/


public class HtmlWriter extends PrintWriter {

    /**
     * Constructor, initializes PrintWriter with the FileWriter.
     *
     * @param filename File Name to which the PrintWriter will do the Output.
     * @exception IOException Exception raised by the FileWriter is passed on
     * to next level.
     */
    public HtmlWriter(String filename, String docencoding)
                       throws IOException, UnsupportedEncodingException {
        super(genWriter(filename, docencoding));
    }

    static Writer genWriter(String filename, String docencoding)
                       throws IOException, UnsupportedEncodingException {
        FileOutputStream fos = new FileOutputStream(filename);
        if (docencoding == null) {
            return new OutputStreamWriter(fos);
        } else {
            return new OutputStreamWriter(fos, docencoding);
        }
    }

    public void html() {
        println("<html>");
    }

    public void htmlEnd() {
        println("</html>");
    }

    public void body() {
        println("<body>");
    }

    public void body(String bgcolor) {
        println("<body bgcolor=\"" + bgcolor + "\">");
    }

    public void bodyEnd() {
        println("</body>");
    }

    public void title() {
        println("<title>");
    }

    public void titleEnd() {
        println("</title>");
    }

    public void ul() {
        println("<ul>");
    }

    public void ulEnd() {
        println("</ul>");
    }

    public void li() {
        print("<li>");
    }

    public void li(String type) {
        print("<li type=" + type + ">");
    }

    public void h1() {
        println("<h1>");
    }

    public void h1End() {
        println("</h1>");
    }

    public void h1(String text) {
        h1();
        println(text);
        h1End();
    }

    public void h2() {
        println("<h2>");
    }

    public void h2(String text) {
        h2();
        println(text);
        h2End();
    }

    public void h2End() {
        println("</h2>");
    }

    public void h3() {
        println("<h3>");
    }

    public void h3(String text) {
        h3();
        println(text);
        h3End();
    }

    public void h3End() {
        println("</h3>");
    }

    public void h4() {
        println("<h4>");
    }

    public void h4End() {
        println("</h4>");
    }

    public void h4(String text) {
        h4();
        println(text);
        h4End();
    }

    public void h5() {
        println("<h5>");
    }

    public void h5End() {
        println("</h5>");
    }

    public void img(String imggif, String imgname, int width, int height) {
        println("<img src=\"images/" + imggif + ".gif\""
              + " width=" + width + " height=" + height
              + " alt=\"" + imgname + "\">");
    }

    public void menu() {
        println("<menu>");
    }

    public void menuEnd() {
        println("</menu>");
    }

    public void pre() {
        println("<pre>");
    }

    public void preEnd() {
        println("</pre>");
    }

    public void hr() {
        println("<hr>");
    }

    public void hr(int size, int widthPercent) {
        println("<hr size=" + size + " width=\"" + widthPercent + "%\">");
    }

    public void bold() {
        print("<b>");
    }

    public void boldEnd() {
        print("</b>");
    }

    public void bold(String text) {
        bold();
        print(text);
        boldEnd();
    }

    public void italics(String text) {
        print("<i>");
        print(text);
        println("</i>");
    }

    public void space() {
        print("&nbsp;");
    }

    public void dl() {
        println("<dl>");
    }

    public void dlEnd() {
        println("</dl>");
    }

    public void dt() {
        print("<dt>");
    }

    public void dd() {
        print("<dd>");
    }

    public void ddEnd() {
        println("</dd>");
    }

    public void sup() {
        println("<sup>");
    }

    public void supEnd() {
        println("</sup>");
    }

    public void font(String size) {
        println("<font size=\"" + size + "\">");
    }

    public void fontEnd() {
        println("</font>");
    }

    public void fontColor(String color) {
        println("<font color=\"" + color + "\">");
    }

    public void center() {
        println("<center>");
    }

    public void centerEnd() {
        println("</center>");
    }

    public void aName(String name) {
        print("<a name=\"" + name + "\">");
    }

    public void aEnd() {
        print("</a>");
    }

    public void anchor(String name, String content) {
        aName(name);
        print(content);
        aEnd();
    }

    public void anchor(String name) {
        aName(name);
        aEnd();
    }

    public void p() {
        println();
        println("<p>");
    }

    public void br() {
        println();
        println("<br>");
    }

    public void address() {
        println("<address>");
    }

    public void addressEnd() {
        println("</address>");
    }

    public void head() {
        println("<head>");
    }

    public void headEnd() {
        println("</head>");
    }

    public void code() {
        println("<code>");
    }

    public void codeEnd() {
        println("</code>");
    }

    public void em() {
        println("<em>");
    }

    public void emEnd() {
        println("</em>");
    }

    public void tr() {
        println("<tr>");
    }

    public void trEnd() {
        println("</tr>");
    }

    public void td() {
        print("<td>");
    }

    public void tdEnd() {
        println("</td>");
    }

    public void trBgcolor(String color) {
        println("<tr BGCOLOR=\"" + color + "\">");
    }

    public void tdColspan(int i) {
        print("<td colspan=" + i + ">");
    }

    public void tdAlign(String str) {
        print("<td align=" + str + ">");
    }

    public void tdAlignRowspan(String str, int span) {
        print("<td align=" + str + " rowspan=" + span + ">");
    }

}
