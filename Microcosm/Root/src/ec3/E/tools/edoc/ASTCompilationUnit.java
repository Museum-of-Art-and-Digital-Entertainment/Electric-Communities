/**
 *  Copyright 1997 Electric Communities. All rights reserved worldwide.
 *
 *  Rob Kinninmont, April 97
 *
 *  This file defines a class used to represent the whole of an input file 
 *  for an E file. It includes methods which collect all the tokens of the 
 *  file so that comments etc can be extracted later.
 */

package ec.edoc;
/* JJT: 0.2.2 */

import java.io.PrintStream;
import java.util.Vector;

public class ASTCompilationUnit extends SimpleNode 
        implements EDocParserConstants {

    ASTCompilationUnit(String id) {
        super(id);
    }

    public static Node jjtCreate(String id) {
        return new ASTCompilationUnit(id);
    }

    // Manually inserted code begins here


    protected Token begin, end;
  
    public void setFirstToken(Token t) { 
        begin = t; 
    }
  
    public void setLastToken(Token t) { 
        end = t; 
    }

    // Collect all tokens into an ordered vector for later use.

    protected Vector tokenVector = new Vector(1000);

    public Vector buildTokenVector() {
        Token t = begin;
        while (t != null) {
            Token tt = t.specialToken;
            if (tt != null) {
                while (tt.specialToken != null) {
                    tt = tt.specialToken;
                }
                while (tt != null) {
                    tokenVector.addElement(tt);
                    tt = tt.next;
                }
            }
            tokenVector.addElement(t);
            t = t.next;
        }
        tokenVector.trimToSize();
        return tokenVector;
    }
  
    protected void printDocComments(PrintStream ostr) {
  
        Token t;
        /*DEADCP
        CommentParser cp = new CommentParser((java.io.InputStream)null);  
        */
        for (int i = 0; i < tokenVector.size(); i++) {
            t = (Token)tokenVector.elementAt(i);
            if (t != null) {
                if (t.kind == FORMAL_COMMENT) {
                    /*DEADCP
                    try {
                        cp.process(t.image);
                    } catch (ParseError e) {
                        System.err.println(" Oh dear we seem to have a "+
                            "malformed DocComment;\n" + t.image + "\n");
                    }
                    */
                }
            }
        }
    }

    private String addUnicodeEscapes(String str) {
        String retval = "";
        char ch;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if ((ch < 0x20 || ch > 0x7e) && ch != '\t' && ch != '\n' 
                    && ch != '\r' && ch != '\f') {
                String s = "0000" + Integer.toString(ch, 16);
                retval += "\\u" + s.substring(s.length() - 4, s.length());
            } else {
                retval += ch;
            }
        }
        return retval;
    }

}
