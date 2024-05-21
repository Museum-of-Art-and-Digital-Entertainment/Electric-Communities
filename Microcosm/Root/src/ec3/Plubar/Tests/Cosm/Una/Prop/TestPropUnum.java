package ec.plubar.tests.cosm.una.prop;

import ec.plubar.Unum;
import ec.plubar.UnumException;

class TestPropUnum {
    public static void main(String argv[]) {
        Unum unum = null;
        try {
            unum = PropUnum.createUnum();
            System.out.println(unum);
            System.out.println("");

            String message = null;
            Object[] args = new Object[0];

            //KSSHack Because we're working with einterfaces, I've got to use
            //KSSHack this stupid $async--This will go away...
            message = "uPropertySheet$async";
            unum.send(message, null);
        } catch (UnumException exc) {
            System.out.println(" *** " + exc);
        }
    }
}

