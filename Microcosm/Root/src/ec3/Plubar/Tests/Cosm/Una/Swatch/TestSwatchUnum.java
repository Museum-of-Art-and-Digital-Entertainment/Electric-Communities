package ec.plubar.tests.cosm.una.swatch;

import ec.cosm.objects.TextureStructure;
import ec.plubar.Unum;
import ec.plubar.UnumException;

class TestSwatchUnum {
    public static void main(String argv[]) {
        Unum unum = null;
        try {
            unum = SwatchUnum.createUnum();
            System.out.println(unum);
            System.out.println("");

            String message = null;
            Object[] args = new Object[2];

            //KSSHack Because we're working with einterfaces, I've got to use
            //KSSHack this stupid $async--This will go away...
            message = "uTexturize$async";
            args[0] = new TextureStructure();
            args[1] = "jacket";
            unum.send(message, args);
        } catch (UnumException exc) {
            System.out.println(" *** " + exc);
        }
    }
}

