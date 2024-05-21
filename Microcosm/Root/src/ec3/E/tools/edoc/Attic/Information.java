/*
 * Copyright 1997 Electric Communities. All rights reserved worldwide.
 * 
 * Rob Kinninmont, April 97
 */

package ec.edoc;
 
/** Exception used to signal that we cannot create a suitable Information 
 *  object */
class NoSuchCommandException extends Exception {
    NoSuchCommandException(String s) {
        super(s);
    }
}
class MalformedCommandException extends Exception {
    /** this is purely for testing to work around a bug (#94) in edoc */
    int XXX;
}
 
/** This class is used to store the details of any '@' commands found in
 *  formal comments within source files.
 * 
 *  Each Information subtype should have a constructor taking a single
 *  argument of type ASTInformation. 
 */
abstract class Information {

    /** For things like trust, where we might have positive or negative
     *  information, or a lack therof. */
    static final int UNSPECIFIED = -1;
    static final int TRUE = 1;
    static final int FALSE = 0;

    /* This is used simply to chain them together and avoid using More vectors*/
    Information next;
    
    /** This method is used to create a subtype of Information according to
     *  which command it refers to */
    static Information create(ASTInformation i) throws 
            NoSuchCommandException, MalformedCommandException {
        ASTCommand astc = (ASTCommand)(i.jjtGetChild(0));
        String s = ((Token)(astc.getInfo())).image;
        
        if (s.equals("@param")) {
            return new ParamInformation(i);
        } else if (s.equals("@return")) {
            return new ReturnInformation(i);
        } else if (s.equals("@exception")) {
            return new ExceptionInformation(i);
        } else if (s.equals("@see")) {
            return new SeeInformation(i);
        } else if (s.equals("@version")) {
            return new VersionInformation(i);
        } else if (s.equals("@author")) {
            return new AuthorInformation(i);
        } else if (s.equals("@deprecated")) {
            return new DeprecatedInformation(i);
        }
        
        throw new NoSuchCommandException(s);
    }
}

class ParamInformation extends Information {

    private String myName;
    private int myTrust = UNSPECIFIED;
    private int myNullOK = UNSPECIFIED;

    String name() {
        return myName;
    }
    
    boolean isTrusted() {
        return (myTrust == TRUE);
    }
    boolean isUntrusted() {
        return (myTrust == FALSE);
    }
    
    boolean isNullOK() {
        return (myNullOK == TRUE);
    }
    boolean isNullFatal() {
        return (myNullOK == FALSE);
    }
        
    ParamInformation(ASTInformation i) throws MalformedCommandException {
    
        //ASTCommand astc = (ASTCommand)(i.jjtGetChild(0));
        //String s = ((Token)(astc.getInfo())).image;

        if (i.jjtGetNumChildren() < 2) {
            System.out.println("didn't find any annotations etc");
            i.dump("");
            System.err.println("Error: @param must name the parameter it "+
                "refers to");
            throw new  MalformedCommandException();
        }
        
        //try {
        
            Node n = i.jjtGetChild(1);

            int annotationsStart = 1;
            if (n instanceof ASTWord) {
                myName = ((ASTWord)n).getName();
                annotationsStart = 2;
            } else {
                System.err.println("Warning: @param should name the " +
                    "parameter it refers to.");
            }
        
            /* loop over all (possibly none) annotations */
            int limit = i.jjtGetNumChildren();
            for (int k = annotationsStart; k < limit; k++) {
                n = i.jjtGetChild(k);
                Token t = (Token)(((ASTAnnotation)n).getInfo());
                
                switch (t.kind) {
                case CommentParserConstants.TRUSTED:
                    myTrust = TRUE;
                    break;
                case CommentParserConstants.UNTRUSTED:
                    myTrust = FALSE;
                    break;
                case CommentParserConstants.NULL_OK:
                    myNullOK = TRUE;
                    break;
                case CommentParserConstants.NULL_FATAL:
                    myNullOK = FALSE;
                    break;
                default:
                    /* Unknown command from grammar. */
                    System.err.println("Warning: annotation " + t.image + 
                        "is not understood. Ignoring");
                }
            }
        //} catch (ClassCastException e) {
        //  throw new MalformedCommandException();
        //}
    }
}

class ReturnInformation extends Information {       

    private String myName;
    private int myTrust = UNSPECIFIED;
    private int myNullOK = UNSPECIFIED;

    String name() {
        return myName;
    }
    
    boolean isTrusted() {
        return (myTrust == TRUE);
    }
    boolean isUntrusted() {
        return (myTrust == FALSE);
    }
    
    boolean isNullOK() {
        return (myNullOK == TRUE);
    }
    boolean isNullFatal() {
        return (myNullOK == FALSE);
    }
    
    ReturnInformation(ASTInformation i) throws MalformedCommandException {
        try {
        
            if (i.jjtGetNumChildren() < 2) {
                System.out.println("Warning - return with no info");
                return;
            }
        
            Node n = i.jjtGetChild(1);
            
            int annotationsStart = 1;
            if (n instanceof ASTWord) {
                myName = ((ASTWord)n).getName();
                annotationsStart = 2;
            } else {
                System.err.println("Warning: @return should name the " +
                    "return type it refers to.");
            }
                
            myName = ((ASTWord)n).getName();
            /* loop over all (possibly none) annotations */
            int limit = i.jjtGetNumChildren();
            for (int k = annotationsStart; k < limit; k++) {
                n = i.jjtGetChild(k);
                Token t = (Token)(((ASTAnnotation)n).getInfo());
                
                switch (t.kind) {
                case CommentParserConstants.TRUSTED:
                    myTrust = TRUE;
                    break;
                case CommentParserConstants.UNTRUSTED:
                    myTrust = FALSE;
                    break;
                case CommentParserConstants.NULL_OK:
                    myNullOK = TRUE;
                    break;
                case CommentParserConstants.NULL_FATAL:
                    myNullOK = FALSE;
                    break;
                default:
                    /* Unknown command from grammar. */
                    System.err.println("Warning: annotation " + t.image + 
                        "is not understood. Ignoring");
                }
            }
        } catch (ClassCastException e) {
            throw new MalformedCommandException();
        }

    }
}

/** The "@exception" command should be followed by a fully qualified type name.
 *  at least for the moment, this will go purely into the documentation as 
 *  a link so we won't bother enforcing anything much here...
 */
class ExceptionInformation extends Information {

    private String myName;

    String name() {
        return myName;
    }
    
    ExceptionInformation(ASTInformation i) {
        myName = ((ASTWord)i.jjtGetChild(1)).getName();
    }
}

/** Similarly to the exceptions, "@see" currently just links to whatever you 
 *  put after it, eg "@see java.lang.String#CharAt" or "@see ec.edoc.ASTWord"
 */
class SeeInformation extends Information {

    private String myName;

    String name() {
        return myName;
    }
    
    SeeInformation(ASTInformation i) {
        myName = ((ASTWord)i.jjtGetChild(1)).getName();     
    }
}

/** Author information is currently ignored... */
class AuthorInformation extends Information {

    AuthorInformation(ASTInformation i) {
    }
}

/** Version Information is currently ignored */
class VersionInformation extends Information {

    VersionInformation(ASTInformation i) {
    }
}

/** Deprecation Information is currently ignored */
class DeprecatedInformation extends Information {

    DeprecatedInformation(ASTInformation i) {
    }
}

