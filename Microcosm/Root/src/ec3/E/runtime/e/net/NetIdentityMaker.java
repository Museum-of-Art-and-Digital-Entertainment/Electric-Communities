package ec.e.run;

import java.util.Random;

public class NetIdentityMaker 
{
    public static final long MIN_ID = 32768;
    public static final int POSITIVE_MASK = 0x7FFFFFFF;

    
    // XXX make sure state gets saved for non-deterministic replay.
    
    // XXXXXXXXXXXXXXXXX  MAJOR SECURITY HOLE!!!!!! Need a real random number generator!!!!!
    static private Random myRandom = new Random();
    
    static public long nextIdentity() {
        long id = 0L;

        while (id < MIN_ID) {
            id = myRandom.nextLong();
            if (id < 0) {
                id = id & POSITIVE_MASK;
            } 
        }
        return id;
    }
}
