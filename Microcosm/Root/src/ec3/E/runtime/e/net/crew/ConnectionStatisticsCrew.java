package ec.e.net.crew;

import ec.e.start.Tether;
import ec.e.start.SmashedException;
import ec.e.start.FragileRootHolder;
import ec.e.net.steward.ConnectionStatistics;


public class ConnectionStatisticsCrew {

    static private FragileRootHolder theConnectionStatistics = null;
    
    public ConnectionStatisticsCrew(FragileRootHolder root) {
        if (null == theConnectionStatistics) {
            theConnectionStatistics = root;
        } else {
            throw new RuntimeException("theConnectionStatistics already initialized");
        }
    }

    static public String statistics() {
        String ret = "";
        if (theConnectionStatistics != null) {
            synchronized (theConnectionStatistics.vatLock()) {
                try {
                    ret = ((ConnectionStatistics)theConnectionStatistics.held()).statistics();
                } catch (SmashedException crunch) {
                    ret = "data not available";
                }
            }
        }
        return ret;
    }

}