/** A Stream Key acts as a magic cookie for retrieving bundles of
  persistent objects.  This class is deprecated and not to be used
  except possibly internally - it is superseded by class GenSymKey and
  various kinds of alias objects.
  
  */

package ec.e.db;

import java.io.IOException;
import ec.util.Convert;

public class RtStreamKey {
    
    long  keyValue;
    
    RtStreamKey(long key) {
        keyValue = key;
    }
    
    RtStreamKey(byte asBytes[]) {
        keyValue = Convert.convertToLong(asBytes);
    }
    
    public byte[] toByteArray() {
        return(Convert.convertToBytes(keyValue));
    }
}
