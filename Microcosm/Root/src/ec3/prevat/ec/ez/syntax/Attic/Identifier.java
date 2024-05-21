package ec.ez.syntax;

public class Identifier extends Token {

    private String myToken;

    private Identifier(String token) {
        super(IDENT);
        myToken = token;
    }

    static public Token make(String token) {
        Integer tt = (Integer)TheTokenTable.get(token);
        if (tt != null) {
            return new Token(tt.intValue());
        } else {
            return new Identifier(token);
        }
    }

    public String token() { return myToken; }
}

