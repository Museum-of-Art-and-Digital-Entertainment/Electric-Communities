/*
  ERegistrar.java -- Vat implementation of the E location server

  Chip Morningstar
  based on earlier work by Eric Messick
  2-April-1997

  Copyright 1997 Electric Communities. All rights reserved worldwide.
*/

package ec.e.net;
import ec.e.cap.EEnvironment;
import ec.e.db.RtSwissNumberGenerator;
import java.net.InetAddress;
import java.net.UnknownHostException;

interface ERegistrar {
    ERegistration register(EObject obj) throws ERegistrationException;
    ERegistration register(EObject obj, Date expiration) throws ERegistrationException;
    void lookup(String url, EDistributor resultDist);
    void lookup(ERegistration registration, EDistributor resultDist);
}

class ERegistrarSteward implements ERegistrar {
    private RtSwissNumberGenerator mySwissNumberGenerator;
    private Hashtable myRegisteredObjects;
    private ??? mySearchPath = TODO;
    private String myProcessId;

    ERegistrarSteward(EEnvironment env) {
        myProcessId = generateProcessId(env);
        myRegisteredObjects = new Hashtable();
        mySwissNumberGenerator = new RtSwissNumberGenerator();
    }

    ERegistration register(EObject obj) throws ERegistrationException {
        return register(obj, null);
    }

    ERegistration register(EObject obj, Date expiration) throws ERegistrationException {
        String name = "#" +
            Long.toString(mySwissNumberGenerator.issueNumber(), 36);
        if (myRegisteredObjects.containsKey((Object) name)) {
            throw new ERegistrationException("register: duplicate of " + name);
        } else {
            myRegisteredObjects.put((Object) name, (Object) obj);
            return new ERegistration(this,
                                     new RtEARL(mySearchPath, myProcessId,
                                                name, null),
                                     name);
        }
    }

    void lookup(String url, EDistributor resultDist) {
        lookup(parseEarl(url), resultDist);
    }

    void lookup(ERegistration registration, EDistributor resultDist) {
    }

    private String generateProcessId(EEnvironment env) {
        String processId = env.getProperty("ProcessId");
        if (processId != null && !processId.equals(""))
            return processId;
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) { /* this should never happen */
            hostName = "localhost";
        }
        /* XXX The following technique for generating the process ID seems
           rather bogus to me... */
        return hostName + ":"
            + System.getProperty("user.name") + ":"
            + Long.toString(System.currentTimeMillis());
    }
}

public class ERegistrationException extends Exception {
    public ERegistrationException(String message) {
        super(message);
    }
}
