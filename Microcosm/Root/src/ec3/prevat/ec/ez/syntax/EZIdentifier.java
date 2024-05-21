package ec.ez.syntax;

import ec.ez.runtime.SourceSpan;

/**
 *
 */
public class EZIdentifier extends EZToken {

    private String myToken;

    private EZIdentifier(SourceSpan source, String token) {
        super(source, EZParser.Identifier);
        myToken = token;
    }

    static public EZToken make(SourceSpan source) {
        Integer tt = (Integer)EZParser.TheTokenTable.get(source.text());
        if (tt != null) {
            return new EZToken(source, tt.intValue());
        } else {
            return new EZIdentifier(source, source.text());
        }
    }

    public String token() { return myToken; }
}

