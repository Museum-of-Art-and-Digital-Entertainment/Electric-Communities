package ec.samp.crew;

import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;

import ec.samp.steward.SampMaker;

public class SampMagicPowerMaker implements MagicPowerMaker {

    public Object make (EEnvironment env) {
        return (Object) new SampMaker(env.vat());
    }
}
