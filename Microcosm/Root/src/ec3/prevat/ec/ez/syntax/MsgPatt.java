package ec.ez.syntax;

import ec.ez.ezvm.Pattern;


/**
 * For a Parser temporary
 */
/*package*/ class MsgPatt {

    private String  myVerb;
    private Pattern[] myPatterns;

    public MsgPatt(String verb, Pattern[] patterns) {
        myVerb   = verb;
        myPatterns = patterns;
    }

    public String   verb()   { return myVerb; }

    public Pattern[] patterns()  { return myPatterns; }
}

