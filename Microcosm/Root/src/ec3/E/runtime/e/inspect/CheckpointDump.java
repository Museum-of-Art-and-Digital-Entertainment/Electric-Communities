package ec.e.inspect;

import java.io.IOException;

import ec.util.NestedException;
import ec.e.run.ELaunchable;
import ec.e.run.EEnvironment;
import ec.e.run.Vat;
import ec.e.quake.StableStore;
import ec.e.file.EStdio;

import java.util.Hashtable;


public eclass CheckpointDump implements ELaunchable
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
                ("usage: java ec.e.start.EBoot ec.e.inspect.CheckpointDump filename [passphrase]");
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
        Hashtable myhash = new Hashtable();
        Inspector.dumpRoot((Object)vat, "Vat(" + filename + ")", EStdio.out(), myhash);
        EStdio.out().println("number of objects dumped: " + myhash.size());
        env.vat().exit(0);
    }
}
