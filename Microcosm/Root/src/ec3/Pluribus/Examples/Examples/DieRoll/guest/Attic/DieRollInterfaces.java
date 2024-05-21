package ec.pl.examples.dieroll;

import ec.pl.runtime.UIFramework;

public einterface DieRollPeer {
    dieroll();
}

public interface DieRollFactory extends UIFramework
{
    DieRollController getDieRollController();
}

public interface DieRollController {
    static final int EVENT_DIEROLL_VALUE =      1000;
    static final int EVENT_DIEROLL_REFRESH =    1001;
    static final int EVENT_DIEROLL_SETPEER =    1002;
    
    void postEvent (int eventType, int value, Object data);
    void setPeer (DieRollPeer peer);
}

