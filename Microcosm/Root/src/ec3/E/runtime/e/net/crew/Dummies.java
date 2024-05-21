package ec.e.net.crew;

import ec.e.start.Tether;
import ec.e.start.FragileRootHolder;

public class ListenThread extends Thread {
    static {
        throw new ExceptionInInitializerError("Loaded dummy ListenThread class");
    }

    public ListenThread(String localAddress, Tether keeperHolder, boolean killerhack) {
        throw new RuntimeException("Dummy class should be overwritten");
    }

    public void startup() {
        throw new RuntimeException("Dummy class should be overwritten");
    }

    public void shutdown() {
        throw new RuntimeException("Dummy class should be overwritten");
    }

    public void suspendListenThread() {
        throw new RuntimeException("Dummy class should be overwritten");
    }
    
    public void resumeListenThread() {
        throw new RuntimeException("Dummy class should be overwritten");
    }
}

public class RawConnection {
    static {
        throw new ExceptionInInitializerError("Loaded dummy RawConnection class");
    }
    public RawConnection(String remoteAddr, FragileRootHolder outerConnection, FragileRootHolder outerReceiver) {
        throw new RuntimeException("Dummy class should be overwritten");
    }
}

public class ConnectionStatisticsCrew {
    static {
        throw new ExceptionInInitializerError("Loaded dummy ConnectionStatisticsCrew class");
    }
    public ConnectionStatisticsCrew(FragileRootHolder root) {
        throw new RuntimeException("Dummy class should be overwritten");
    }
}
