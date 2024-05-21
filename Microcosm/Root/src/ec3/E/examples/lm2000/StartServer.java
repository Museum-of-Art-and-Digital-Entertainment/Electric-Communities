package ec.examples.lm2000;

import ec.e.comm.*;

public class StartServer {

    public static void main(String args[]) {
      System.out.println("Entering StartServer.main()");
      System.out.println("Launching ServerLauncher");
      RtLauncher.launch(new EServer(), args);
      System.out.println("StartServer.main() completed");
    }
}
