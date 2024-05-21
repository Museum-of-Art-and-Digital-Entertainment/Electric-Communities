/*
 * @(#)Configuration.java   1.10 98/03/18
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
import java.util.*;
import java.io.*;

/**
 * Configure the output based on the options.
 *
 * @author Robert Field
 */
public class Configuration {
    public String destDirName = "";
    public String docencoding = null;
    public String encoding = null;
    public String releaseName = "";
    public String javaPackagesHeader = "";
    public String otherPackagesHeader = "";
    public String footer = "";
    public String header = "";
    public String title = "";
    public String bottom = "";
    public String helpfile = null;
    public String sourcePath = null;
    public String srcRelativePath = null;
    public boolean showAuthor = false;
    public boolean showVersion = false;
    public boolean linkall = false;
    public boolean nohelp = false;
    public boolean breakIndex = false;
    public boolean createIndex = true;
    public boolean createTree = true;
    public boolean usersGuideLink = false; // User's guide link in package index
    public boolean oneOne = false;         // backwards compatible with JDK 1.1
    public boolean nodate = false;         // don't put dates in header
    public boolean noDeprecatedList = false;
    public boolean frame = false;
    public PackageDoc[] packages;

    public Configuration() {
    }

    public void setOptions(Root root) throws DocletAbortException {
        String[][] options = root.options();
        packages = root.specifiedPackages();
        for (int oi = 0; oi < options.length; ++oi) {
            String[] os = options[oi];
            String opt = os[0].toLowerCase();
            if (opt.equals("-d")) {
                String dd = os[1];
                File destDir = new File(dd);
                destDirName = addTrailingFileSep(dd);
                if (!destDir.exists()) {
                    HtmlDocWriter.error("doclet.destination_directory_not_found_0",
                                        destDir.getPath());
                    throw new DocletAbortException();
                }
            } else  if (opt.equals("-docencoding")) {
                docencoding = os[1];
            } else  if (opt.equals("-encoding")) {
                encoding = os[1];
            } else  if (opt.equals("-footer")) {
                footer =  os[1];
            } else  if (opt.equals("-header")) {
                header =  os[1];
            } else  if (opt.equals("-title")) {
                title =  os[1];
            } else  if (opt.equals("-bottom")) {
                bottom =  os[1];
            } else  if (opt.equals("-helpfile")) {
                helpfile =  os[1];
            } else  if (opt.equals("-author")) {
                showAuthor = true;
            } else  if (opt.equals("-nohelp")) {
                nohelp = true;
            } else  if (opt.equals("-version")) {
                showVersion = true;
            } else  if (opt.equals("-linkall")) {
                linkall = true;
            } else  if (opt.equals("-breakindex")) {
                breakIndex = true;
            } else  if (opt.equals("-noindex")) {
                createIndex = false;
            } else  if (opt.equals("-notree")) {
                createTree = false;
            } else  if (opt.equals("-frame")) {
                frame = true;
            } else  if (opt.equals("-xrelease")) {
                releaseName = os[1];
            } else  if (opt.equals("-xjavapackagesheader")) {
                javaPackagesHeader = os[1];
            } else  if (opt.equals("-xotherpackagesheader")) {
                otherPackagesHeader = os[1];
            } else  if (opt.equals("-xusersguidelink")) {
                usersGuideLink = true;
            } else  if (opt.equals("-x1.1")) {
                oneOne = true;
            } else  if (opt.equals("-xnodate")) {
                nodate = true;
            } else  if (opt.equals("-sourcepath")
                        || (opt.equals("-classpath"))) {
                sourcePath = os[1];
            } else if  (opt.equals("-relativepath")) {
                srcRelativePath = os[1];
            }
        }
        if (docencoding == null) {
            docencoding = encoding;
        }

        // hook configuration to writers
        HtmlDocWriter.configuration = this;
    }

    String addTrailingFileSep(String path) {
    String fs = System.getProperty("file.separator");
    if (!path.endsWith(fs))
        path += fs;

    return path;
    }

    /**
     * Check for doclet added options here.
     *
     * @return number of arguments to option. Zero return means
     * option not known.  Negative value means error occurred.
     */
    public int optionLength(String option) {
        option = option.toLowerCase();
        if (option.equals("-version") ||
            option.equals("-author") ||
            option.equals("-nodeprecated") ||
            option.equals("-list") ||
            option.equals("-noindex") ||
            option.equals("-notree") ||
            option.equals("-linkall") ||
            option.equals("-breakindex") ||
            option.equals("-frame") ||
            option.equals("-xusersguidelink") ||
            option.equals("-x1.1") ||
            option.equals("-xnodate")
                                     ) {
            return 1;
        } else if (option.equals("-help") ) {
            HtmlDocWriter.notice("doclet.usage");
            return 1;
        } else if (option.equals("-x") ) {
            HtmlDocWriter.notice("doclet.xusage");
            return -1; // so run will end
        } else if (option.equals("-docencoding") ||
                   option.equals("-footer") ||
                   option.equals("-header") ||
                   option.equals("-title") ||
                   option.equals("-bottom") ||
                   option.equals("-helpfile") ||
                   option.equals("-xrelease") ||
                   option.equals("-xjavapackagesheader") ||
                   option.equals("-xotherpackagesheader") ||
                   option.equals("-d") ||
                   option.equals("-relativepath") ) {
            return 2;
        } else {
            return 0;
        }
    }

}


