package ec.ez.syntax;

import ec.ez.runtime.SourceSpan;
import ec.ez.ezvm.Expr;
import ec.ez.ezvm.LiteralExpr;
import ec.ez.ezvm.NounExpr;
import ec.ez.ezvm.Pattern;
import java.util.Vector;


/**
 *
 */
public class EZQuasiString extends EZToken {

    private String myToken;
    private String myValue;
    private LiteralExpr myTemplate;
    private Vector myExprs;
    private Vector myPatterns;

    public EZQuasiString(SourceSpan source, String value) {
        super(source, EZParser.QuasiString);
        myToken = source.text();
        myValue = value;

        StringBuffer template = new StringBuffer();
        myExprs = new Vector();
        myPatterns = new Vector();

        for (int i = 0; i < value.length(); ) {
            char c1 = value.charAt(i);
            if (c1 == '$' || c1 == '@') {
                i++;
                char c2 = value.charAt(i);
                if (c1 == c2) {
                    template.append(c2);
                    i++;

                } else if (c2 == '{') {
                    throw new Error("XXX ${...} not yet implemented");

                } else if (Character.isJavaIdentifierStart(c2)) {
                    int j;
                    for (j = i; 
                         j < value.length()
                         && Character.isJavaIdentifierPart(value.charAt(j));
                         j++) {}
                    String ident = value.substring(i, j);
                    if (c1 == '$') {
                        template.append("$" + myExprs.size() + " ");
                        myExprs.addElement(new NounExpr(ident));
                    } else if (c1 == '@') {
                        template.append("@" + myPatterns.size() + " ");
                        myPatterns.addElement(Pattern.name(ident));
                    } else {
                        throw new Error("internal: bad case");
                    }
                    i = j;
                } else {
                    throw new Error("unrecognized: " + c1 + c2);
                }
            } else {
                template.append(c1);
                i++;
            }
        }
        String str = template.toString();
        //XXX must create printRep properly!
        myTemplate = new LiteralExpr("\"" + str + "\"", str);
    }

    public String      token()    { return myToken; }
    public String      value()    { return myValue; }
    public LiteralExpr template() { return myTemplate; }
    public Vector      exprs()    { return myExprs; }
    public Vector      patterns() { return myPatterns; }
}

