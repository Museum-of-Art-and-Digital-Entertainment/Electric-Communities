package ec.e.net;

import ec.e.net.steward.Proxy;

public interface ProxyDeathHandler {
    void noteProxyDeath(Object proxy, Object data);
}

public class ProxyInterest
{
    private ProxyDeathHandler myHandler;
    private Object myData;
    private Proxy myProxy;
    private Object myProxyPrimeDeflector;
            
    public ProxyInterest(Proxy proxy, ProxyDeathHandler handler, 
            Object data) {
        myHandler = handler;
        myData = data;
        myProxy = proxy;
        myProxyPrimeDeflector = proxy.getPrimeDeflector();
    }
            
    boolean isInterestForProxy(Object proxy)  {
        return (myProxyPrimeDeflector == proxy);
    }
    
    void noteProxyDeath () {
        myHandler.noteProxyDeath(myProxyPrimeDeflector, myData);
    }
}
