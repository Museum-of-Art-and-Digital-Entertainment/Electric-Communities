
package ec.url;

public class URLLauncherDemo  {
    public static void main (String argv[])  {
        URLLauncher ul = new URLLauncher();
        System.out.println ("Launching URL " + argv[0]);
        int result = ul.OpenURL(argv[0]);
        System.out.println ("Result is " + result);
    }
}
