package ec.e.run;

public class ZippySecurityManager extends SecurityManager {

    protected boolean assessTrust(Object[] signers)
    {
       //Class clazz = (Class) signers[0];
       //System.out.println("assessTrust called for: " + clazz.toString());
       return true;
    }

    public void checkCreateClassLoader() { } 
    public void checkAccess(Thread g) { }
    public void checkAccess(ThreadGroup g) { }
    public void checkExit(int status) { }
    public void checkExec(String cmd) { }
    public void checkLink(String lib) { }
    public void checkRead(java.io.FileDescriptor fd) { }
    public void checkRead(String file) { }
    public void checkRead(String file, Object context) { }
    public void checkWrite(java.io.FileDescriptor fd) { }
    public void checkWrite(String file) { }
    public void checkDelete(String file) { }
    public void checkConnect(String host, int port) { }
    public void checkConnect(String host, int port, Object context) { }
    public void checkListen(int port) { }
    public void checkAccept(String host, int port) { }
    public void checkMulticast(java.net.InetAddress maddr) { }
    public void checkMulticast(java.net.InetAddress maddr, byte ttl) { }
    public void checkPropertiesAccess() { }
    public void checkPropertyAccess(String key) { }
    public void checkPropertyAccess(String key, String def) { }
    public boolean checkTopLevelWindow(Object window) { return true; }
    public void checkPrintJobAccess() { }
    public void checkSystemClipboardAccess() { }
    public void checkAwtEventQueueAccess() { }
    public void checkPackageAccess(String pkg) { }
    public void checkPackageDefinition(String pkg) { }
    public void checkSetFactory() { }
    public void checkMemberAccess(Class clazz, int which) { }
    public void checkSecurityAccess(String provider) { }

}
