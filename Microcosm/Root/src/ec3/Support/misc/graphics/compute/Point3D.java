// Definition of the Point3D class
// Harry Richardson 28/11/96

//
// The Point3D class has two sets of method calls - one is a more 
// functional style (the static methods), which is much more elegant,
// but which may be more expensive in tems of both object creation
// and gc.  Hence the second set of imperative-style methods.
//
// 1/21/97 - moved to gui.utils 
// 2/21/97 - moved to ec.misc.graphics
// 3/22/97 - added a dot product method
// 6/3/97  - added a cross product method
// 12/14/97 - added serialization methods

package ec.misc.graphics;

import java.io.Serializable;

public class Point3D 
        implements Serializable {

    public float x, y ,z;

    public Point3D(float x, float y, float z){
        set(x, y, z);
    }

    public Point3D(Point3D p) {
        set(p);
    }

    public Point3D(float[] c) {
        if (c.length == 3)
            set(c[0], c[1], c[2]);
    }

    public Point3D set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Point3D set(Point3D p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
        return this;
    }

    public static Point3D plus(Point3D p1, Point3D p2) {
        return new Point3D(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
    }

    public Point3D plus(Point3D p) {
        x += p.x; y += p.y; z += p.z;
        return this;
    }
    
    public static Point3D minus(Point3D p1, Point3D p2) {
        return new Point3D(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
    }

    public Point3D minus(Point3D p) {
        x -= p.x; y-= p.y; z -= p.z; 
        return this;
    }

    public static Point3D rotateAboutZ(Point3D p, double phi) {
        float sin = (float) Math.sin(phi);
        float cos = (float) Math.cos(phi);
        return new Point3D(p.x * cos - p.y * sin, p.x * sin + p.y * cos, p.z);
    }


    public Point3D rotateAboutZ(double phi) {
        float sin = (float) Math.sin(phi);
        float cos = (float) Math.cos(phi);
        float tx = x * cos - y * sin;
        float ty = x * sin + y * cos;
        x = tx;
        y = ty;
        return this;
    }

    public static boolean equals(Point3D p1, Point3D p2) {
        return p1.x == p2.x && p1.y == p2.y && p1.z == p2.z;
    }

    public boolean equals(Point3D p) {
        return x == p.x && y == p.y && z == p.z;
    }

    public static Point3D scale(Point3D p, float scale) {
        return new Point3D(p.x * scale, p.y * scale, p.z * scale);
    }

    public Point3D scale(float scale) {
        x *= scale; y *= scale; z *=scale;
        return this;
    }


    /**
     * Returns the dot product treating the points like vectors.
     */
    public float dot(Point3D p) {
        return x*p.x + y*p.y + z*p.z;
    }


    /**
     * Returns the cross product treating the points like vectors.
     */
    public static Point3D cross(Point3D p1, Point3D p2) {
        return new Point3D(p1.x*p2.y - p1.y*p2.x, p1.z*p2.x - p1.x*p2.z, p1.y*p2.z + p1.z*p2.y);
    }


    public static float magnitude(Point3D p) {
        return (float) Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
    }

    public float magnitude() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public float[] getFloatArray() {
        float[] array = {x, y, z};
        return array;
    }

    public String toString () {
        String ret = "Point3D(" + x + "," + y + "," + z + ")";
        return ret;
    }

    public static Point3D average(Point3D[] p) {
        Point3D result = new Point3D(0, 0, 0);

        for (int i = p.length - 1; i >= 0; i--)
            result.plus(p[i]);

        result.scale(1.0f/(float)p.length);
        return result;
    }
}
