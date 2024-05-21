package ec.url;


import java.lang.*;
import ec.e.start.EEnvironment;

public class URLLauncher  {

  static String Browser;

  public static void init(EEnvironment env)  {
    Browser = env.getProperty("browser");
    if (Browser == null) {
      System.out.println("Your web browser is not set.");
      System.out.println("In the *.props file add the line...");
      System.out.println("browser=<full dir path including netscape.exe>");
    } else {
      System.out.println("using browser " + Browser);
    }
  }

  public int OpenURL(String url) { 
    try {

      if (url == null) {
    return 1;
      }
      
      if (Browser == null) {
    System.out.println("dude, your browser= in .props file is not set");
    return 1;
      }

      Runtime.getRuntime().exec(Browser + " " + url);

    } catch (Exception e) {
      System.out.println(e);
    }
    return 0;
  }
}

/****

// Previous version of this class...

package ec.url;

public class URLLauncher  {
    static  { 
        try {
            System.loadLibrary("URLLauncher");
            } 
        catch (UnsatisfiedLinkError e) {
            System.out.println(e);
            System.exit(1);
        }
    }
    public native int OpenURL(String url);
}

***/
