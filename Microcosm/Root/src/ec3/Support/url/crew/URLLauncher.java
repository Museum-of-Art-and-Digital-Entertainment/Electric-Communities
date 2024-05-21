package ec.url.crew;

import ec.util.Native;

public class URLLauncher  {

    public URLLauncher() {}

    public int openURL(String url) {
      return Native.openURL(url);
    }

    /* ** Test stub **
     * public int nativeOpenURL(String url) {      
     *   System.out.println("Hey!  my url is " + url);
     *   return 0;
     * }
     */

}

