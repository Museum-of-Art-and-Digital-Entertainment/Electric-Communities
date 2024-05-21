package ec.pl.examples.lamp;
import ec.pl.runtime.Unum;

public einterface LampOwner   {
    noteNewLampUnum (Unum lamp);
    getLamp (EResult dist);
}

public einterface LampPeer {
    lampToggle();
    lampInvalidate();
}

public interface LampFramework {
    LampController getLampController();
}

public interface LampController {
    static final int EVENT_LAMP_STATE =     1000;
    static final int EVENT_LAMP_STATUS =    1001;

    void postEvent (int eventType, boolean state);
    void setPeer (LampPeer peer, boolean hostState, boolean lampState);
}
