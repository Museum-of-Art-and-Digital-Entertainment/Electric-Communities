package ec.examples.lm2000;

import ec.e.comm.*;

public class StartClient {

  public static void main(String args[]) {
    System.out.println("Entering StartClient.main()");
    System.out.println("Instantiating ServerFinder");
    RtLauncher.launch(new ServerFinder(), args);
  }

}
