package ec.ez.runtime;

import ec.ez.ezvm.LiteralExpr;
import ec.ez.ezvm.Expr;

/**
 * Globals;
 *   some objects used in assorted places.
 *
 */
public class Globals {
    public static final EZNull EZ_NULL = EZNull.theOne();
    public static final EZObject NULL_EZObject_ARRAY [] = new EZObject[0];
    public static final String NULL_String_ARRAY [] = new String[0];
    public static final Expr EZ_TRUE_EXPR  =
        new LiteralExpr(EZBoolean.EZ_TRUE);
    public static final Expr EZ_FALSE_EXPR =
        new LiteralExpr(EZBoolean.EZ_FALSE);
    public static final Class STRING_CLASS = new String("").getClass();
}