/* Generated Symbol Keys are used when you don't want to invent lots of key names.
*/

package ec.e.rep;

import java.io.IOException;
import ec.util.Convert;

public class GenSymKey {

  long  keyValue;

  GenSymKey(long key) { keyValue = key; }

  public boolean equals(Object x) { 
    if (x instanceof GenSymKey) return ((GenSymKey)x).keyValue == keyValue;
    else return false;
  }

  public String toString() { return "G"+Long.toString(keyValue); }
  public int hashCode() { return (int)keyValue; }
}
