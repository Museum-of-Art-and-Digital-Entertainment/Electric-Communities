package ec.plubar.tests.ColorShapeUnum;

import ec.e.run.RtDecoder;
import ec.e.run.RtEncoder;
import ec.e.db.RtStandardEncoder;
import ec.plubar.Unum;
import ec.plubar.UnumException;

class TestExampleUnum {
    public static void main(String argv[]) {
        Unum unum = null;
        try {
            unum = ExampleUnum.createUnum();
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
            System.out.println(" *** " + exc);
        }
        System.out.println("Changed unum");
        System.out.println(unum);
        System.out.println("");
    }
}

