// A class to let me talk to Alan without using tedious byte arrays...
// Harry Richardson - 20/1/97
// 1/20/97 - moved to gui.utils 
// 2/21/97 - moved to ec.misc.graphics

package ec.misc.graphics;
import java.io.Serializable;

public class RGB 
            implements Serializable {
    public int[] value;

    public RGB(int r, int g, int b) {
        value = new int[3];
        value[0] = r;
        value[1] = g;
        value[2] = b;
    }

    public RGB(RGB copy) {
        this(copy.value[0], copy.value[1], copy.value[2]); 
    }

    public byte[] getByteArray() {
        byte[] result = new byte[3];
        result[0] = (byte) value[0];
        result[1] = (byte) value[1];
        result[2] = (byte) value[2];
        return result;
    }

    public RGB scale(float sf) {
        value[0] = (int) (value[0] * sf);
        value[1] = (int) (value[1] * sf);
        value[2] = (int) (value[2] * sf);
        return this;
    }

    public String toString() {
        return "x: " + value[0] + ", y: " + value[1] + ", z: " + value[2];
    }
}
