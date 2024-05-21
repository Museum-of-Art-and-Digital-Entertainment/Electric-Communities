
package ec.examples.lm2000;

import ec.e.comm.*;

eclass ServerFinder implements ELaunchable
{
  emethod go(RtEEnvironment env) {

    int listenPortNumber;
    String listenPort = (String)env.getProperty("listenPort");
    if ((listenPort == null) ||
            (listenPort == RtEEnvironment.DefaultPropertyValue)) {
        listenPortNumber = RtEEnvironment.getDefaultPortNumber();
    } else {
        listenPortNumber = (new Integer(listenPort)).intValue();
    }
    env.startNetworkEnvironment(listenPortNumber);

    String hostName = (String)env.getProperty("hostName");
    if (listenPort == null) {
      hostName = "localhost";
    }

    EServer server;
    etry {
      System.out.println("Attempting to look up EServer1 on " + hostName);
      env.getConnector().lookup(hostName, "EServer1", &server);
    } ecatch (RtDirectoryEException e) {
        System.err.println("ServerFinder caught exception: "+e.getMessage());
        System.exit(1);
    }

    System.out.println("Pinging server...");
    server<-ping();

    lm2000 lm2k = new lm2000(server);
  }
}
