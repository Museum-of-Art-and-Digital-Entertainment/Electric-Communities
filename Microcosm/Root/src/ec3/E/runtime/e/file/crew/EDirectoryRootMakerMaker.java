package ec.e.file.crew;

import java.io.IOException;
import ec.e.start.MagicPowerMaker;
import ec.e.start.EEnvironment;
import ec.e.file.EDirectoryRootMaker;


public class EDirectoryRootMakerMaker implements MagicPowerMaker {

    public Object make(EEnvironment eEnv) {
        return new EDirectoryRootMaker(eEnv.vat());
    }
}

