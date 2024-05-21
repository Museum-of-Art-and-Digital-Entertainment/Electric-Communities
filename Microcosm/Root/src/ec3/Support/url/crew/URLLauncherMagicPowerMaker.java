package ec.url.crew;

import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.url.steward.URLLauncherMaker;

public class URLLauncherMagicPowerMaker implements MagicPowerMaker {

    public Object make (EEnvironment env) {
        return (Object) new URLLauncherMaker(env.vat());
    }
}
