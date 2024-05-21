package ec.ez.syntax;

public class Literal extends Token {

    private String myToken;
    private Object myValue;

    public Literal(String token, Object value) {
        super((value instanceof String)
              ? LITERAL_STRING : OTHER_LITERAL);
        myToken = token;
        myValue = value;
    }

    public String token() { return myToken; }
    public Object value() { return myValue; }
}

