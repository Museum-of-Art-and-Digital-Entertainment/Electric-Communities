package ec.ez.syntax;

import ec.ez.runtime.SourceSpan;


/**
 *
 */
public class EZLiteral extends EZToken {

    private String myToken;
    private Object myValue;

    public EZLiteral(SourceSpan source, Object value) {
        super(source, EZParser.Literal);
        myToken = source.text();
        myValue = value;
    }

    public String token() { return myToken; }
    public Object value() { return myValue; }
}

