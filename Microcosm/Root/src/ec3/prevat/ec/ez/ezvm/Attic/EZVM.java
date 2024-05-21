package ec.ez.ezvm;
import ec.ez.runtime.*;

/**
 * ezvm
 *
 * Main test program for EZ.
 */

public class EZVM {
    public static void main(String argv[]) {
        byte kitty[] = new byte[0];
      //  try {
      //      int smurgle = System.in.read(kitty);
      //  }
      //  catch (Exception e) {e.printStackTrace();}

      //  test1();
        System.out.println("Meow");
    }
/**
    public static void test1() {
        LiteralExpr ex1 = new LiteralExpr(null);
        DefineExpr ds1 = new DefineExpr("Cat",ex1);
        LiteralExpr ex2 = new LiteralExpr(new EZString("Meow"));
        LiteralExpr ex3 = new LiteralExpr(new EZDouble(3.0));
        LiteralExpr ex4 = new LiteralExpr(new EZDouble(4.0));
        AssignExpr as1 = new AssignExpr("Cat", ex2);
        Expr arg[] = new Expr[1];
        arg[0] = ex4;
        RequestNode rn = new RequestNode(Verbs.add, arg);
        RequestNode rnp = new RequestNode(Verbs.print, new Expr[0]);
        CallExpr addx = new CallExpr(ex3, rn);
        Pov pv = new Pov(new NameTableEditorImpl(), null);
        try {
        try {
        //    ds1.eval(pv);
        //    as1.eval(pv);
            Expr argy[] = {new LiteralExpr(new EZDouble(8.0))};
            CallExpr callX1 = new CallExpr(
                new CallExpr(new LiteralExpr(new EZDouble(9.0)),
                    new RequestNode(Verbs.add, argy)),
                    rnp);

//            EZObject val = addx.eval(pv);
//            LiteralExpr ex5 = new LiteralExpr(val);
//            CallExpr prx = new CallExpr(ex5, rnp);
            callX1.eval(pv);
//           ((EZDouble) val).print();
        }
        catch (Exception e) {e.printStackTrace();} }
        catch (Ejection e) {e.printStackTrace();}
   }
*/
}
