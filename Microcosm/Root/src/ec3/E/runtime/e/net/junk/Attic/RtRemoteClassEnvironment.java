package ec.e.net;

import java.util.Hashtable;
import ec.eload.RtClassEnvironment;

class RtRemoteClassEnvironment implements RtClassEnvironment {
    private Hashtable myPaths = new Hashtable();
    private Hashtable myCache = new Hashtable();

    public void put(String path) {
        myPaths.put(path, path);
    }

    public void remove(String path) {
        myPaths.remove(path);
        myCache.clear();
    }

    public boolean contains(String path) {
        if (myCache.contains(path)) {
            //System.out.println("Have class " + path + " in cache");
            return(true);
        }
        if (myPaths.contains(path)) {
            //System.out.println("Have class " + path + " in paths");
            return(true);
        }
        String pack;
        int index = 0;
        while ((index = path.indexOf('.', index)) > 0) {
            pack = path.substring(0, index++);
            if (myPaths.contains(pack)) {
                myCache.put(path, path);
                //System.out.println("Matched class " + path);
                return(true);
            }
        }
        //System.out.println("Don't have class " + path);
        return(false);
    }
}
