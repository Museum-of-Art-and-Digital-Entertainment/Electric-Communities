/** A Swiss number is a encrypted identity issued in a sequential
  identity space.
  
  Each one issued is guaranteed to be unique, and as long as the key in not
  compromised, unpredictable. Therefore they can be used to represent
  external capabilities.
  
  */

package ec.e.db;

import java.util.Random;
import ec.e.run.*;
import ec.util.Convert;

public class RtSwissNumberGenerator implements RtCodeable {
    static final Random randomKeySource = new Random();
    
    long identityCounter;
    byte[] scrambleKeyBytes;
    
    public RtSwissNumberGenerator(boolean shouldInitialize) {
        if (shouldInitialize)
            initialize();
    }
    
    public RtSwissNumberGenerator() {
        this(true);
    }
    
    private void initialize () {
        identityCounter = randomKeySource.nextLong();
        if (identityCounter < 0)
            identityCounter = -identityCounter;
        if(scrambleKeyBytes == null)
            scrambleKeyBytes =
                Convert.convertToBytes(randomKeySource.nextLong());
    }
    
    public String classNameToEncode (RtEncoder encoder) {
        return(this.getClass().getName());
    }
    
    public void encode (RtEncoder toStream) {
        try {
            toStream.writeLong(identityCounter);
            toStream.writeInt(scrambleKeyBytes.length);
            toStream.write(scrambleKeyBytes, 0, scrambleKeyBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Object decode (RtDecoder fromStream) {
        try {
            identityCounter = fromStream.readLong();
            int scrambleKeyLen = fromStream.readInt();
            scrambleKeyBytes = new byte[scrambleKeyLen];
            fromStream.readFully(scrambleKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return(this);
    }
    
    /* Issue a Swiss Number, rejecting any that fall within the short integer
       range. */
    
    public long issueNumber() {
        if ((identityCounter >= -32678) && (identityCounter < 32768))
            identityCounter = 32768;
        return(identityCounter++);
    }
}
