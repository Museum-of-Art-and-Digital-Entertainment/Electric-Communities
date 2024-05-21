package ec.pl.runtime;

import ec.e.net.ENetAddr;
import ec.e.net.ERegistrar;
import ec.e.net.ERegistrationException;
import ec.e.net.RtConnection;
import ec.e.start.EEnvironment;
import ec.e.start.ELaunchable;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

public einterface Agent {
    go(EEnvironment env);
}

public eclass Agency implements ELaunchable {
    private ERegistrar myRegistrar;
    private static Agency theAgency = null;
    private boolean amStarted = false;
    static Trace tr = new Trace(false, "Agency");

    public Agency() {
        if (theAgency == null) {
            theAgency = this;
        }
        else {
            // XXX - Squawk!
        }
    }

    emethod go (EEnvironment env) {
        if (amStarted) {
            return;
        }
        amStarted = true;

        String tracingString = env.getProperty("PluribusTracing");
        if (tracingString != null) {
            boolean tracing = (new Boolean(tracingString)).booleanValue();
            if (tracing) {
                BasePresence.tr.traceMode(true);
                Agency.tr.traceMode(true);
            }
        }
        
        try {
            myRegistrar = (ERegistrar)env.magicPower("ec.e.net.ERegistrarMaker");
        } catch (ClassNotFoundException e) {
            System.out.println("Pluribus Agency: Can't get ERegistrarMaker");
            e.printStackTrace();
            System.exit(-100);
        } catch (IllegalAccessException e) {
            System.out.println("Pluribus Agency: Can't get ERegistrarMaker");
            e.printStackTrace();
            System.exit(-101);
        } catch (InstantiationException e) {
            System.out.println("Pluribus Agency: Can't get ERegistrarMaker");
            e.printStackTrace();
            System.exit(-102);
        }
        
        try {
            // XXX - Need to do whatever magic to start Network!
            ////String address = myAddr.toString();
            String address = "123.456.789/hello:5555";
            int slash = address.indexOf('/');
            int colon = address.indexOf(':');
            String addr;
            if (slash >= 0) {
                addr = address.substring(0, slash);
                if (colon >= 0) // it better be!
                    addr += address.substring(colon);
            }
            else {
                addr = address ;
            }
            if (tr.tracing) tr.$("Now listening at address: " + addr);
            String locationFile = (String)env.getProperty("ServerLocationFile");
            if (locationFile != null) {
                RtUtil.writeStringInFile(addr, locationFile);
            }
        } catch (Exception e) {
            System.out.println("Pluribus Agency: Can't start network");
            e.printStackTrace();
            System.exit(-200);
        }
        
        try {
            String agentClassName = env.getProperty("Agent") + "_$_Impl";
            Class agentClass = Class.forName(agentClassName);
            Agent agent = (Agent)agentClass.newInstance();
            agent <- go(env);
        } catch (Exception e) {
            System.out.println("Pluribus Agency: Can't get Agent");
            e.printStackTrace();
            System.exit(-300);
        }
    }
}
