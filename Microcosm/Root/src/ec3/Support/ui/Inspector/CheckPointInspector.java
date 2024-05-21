package ec.ui;

import java.io.IOException;

import ec.util.NestedException;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.start.Vat;
import ec.e.quake.StableStore;
import ec.e.file.EStdio;
import ec.e.inspect.Inspector;

public eclass CheckPointInspector implements ELaunchable
{
    emethod go(EEnvironment env) {
        String args[] = env.args();

        String filename = null;
        if (args.length < 1) {
            filename = env.getProperty("CheckPoint");
        }
        else {
            filename = args[0];
        }
        
        String passphrase = null;
        if (args.length < 2) {
            passphrase = env.getProperty("Passphrase");
        }
        else {
            passphrase = args[1];
        }
        
        if (filename == null) {
            throw new IllegalArgumentException
                ("usage: java ec.e.start.EBoot ec.ui.CheckPointInspector filename [passphrase]");
        }
        EStdio.out().println("loading from " + filename + " with passphrase <<" + passphrase + ">>");

        StableStore checkpoint = new StableStore(filename, passphrase);
        Vat vat = null;
        try {
            vat = (Vat)checkpoint.restore();
        }
        catch (IOException e) {
            throw new NestedException("error loading checkpoint file " + filename, e);
        }
        
        ec.ui.IFCInspectorUI.start("full");
        Inspector.inspect(vat, "Vat(" + filename + ")");
    }
}
