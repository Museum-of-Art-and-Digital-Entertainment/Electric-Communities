package ec.e.inspect;

/* Test revival of the same checkpoint file twice into two different vats to time
   the initial class loading that is done for te first revival and not the second

Run using
  java ec.e.start.EBoot ec.e.inspect.ReviveTest filename [passphrase] TraceLog_ec.e.quake.StableStore=debug
You can substitute checkpoint=foo and passphrase=foo for the positional arguments */

import java.io.IOException;

import ec.util.NestedException;
import ec.e.start.ELaunchable;
import ec.e.start.EEnvironment;
import ec.e.start.Vat;
import ec.e.quake.StableStore;
import ec.e.file.EStdio;

public eclass ReviveTest implements ELaunchable
{
    emethod go(EEnvironment env) {
        String args[] = env.args();

        String filename = null;
        if (args.length < 1) {
            filename = env.getProperty("checkpoint");
        }
        else {
            filename = args[0];
        }
        
        String passphrase = null;
        if (args.length < 2) {
            passphrase = env.getProperty("passphrase");
        }
        else {
            passphrase = args[1];
        }
        
        if (filename == null) {
            throw new IllegalArgumentException
                ("usage: java ec.e.start.EBoot ec.e.inspect.ReviveTest filename [passphrase]");
        }
        EStdio.out().println("loading from " + filename + " with passphrase <<" + passphrase + ">>");

        StableStore checkpoint = new StableStore(filename, passphrase);
        Vat vat = null;
        try {
            vat = (Vat)checkpoint.restore();
        }
        catch (IOException e) {
            throw new NestedException("error in first loading checkpoint file " + filename, e);
        }
        EStdio.out().println("loaded once");
        Vat vat2 = null;
        try {
            vat2 = (Vat)checkpoint.restore();
        }
        catch (IOException e) {
            throw new NestedException("error in second loading checkpoint file " + filename, e);
        }
        EStdio.out().println("loaded twice");
        env.vat().exit(0);
    }
}













