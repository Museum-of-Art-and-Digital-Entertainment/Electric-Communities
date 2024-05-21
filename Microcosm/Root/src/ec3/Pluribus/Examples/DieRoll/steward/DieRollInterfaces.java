package ec.pl.examples.dieroll;

public einterface DieRollPeer {
    dieroll();
}

public interface DieRollFramework 
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

