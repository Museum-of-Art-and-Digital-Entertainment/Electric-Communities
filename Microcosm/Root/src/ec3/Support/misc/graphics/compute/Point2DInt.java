// Point2DInt definition - Harry Richardson 17/1/97

//
// This is a 2D integer equivalent to the Point3D class (without
// quite so many methods).
//
// 1/21/97 - moved to gui.utils 
// 1/31/97 - moved to ui.presenter
// 2/21/97 - moved to ec.misc.graphics
// 12/14/97 - added serialization methods

package ec.misc.graphics;

import java.awt.Point;

import java.io.Serializable;

public class Point2DInt 
        implements Serializable {

    public int x, y;

    public Point2DInt(Point p) {
        set(p.x, p.y);
    }

    public Point2DInt(int x, int y){
        set(x, y);
    }

    public Point2DInt(Point2DInt p) {
        set(p);
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Point2DInt p) {
        this.x = p.x;
        this.y = p.y;
    }

    public static Point2DInt plus(Point2DInt p1, Point2DInt p2) {
        return new Point2DInt(p1.x + p2.x, p1.y + p2.y);
    }

    public void plus(Point2DInt p) {
        x += p.x; y += p.y;
    }
    
    public static Point2DInt minus(Point2DInt p1, Point2DInt p2) {
        return new Point2DInt(p1.x - p2.x, p1.y - p2.y);
    }

    public void minus(Point2DInt p) {
        x -= p.x; y-= p.y;
    }

    public static boolean equals(Point2DInt p1, Point2DInt p2) {
        return p1.x == p2.x && p1.y == p2.y;
    }

    public boolean equals(Point2DInt p) {
        return x == p.x && y == p.y;
    }

    public String toString() {
        return "x: " + x + ", y: " + y;
    }
}
