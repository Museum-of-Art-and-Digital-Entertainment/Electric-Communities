/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 * Rob Kinninmont, June 97
 */
package ec.edoc;

import java.util.Vector;

class CharArray {
    char[] array;
    int length;
}

/** This class is a simple rewrite of the 'sledgehammer to crack a nut' comment
 *  parser which was built using javacc.
 *  it simply plays with a string to extract any commands it knows about, and
 *  puts everything else back into a string.
 */

class CommentParser {

    /** this method takes two strings, presumed to be doc-comments, and
     *  bundles them together into one string.
     *
     *  this currently just does stuff with the '/***' etc at the start
     *  and end of te strings.
     *  XXX it should really be aware of the distinction between the
     *  description and the collection of @commands which follow it.
     *  hence the description at the start of the second comment will
     *  become part of the last @command's description in the first string.
     */
    static String combine(String comment1, String comment2) throws ParseError {

        if (comment1 == null) {
            return comment2;
        }
        if (comment2 == null) {
            return comment1;
        }

        if (!comment1.startsWith("/**") || !comment1.endsWith("*/")) {
            System.err.println("tried to process comment not within /** */");
            throw new ParseError();
        }
        if (!comment2.startsWith("/**") || !comment2.endsWith("*/")) {
            System.err.println("tried to process comment not within /** */");
            throw new ParseError();
        }

        int end1 = comment1.length() - 1;
        int start2 = 0;

        while (comment1.charAt(--end1) == '*') {
        }
        while (comment2.charAt(++start2) == '*') {
        }

        if (end1 == 0) {
            /* then we had a comment with nothing but stars
             * we need to include the starting stars otherwise
             * the generated comment doesn't begin properly */
            end1 = 2;
        }

        //System.out.println("\n[in1]"+comment1);
        //System.out.println("[in2]"+comment2);

        //System.out.println("[out]"+comment1.substring(0, end1 + 1)
        //    +"!!"+ comment2.substring(start2));

        return comment1.substring(0, end1 + 1) + comment2.substring(start2);
    }

    /** This method turns a string containing a doc comment into a
     *  Comment object. This contains the description and any @commands
     *  from the String
     *  @param s, nullFatal; the string to be parsed / processed
     *  @returns nullFatal; a Comment object
     *  @throws ParseError if null is passed in, or the string
     *   doesn't look like a doc comment
     *  @see Comment
     *  @see ParseError
     *  @see AtCommand
     */
    static Comment process(String s) throws ParseError {

        //System.out.println("\nProcessing \"" + s + "\"");

        if (s == null || !s.startsWith("/**") || !s.endsWith("*/")) {
            System.err.println("tried to process comment not within /** */");
            throw new ParseError();
        }

        Comment result = new Comment();

        /* We now create an array without //, but including start & end '*' */
        CharArray tmp = new CharArray();
        tmp.array = new char[s.length() - 2];
        tmp.length = tmp.array.length;
        s.getChars(1, s.length() - 1, tmp.array, 0);

        /* remove any '\r' chars in the input.XXX Possibly shouldn't do this.*/
        for (int i = 0; i < tmp.length; i++) {
            if (tmp.array[i] == '\r') {
                tmp.array[i] = ' ';
            }
        }

        stripStartAndEnd(tmp);
        stripLines(tmp);


        /* What happens here;
         * we loop over the entire string. When(if) we get to the first
         * '@command', then the text prior to that goes into 'text'
         * for each subsequent @command, we generate a string representing
         * that command and create a command object.
         * at the end of the string, we either had just text, in which case
         * we didn't have a command & hence didn't creat the text string,
         * or we had a command. since the command generation triggers each
         * time it encounters a command, it will miss the last one, so we
         * create that.
         */

        /* XXX We might want to put a test in here to mandate that the
         * commands always start at the beginning of the line. (?) */

        /* NB String(char[], int, int) is (start, length) not (start, end) */
        String commentText = null;
        int lastAtCommand = -1;
        for (int i = 0; i < tmp.length; i++) {
            if (tmp.array[i] == '@' && isCommand(tmp, i)) {
                /* if this is our first command - note the end of the text */
                if (lastAtCommand == -1) {
                    lastAtCommand = i;
                    result.description(new String(tmp.array, 0, i));
                } else {
                    //String command = new String(tmp.array,
                    //  lastAtCommand, i - lastAtCommand);
                    try {
                        result.addAtCommand(
                            processCommand(tmp,
                                           lastAtCommand,
                                           i - lastAtCommand));
                    } catch (StringNotUnderstoodException e) {
                        System.err.println("Warning: the annotation '"+
                            e.getMessage()+"' was not understood in "+
                            new String(tmp.array, lastAtCommand,
                                tmp.length - lastAtCommand));
                    }
                    lastAtCommand = i;
                }
            }
        }
        /* check whether we need te create the text, or the last command. */
        if (lastAtCommand == -1) {
            /* NB. not tmp.array.length */
            result.description(new String(tmp.array, 0, tmp.length));
        } else {
            //String command = new String(tmp.array,
            //  lastAtCommand, tmp.length - lastAtCommand);
            try {
                result.addAtCommand(
                    processCommand(tmp,
                                   lastAtCommand,
                                   tmp.length - lastAtCommand));
            } catch (StringNotUnderstoodException e) {
                System.err.println("Warning: the annotation '"+
                    e.getMessage()+"' was not understood in "+
                    new String(tmp.array, lastAtCommand,
                        tmp.length - lastAtCommand));
            }
        }

        return result;

        /* debugging only */

        //System.out.println("tmp.array length " +tmp.array.length +
        //  ", endOfText "+ endOfText + ", tmp.length " + tmp.length);

        /* NB String(char[], int, int) is (start, length) not (start, end) */

        //System.out.println("\ninput =\n" + s + "<end>");

        //System.out.println("\noutput=\n" + commentText + "<end>");

    }

    /** check to see if the referenced part of tmp.array is a valid command. */
    private static boolean isCommand(CharArray tmp, int i) {
        int len = 0;
        while (len + i <= tmp.length && tmp.array[len + i] != ' ' &&
            tmp.array[len + i] != ';' && tmp.array[len + i] != ',') {
            len++;
        }
        String test = new String(tmp.array, i, len);
        return AtDispatcher.isCommand(test);
    }


    /** private helper funciton which removes cruft from the start & end of
     * the comment */
    private static void stripStartAndEnd(CharArray tmp) {

        int lookahead = 0; /* index into the array for our current position */

        /* we know that start & end have '*' */
        while (lookahead < tmp.length && tmp.array[lookahead] == '*') {
            lookahead++;
        }
        remove(tmp, 0, lookahead);

        int lookback = tmp.length - 1;

        while (lookback > 0 && tmp.array[lookback] == '*') {
            lookback--;
        }
        remove(tmp, lookback + 1, tmp.length);
    }


    /** private helper funciton which removes cruft from the start of each
     *  line. */
    /* this works by searching through the tmp.array array. for each start of line,
     * any leading spaces and stars are removed, and we continue processing. */
    private static void stripLines(CharArray tmp) {

        int i = 0; /* index into the array for our current position */

        while (i < tmp.length) {

            if (tmp.array[i] == '\n') {

                int lookahead = i + 1;
                while (lookahead < tmp.length &&
                        (tmp.array[lookahead] == ' ' ||
                        tmp.array[lookahead] == '\t' ||
                        tmp.array[lookahead] == '*' ||
                        tmp.array[lookahead] == '\r')) {
                    lookahead++;
                }
                remove(tmp, i+1, lookahead);
            }

            i++;
        }
    }

    /** private helper function to decide if the given portion of the tmp
     *  array is a valid command or not. */
    private static AtCommand processCommand(CharArray source,
            int start, int length) throws StringNotUnderstoodException {

        String s = new String(source.array, start, length);
        return AtDispatcher.dispatch(s);
    }

    /** Removes a section of characters from the tmp.array array
     *  first inclusive (start of section to be removed)
     *  last exclusive (first one to keep after section)
     *  ( to fit in with java's api's style)
     *
     *  XXX Possibly naughty - it currently does nothing if you try to remove
     *  0 characters (ie. last == first). Perhaps it should throw IOBE*/
    private static void remove(CharArray tmp, int first, int last)
            throws IndexOutOfBoundsException {

        if (first < 0 || first > tmp.length
                || last < first || last > tmp.length) {
            throw new IndexOutOfBoundsException();
        }

        if (last == first) {
            return; /* no op */
        }

        /* copy from just after section, onto the section. */
        System.arraycopy(tmp.array, last, tmp.array, first, tmp.length - last);

        int numRemoved = last - first;
        tmp.length -= numRemoved;

    }

    /** for debugging / testing purposes only */
    public static void main(String[] args) {

        String[] tests = {"/** some textual description\n"+
            " **** @param foo trusted nullOK waffle param desc\n\r"+
            " blip blip * @param bar more; desc desc desc desc*/"
            //"/hello/","/*hello*/","/**hello*/",
            //"//","/**/","/***/","/** */",
            //"/****&$%This class is a simple rewrite of the 'sledgehammer"+
            //"to crack a nut' \n\r *  comment parser which was built *using* "+
            //"javacc.\n\r **** *  it simply plays with a string to ext@ract "+
            //"any commands it knows about, and \n\r *  puts  @param "+
            //"everything else back into a * string.****/"/*,
            //"/** hello @returns blah @param fibble; pling @see@throws /"*/
            };


        for (int i = 0; i < tests.length; i++) {
            System.out.println("\nTest = " + tests[i]);
            try {
                CommentParser.process(tests[i]);
            } catch (ParseError e) {
                //e.printStackTrace();
                System.out.println(e);
            }
        }

        try {
            System.out.println(combine("/***aaa**/", "/**bbbb*/"));
        } catch (ParseError e) {
            //e.printStackTrace();
            System.out.println(e);
        }

    }
}
