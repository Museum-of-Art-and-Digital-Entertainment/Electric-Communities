package ec.security;

import ec.e.start.EEnvironment;
import ec.e.start.MagicPowerMaker;

public class ECSecureRandomMaker implements MagicPowerMaker {
    public Object make(EEnvironment env) {
            return new ECSecureRandom(env);
    }
}
