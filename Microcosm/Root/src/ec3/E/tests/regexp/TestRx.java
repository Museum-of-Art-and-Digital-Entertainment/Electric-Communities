package ec.tests.regexp;

import ec.regexp.RegularExpression;

public class TestRx 
{
    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("usage: java ec.tests.regexp.TestRx <regular_expression> <string_to_match>");
            System.exit(1);
        }
        System.out.println("/" + args[0] + "/" + args[1] + "/");
        RegularExpression rx = new RegularExpression(args[0]);
        if (rx.Match(args[1])) {
            System.out.println("matched.");
            int nsub = rx.SubExpressions();
            for (int i=0; i<nsub; i++) {
                System.out.println("&" + i + rx.SubMatch(i));
            }
        }
        else {
            System.out.println("did not match.");
        }
    }
}
