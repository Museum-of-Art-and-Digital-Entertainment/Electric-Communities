package ec.samp.steward;

import ec.e.start.Vat;
import ec.e.start.EEnvironment;

public class SampMaker {
    private static boolean IExistAlready = false;
    private Vat myVat;

    public SampMaker(Vat vat) {
        if (IExistAlready) {
            throw new SecurityException("SampMaker already exists");
        }
        IExistAlready = true;
        myVat = vat;
    }


    static public SampMaker summon(EEnvironment eEnv)
         throws ClassNotFoundException, 
                IllegalAccessException,
                InstantiationException
    {
        return (SampMaker) eEnv.magicPower("ec.samp.crew.SampMagicPowerMaker");
    }

    public SampSeismoSteward makeSampSteward() {
        return new SampSeismoSteward(myVat);
    }
}


