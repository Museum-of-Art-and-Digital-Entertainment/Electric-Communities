/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

import java.util.Hashtable;

/** This class provides a method for turning a string representing an
 *  '@' command into an AtCommand of the appropriate type.
 *
 *  @see AtCommand;
 */
class AtDispatcher {

    /* this is the collection of strings which are valid AtCommands */
    private static final String Aparam = "@param".intern();
    private static final String Areturns = "@returns".intern();
    private static final String Areturn = "@return".intern();
    private static final String Athrows = "@throws".intern();
    private static final String Aexception = "@exception".intern();
    private static final String Asee = "@see".intern();
    private static final String Aversion = "@version".intern();
    private static final String Aauthor = "@author".intern();
    private static final String Asince = "@since".intern();
    private static final String Adeprecated = "@deprecated".intern();
    private static final String Adepricated = "@depricated".intern();

    /** this method replaces a cute hashtable trick.
     * it was part of the grand baroque version of dealing with @commands.
     * the whole scheme was still-born and was swiftly run onto the rocks
     * when it tried to mix its metaphors. this is not as cute, but should
     * actually work ;-)
     */
    private static AtCommand getCommand(String commandname)
            throws StringNotUnderstoodException {
        /* this gets back some of the efficiency of the first scheme,
         * which would have been cute, by interning strings so that this
         * list is much quicker than it would otherwise have been. */

        //System.out.println("AtDispatcher.getCommand("+commandname+")");

        if (commandname.endsWith(";") || commandname.endsWith(",")) {
            commandname = commandname.substring(0, commandname.length() - 1);
        }

        String lookup = commandname.intern();

        //System.out.println("AtDispatcher.getCommand("+lookup+")");

        if (lookup == Aparam) {
            return new AtParam();
        } else if (lookup == Areturns || lookup == Areturn) {
            return new AtReturns();
        } else if (lookup == Athrows || lookup == Aexception) {
            return new AtThrows();
        } else if (lookup == Asee) {
            return new AtSee();
        } else if (lookup == Aversion) {
            //return new AtVersion();
            throw new StringNotUnderstoodException(lookup);
        } else if (lookup == Aauthor) {
            //return new AtAuthor();
            throw new StringNotUnderstoodException(lookup);
        } else if (lookup == Asince) {
            //return new AtSince();
            throw new StringNotUnderstoodException(lookup);
        } else if (lookup == Adeprecated || lookup == Adepricated) {
            //return new AtDeprecated();
            throw new StringNotUnderstoodException(lookup);
        } else {
            throw new StringNotUnderstoodException(lookup);
        }
    }

    /** this method can be used to determine if a string is a valid
     * @command or not.
     * it is probably more efficient to do this elsewhere (since we're
     * really looking at a char array, not a string, but this keeps all
     * the knowledge about which strings are valid here.
     */
    static boolean isCommand(String commandname) {

        //System.out.println("AtDispatcher.isCommand("+commandname+")");

        //if (commandname.endsWith(";") || commandname.endsWith(",")) {
        //    commandname = commandname.substring(0, commandname.length() - 1);
        //}

        String lookup = commandname.intern();
        if (lookup == Aparam
                || lookup == Areturns  || lookup == Areturn
                || lookup == Athrows || lookup == Aexception
                || lookup == Asee
                || lookup == Aversion
                || lookup == Aauthor
                || lookup == Asince
                || lookup == Adeprecated || lookup == Adepricated) {
            return true;
        } else {
            return false;
        }
    }

    static AtCommand dispatch(String commandString)
            throws StringNotUnderstoodException {

        int i = commandString.indexOf(' ');
        int others = commandString.indexOf(';');
        if (others < i && others != -1) {
            i = others;
        }
        others = commandString.indexOf(',');
        if (others < i && others != -1) {
            i = others;
        }

        if (i == -1) {
            i = commandString.length();
        }
        String s = commandString.substring(0, i);
        AtCommand command = getCommand(s);

        /*start looking from first space == i; */
        int words = 0;
        boolean inword = false;
        int startOfWord = 0;
        char c;

        /* OK, explanation time;
         * we search through the string collecting words as annotations.
         * we stop if we've looked at more words than we should lookahead
         *         or we get to the end of the string
         *         or we hit a ';', but only if we are supposed to stop on ';'
         * of course we loop on !(when we should stop). hence;
         */
        while (words <= command.lookahead() &&
                i < commandString.length()) {

            //System.out.print(c);System.out.flush();
            c = commandString.charAt(i);

            if (c == ';' && command.expectSemiColon()) {
                /* then we should stop looking */
                if (startOfWord == 0) {
                    /* then we have the situation where someone has done
                     * "@command;" and we are at the ';'. in this case
                     * we don't really have an annotation, so we don't want
                     * to add one */
                } else {
                    doAnnotationAddition(command, commandString,startOfWord,i);
                }
                /* terminate the search for annotations */
                i++; /* since we're going to break */
                i++; /* since we're going to ignore the ';' */
                break;
            } else if (!inword && c != ' ') {
                inword = true;
                words++;
                startOfWord = i;
            } else if (inword && c == ' ') {
                inword = false;
                doAnnotationAddition(command, commandString, startOfWord, i);
            }
            i++;
        }

        /*
        System.out.println("\ndispatchLoopOver "+i);
        System.out.println("dispatchLoopOver \""+
            commandString.substring(0,i-1)+"\"");
        System.out.println("dispatchLoopOver \""+
            commandString.substring(i-1)+"\"");
        */

        command.addDescription(commandString.substring(i-1));

        return command;
    }

    private static void doAnnotationAddition(AtCommand command,
            String commandString, int startOfWord, int index)
            throws StringNotUnderstoodException {

        String annotation;
        if (index >= 0 && commandString.charAt(index - 1) == ',') {
            /* remove the ',' */
            annotation = commandString.substring(startOfWord, index - 1);
        } else {
            annotation = commandString.substring(startOfWord, index);
        }

        //System.out.println("Adding annotation ("+annotation+")");

        command.addAnnotation(annotation);
    }

}

