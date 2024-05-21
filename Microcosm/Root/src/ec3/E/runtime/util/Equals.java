package ec.util;

// Generalized (Array and primitive type) equality comparison functions by KJD (Kari Dubbelman)
// I created these methods to compare results of code regression testing.
// The primitive datatype functions are provided for orthogonality.

public class Equals {

  public final static boolean equals(boolean x, boolean y) {
    return (x == y);
  }

  public final static boolean equals(int x, int y) {
    return (x == y);
  }

  public final static boolean equals(long x, long y) {
    return (x == y);
  }

  public final static boolean equals(char x, char y) {
    return (x == y);
  }

  public final static boolean equals(float x, float y) {
    return (x == y);
  }

  public final static boolean equals(boolean[] x, boolean[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  public final static boolean equals(int[] x, int[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  public final static boolean equals(byte[] x, byte[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  public final static boolean equals(short[] x, short[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  public final static boolean equals(long[] x, long[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  // XXX Warning - floating-point number != comparisons are unreliable
  // I designed this file to be used in product test situations only
  // Use the function below in production code at your own risk. - KJD.

  public final static boolean equals(float[] x, float[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  public final static boolean equals(char[] x, char[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (x[i] != y[i]) return false;
    return true;
  }

  public final static boolean equals(Object[] x, Object[] y) {
    int last = x.length;
    int i;

    if (last != y.length) return false;
    for (i=0; i<last; i++) if (!(x[i].equals(y[i]))) return false;
    return true;
  }

  // possibly this should call equals recursively but I don't know
  // if that would work. Easier to do it iteratively.
  // We cannot do arbitrary dimensionality anyway so why bother.

  public final static boolean equals(Object[][] x, Object[][] y) {
    int last = x.length;
    int i;
    int last2;
    int i2;

    if (last != y.length) return false;
    for (i=0; i<last; i++) {
      last2 = x[i].length;
      if (last2 != y[i].length) return false;
      for (i2 = 0; i2 < last2; i2++) 
	if (! (x[i][i2].equals(y[i][i2]))) return false;
    }
    return true;
  }
}
