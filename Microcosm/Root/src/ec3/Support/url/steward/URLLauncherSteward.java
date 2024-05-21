package ec.url.steward;

import ec.url.crew.URLLauncher;
import java.io.Exception;
import ec.e.start.*;

public class URLLauncherSteward extends Tether {

    URLLauncherSteward(Vat vat) {
        // Note:  URLLauncher is considered sturdy, but I'm treating
        // it as fragile.

        super(vat, (Object)(new URLLauncher()));
    }

    protected Object reconstructed() {
        // Vat.println("reconstructing the URLLauncher");
        return new URLLauncher();
    }

    public void openURL(String url) {

        try {
            URLLauncher heldURLLauncher = (URLLauncher)this.held();
            heldURLLauncher.openURL(url);
        } catch (SmashedException e) {
            Vat.println("URLLauncherSteward: got exception from openURL " +
                                  e.getMessage());
        }
    }

}





