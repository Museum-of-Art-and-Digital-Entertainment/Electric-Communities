package ec.pl.runtime.tests;

import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.e.db.RtStandardEncoder;
import ec.pl.runtime.Unum;
import ec.pl.runtime.UnumException;

class TestExampleUnum {
    public static void main(String argv[]) {
        try {
            Unum unum = ExampleUnum.createUnum();
            System.out.println(unum);
            System.out.println("");

            String message = null;
            String[] args = new String[1];

            message = "uSetColor";
            args[0] = "blue";
            unum.send(message, args);
            message = "uSetShape";
            args[0] = "square";
            unum.send(message, args);

            message = "uiSetColor";
            unum.send(message, null);
        } catch (UnumException exc) {
            System.out.println(exc);
        }
    }
}

