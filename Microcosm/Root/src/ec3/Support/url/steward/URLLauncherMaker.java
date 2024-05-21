package ec.url;

import ec.e.run.Vat;
import ec.e.run.EEnvironment;

public class URLLauncherMaker {
    private static boolean IExistAlready = false;
    private Vat myVat;

    static public URLLauncherMaker summon(EEnvironment eEnv)
         throws ClassNotFoundException, 
                IllegalAccessException,
                InstantiationException
    {
        return (URLLauncherMaker) eEnv.magicPower("ec.url.URLLauncherMagicPowerMaker");
    }

    public URLLauncherMaker(Vat vat) {
        if (IExistAlready) {
            throw new SecurityException("URLLauncherMaker already exists");
        }
        IExistAlready = true;
        myVat = vat;
    }


    public URLLauncherSteward makeURLLauncherSteward() {
        return new URLLauncherSteward(myVat);
    }
}


