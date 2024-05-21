/*
    j_space.java
    Utilities for spatial detection.
    (Actually, at the moment, just 2D polygon-based utilities.)
    Rob Jellinghaus
    (c) 1996 Electric Communities
*/

package ec.misc.graphics;

import java.util.*;

import java.io.Serializable;

import ec.e.file.EStdio;
import ec.misc.graphics.*;

import ec.e.run.Trace;


// class defining a Point2DFloat
// real rudimentary
// INSECURE CLASS: x and y are public
public class Point2DFloat {
    protected float x, y;

    public Point2DFloat (float ax, float ay) {
        x = ax; y = ay;
    }
    
    public float x() {
        return x;
    }
    public float y() {
        return y;
    }

    public String toString () {
        return "2DPoint["+x+","+y+"]";
    }

    public float dot (Point2DFloat other) {
        return x * other.x() + y * other.y();
    }

    public float dot (float ax, float ay) {
        return x * ax + y * ay;
    }

    public Point2DFloat times (float scalar) {
        return new Point2DFloat(x * scalar, y * scalar);
    }

    public Point2DFloat plus (Point2DFloat other) {
        return new Point2DFloat(x + other.x(), y + other.y());
    }

    public Point2DFloat minus (Point2DFloat other) {
        return new Point2DFloat(x - other.x(), y - other.y());
    }

    public Point2DFloat plus (float ax, float ay) {
        return new Point2DFloat(x + ax, y + ay);
    }

    public Point2DFloat minus (float ax, float ay) {
        return new Point2DFloat(x - ax, y - ay);
    }

    public Point2DFloat dividedBy (float scalar) {
        return new Point2DFloat(x / scalar, y / scalar);
    }

    public boolean equals (Point2DFloat other) {
        return (x == other.x() && y == other.y());
    }

    // treat this point as a vector starting at the origin
    // normalize its length
    public Point2DFloat normalize () {
        float length = this.length();
        // do nothing if this point is as good as zero
        if (length < 1e-6)
            return this;
        return new Point2DFloat(x / length, y / length);
    }

    // get the length of this point considered as a vector
    public float length () {
        return (float)Math.sqrt(this.dot(this));
    }
}


// thrown when a line is created with zero (or epsilon, actually) length
// also throw when a zero point is normalized
public class ZeroLengthException extends Exception {
}

// thrown when an intersection point is requested for parallel lines
public class ParallelException extends Exception {
}

// thrown when an expanded polygon is requested for a polygon which has not
// had its final line added
public class NotClosedPolygonException extends Exception {
}

// class defining a 2-D line
// protocol for creating it, offsetting it, calculating with respect to it
// SECURE CLASS: no way to side-effect it, all protocol functional
public class Line2DFloat {
    /*
     * state
     */

    // explicit representation; see Graphics Gems I, p. 3
    private Point2DFloat u, v;
    // implicit representation
    private Point2DFloat n;
    // normalized version
    private Point2DFloat normalized_n;
    // version which we use in calculations
    // we're not yet sure whether original n or normalized n is preferable
    // so this lets us hide that
    private Point2DFloat use_n;
    private float c;

    // actual endpoints
    private float x1, y1, x2, y2;
    
    static private Trace theTrace = new Trace("line2dfloat");


    /*
     * constructors
     */

    private void setup (float ax1, float ay1, float ax2, float ay2)
        throws ZeroLengthException {
        x1 = ax1; y1 = ay1; x2 = ax2; y2 = ay2;

        // construct explicit and implicit representations
        // see Graphics Gems I, p. 5
        u = new Point2DFloat(ax1, ay1);
        v = new Point2DFloat(ax2 - ax1, ay2 - ay1);

        // ALSO note that Graphics Gems I is WRONG!  It has "-v.x(), v.y()" where it should be "-v.y(), v.x()".
        // this is as per the errata
        n = new Point2DFloat(-v.y(), v.x());
        float length_n = (float)Math.sqrt(n.dot(n));

        if (length_n < 1e-6)
            throw new ZeroLengthException();

        normalized_n = n.dividedBy(length_n);

        // HERE IS WHERE WE SELECT WHICH N TO USE
        use_n = normalized_n;

        c = use_n.dot(u);
    }

    public Line2DFloat (float ax1, float ay1, float ax2, float ay2)
        throws ZeroLengthException {
        setup(ax1, ay1, ax2, ay2);
    }
    public Line2DFloat (Line2DFloat line) {
        x1 = line.x1; y1 = line.y1; x2 = line.x2; y2 = line.y2;
        u = line.u;
        v = line.v;
        n = line.n;
        normalized_n = line.normalized_n;
        use_n = line.use_n;
        c = line.c;
    }
    public Line2DFloat (Point2DFloat start, Point2DFloat end)
        throws ZeroLengthException {
        setup(start.x(), start.y(), end.x(), end.y());
    }

    /*
     * accessors
     */

    public Point2DFloat firstEndpoint () {
        return new Point2DFloat(x1, y1);
    }
    public Point2DFloat secondEndpoint () {
        return new Point2DFloat(x2, y2);
    }
    public float getX1 () { return x1; }
    public float getY1 () { return y1; }
    public float getX2 () { return x2; }
    public float getY2 () { return y2; }
    public String toString () {
        return "2DLine["+x1+","+y1+" "+x2+","+y2+" n:"+n+" n_n:"+normalized_n+" c:"+c+"]";
    }
    public Point2DFloat normalizedNormal () { return normalized_n; }

    /*
     * derived constructors
     */

    public Line2DFloat offsetBy (float xDelta, float yDelta)
        throws ZeroLengthException {
        return new Line2DFloat (x1 - xDelta, y1 - yDelta, x2 - xDelta, y2 - yDelta);
    }

    // return a new point that is offset from the given point by
    // a multipler of the line's normal.
    public Point2DFloat offNormal (Point2DFloat startingPoint,
        float distance) {
        return startingPoint.plus(normalized_n.x() * distance, normalized_n.y() * distance);
    }

    /*
     * queries
     */

    // calculate the implicit function of this line with respect to x and y
    // do so efficiently, without allocation
    public float implicitFunction (float x, float y) {
        // calculate a function of this line for the given point
        // this function = 0 when (x, y) is on the line
        // see Graphics Gems II, p. 7
        // also note that this function is just the implicit representation--
        // i.e. see Graphics Gems I, p. 3
        // ALSO note that Graphics Gems I is WRONG!  It has "+ c" where it should be "- c".
        // this is as per the errata
        if (theTrace.debug && Trace.ON) theTrace.debugm("      Implicit function for ("+x+" "+y+"): dot is "+normalized_n.dot(x,y)+", dot-c is "+(normalized_n.dot(x,y)-c)+".");
        return use_n.dot(x, y) - c;
    }

    // calculate whether this line intersects other
    // do so efficiently, without allocation
    public boolean intersects (Line2DFloat other) {
        if (theTrace.debug && Trace.ON) theTrace.debugm("      Line::intersects: comparing "+this+" with "+other+".");
        // see Graphics Gems II, p. 7
        float r3 = this.implicitFunction(other.x1, other.y1);
        float r3abs = Math.abs(r3);
        float r3sign = r3 / r3abs;
        float r4 = this.implicitFunction(other.x2, other.y2);
        float r4abs = Math.abs(r4);
        float r4sign = r4 / r4abs;
        if (theTrace.debug && Trace.ON) theTrace.debugm("        r3 is "+r3+" sign "+r3sign+", r4 is "+r4+" sign "+r4sign);

        if (r3abs > 1e-6 && r4abs > 1e-6 && 
                ((r3sign < 0 && r4sign < 0) || (r3sign > 0 && r4sign > 0)))
            return false;

        float r1 = other.implicitFunction(x1, y1);
        float r1abs = Math.abs(r1);
        float r1sign = r1 / r1abs;
        float r2 = other.implicitFunction(x2, y2);
        float r2abs = Math.abs(r2);
        float r2sign = r2 / r2abs;
        if (theTrace.debug && Trace.ON) theTrace.debugm("        r1 is "+r1+" sign "+r1sign+", r2 is "+r2+" sign "+r2sign);
        
        if (r1abs > 1e-6 && r2abs > 1e-6 &&
                ((r1sign < 0 && r2sign < 0) || (r1sign > 0 && r2sign > 0)))
            return false;

        // note that collinearity check not implemented yet!!!!!!!!!

        if (theTrace.debug && Trace.ON) theTrace.debugm("        Lines intersect!");

        return true;
    }

    // calculate point of intersection of this line and other
    // do so efficiently, without unnecessary allocation
    // (only return value is allocated)
    public Point2DFloat intersection (Line2DFloat other) throws ParallelException {
        // this will return a result for the generalized line,
        // not for the line segment
        // so make sure they actually intersect first!

        // see Graphics Gems I, p. 11; note erratum, it should be "- c" (as here)
        // not "+ c" (as in the book)
        float d = use_n.dot(other.v);
//      if (theTrace.debug && Trace.ON) theTrace.debugm("        d is "+d);
        if (Math.abs(d) < 1e-6)
            throw new ParallelException();


        float top = use_n.dot(other.u) - c;
        float multiplierTerm = top / d;
        if (theTrace.debug && Trace.ON) theTrace.debugm("        d is "+d+", top is "+top+", multiplierTerm is "+multiplierTerm);
        if (theTrace.debug && Trace.ON) theTrace.debugm("        other.u is "+other.u+", other.v is "+other.v+", other.v*mult is "+
            other.v.times(multiplierTerm)+", result is "+other.u.minus(other.v.times(multiplierTerm)));
        Point2DFloat result = other.u.minus(other.v.x() * multiplierTerm, other.v.y() * multiplierTerm);
        if (theTrace.debug && Trace.ON) theTrace.debugm("      In Line::intersection, intersecting "+this+" with "+other+", result "+result);
        return result;
    }
}

    

// this stupid class just lets us package together a point of intersection
// and the index of a line which is intersected with at that point
public class PolyIntersection2DFloat {
    public Point2DFloat point;
    public int lineIndex;
    public Polygon2DFloat polygon;
    public PolyIntersection2DFloat (Point2DFloat pointarg, int linearg, Polygon2DFloat poly) {
        point = pointarg;
        lineIndex = linearg;
        polygon = poly;
    }
    public String toString () {
        return "PolyIntersection["+polygon+" line "+lineIndex+" at "+point+"]";
    }
}

// class defining a 2-D polygon
// coordinates are assumed relative to some control point
// this is also used for path definition
// INSECURE CLASS: add operation side-effects it
public class Polygon2DFloat 
implements Serializable {
    /*
     * state
     */

    // vector of Line2DFloats
    private Vector lines;
    // bounding box of lines
    private float xmin, ymin, xmax, ymax;
    // last point added
    private float xlast, ylast;

    // vector of Line2DFloats
    // this vector matches up with the lines vector
    // it is used to store the precomputed bounding lines that are
    // used for path planning
    // this vector gets created in addFinalLine
    private Vector boundingLines;
    // distance of bounding lines
    private float boundingDist;
    
    static private Trace theTrace = new Trace("polygon2dfloat");

    /*
     * constructors
     */

    // construct a polygon with just one point
    public Polygon2DFloat (float x, float y) {
        lines = new Vector();
        boundingLines = null;
        xmin = xmax = xlast = x;
        ymin = ymax = ylast = y;
    }

    public Polygon2DFloat (Point2DFloat point) {
        lines = new Vector();
        boundingLines = null;
        xmin = xmax = xlast = point.x();
        ymin = ymax = ylast = point.y();
    }

    // make a new Polygon2DFloat from this one, with offset if desired
    // handle degenerate cases
    public Polygon2DFloat (Polygon2DFloat other, float xoffset, float yoffset) {
        if (theTrace.debug && Trace.ON) theTrace.debugm("Constructing derivative poly from "+other+" at ("+xoffset+","+yoffset+")");
        lines = new Vector();
        if (other.size() == 0) {
            // empty lines, just copy other state
            xmin = xmax = xlast = other.xlast + xoffset;
            ymin = ymax = ylast = other.ylast + yoffset;
        } else for (int i = 0; i < other.size(); i++) {
            // get current line
            Line2DFloat line = (Line2DFloat)other.lineAt(i);
            if (theTrace.debug && Trace.ON) theTrace.debugm("  Line is "+line+".");
            if (i == 0) {
                xmin = xmax = xlast = line.firstEndpoint().x() + xoffset;
                ymin = ymax = ylast = line.firstEndpoint().y() + yoffset;
                if (theTrace.debug && Trace.ON) theTrace.debugm("  Setting xlast to "+xlast+" and ylast to "+ylast+".");
            }
            
            if (i == other.size() - 1 && other.boundingLines != null) {
                // if other is a closed polygon, we can assume that the second endpoint of
                // this last line is the same as the first endpoint of the first line, and
                // therefore that addFinalLine here will do the right thing
                try {
                    addFinalLine(other.boundingDist);
                } catch (ZeroLengthException e) {
                    // there can be no zero length exception here, since if there were, the other
                    // polygon itself wouldn't be well-formed.
                    // so, we can plausibly ignore, by construction, any such exception.
                }
            } else {
                // in all other cases, just add the second endpoint
                try {
                    addLine(line.secondEndpoint().x() + xoffset, line.secondEndpoint().y() + yoffset);
                } catch (ZeroLengthException e) {
                    // see above reasoning
                }
            }
        }
    }

    /*
     * accessors
     */

    // number of lines
    public int size () {
        return lines.size();
    }

    // index for a given line
    public int indexOf (Line2DFloat line) {
        return lines.indexOf(line);
    }

    // return a line at an index
    public Line2DFloat lineAt (int index) {
        return (Line2DFloat)lines.elementAt(index);
    }

    // return a bounding line at an index
    public Line2DFloat boundingLineAt (int index)
        throws NotClosedPolygonException {
        if (boundingLines == null)
            throw new NotClosedPolygonException();

        return (Line2DFloat)boundingLines.elementAt(index);
    }

    // get the first point
    public Point2DFloat startPoint () {
        if (lines.size() == 0)
            return new Point2DFloat(xlast, ylast);
        else 
            return ((Line2DFloat)lines.elementAt(0)).firstEndpoint();
    }

    // get the last point
    public Point2DFloat endPoint () {
        return new Point2DFloat(xlast, ylast);
    }
    
    // get the total length
    public float totalLength () {
        float ret = 0.0f;
        for (int i = 0; i < lines.size(); i++) {
            Line2DFloat line = (Line2DFloat)lines.elementAt(i);
            Point2DFloat lineVec = line.firstEndpoint().minus(line.secondEndpoint());
            ret = ret + (float)Math.sqrt(lineVec.dot(lineVec));
        }
        return ret;
    }

    public String toString () {
        return "Polygon2DFloat["+this.startPoint()+" with "+lines.size()+" lines, boundingLines is "+boundingLines+"]";
    }
    
    // return a vector of Point3Ds created by adding each point in this poly + offset
    public Vector asGlobalPoints (Point3D offset) {
        Vector ret = new Vector();
        Point2DFloat start = this.startPoint();
        Point3D p = new Point3D(start.x(), start.y(), 0);
        p.plus(offset);
        ret.addElement(p);
        for (int i = 0; i < lines.size(); i++) {
            Point2DFloat end = ((Line2DFloat)lines.elementAt(i)).secondEndpoint();
            p = new Point3D(end.x(), end.y(), 0);
            p.plus(offset);
            ret.addElement(p);
        }
        return ret;
    }

    /*
     * mutators
     */

    // add a line by providing the endpoint of the line
    // points for closed polys are in clockwise order
    public void addLine (float x, float y)
        throws ZeroLengthException {
        if (x < xmin) xmin = x;
        if (x > xmax) xmax = x;
        if (y < ymin) ymin = y;
        if (y > ymax) ymax = y;
//      if (theTrace.debug && Trace.ON) theTrace.debugm("Adding line ending at "+x+","+y+".");
        lines.addElement(new Line2DFloat(xlast, ylast, x, y));
        if (theTrace.debug && Trace.ON) theTrace.debugm("  Lines size is now "+lines.size());
        xlast = x;
        ylast = y;
    }

    public void addLine (Point2DFloat point)
        throws ZeroLengthException {
        addLine(point.x(), point.y());
    }

    // complete the polygon
    // equivalent to adding a line which connects the last point to the start point
    // also precalculates the bounding lines used for avoidance
    // therefore takes a parameter of how far out the avoidance lines should go
    //
    // The algorithm here is designed to allow for bounding lines that may be
    // coincident (in the common case) or not (if the bounding lines are at too steep
    // an angle, we essentially create a mitered join).  Calculating a bounding line
    // for a given line segment in the lines array necessarily involves calculating the
    // bounding line for the adjoining line segments, in order to figure out the join.
    // Therefore we carry information forward from line segment to line segment to
    // avoid recalculating information needlessly.
    public void addFinalLine (float dist)
        throws ZeroLengthException {
        if (theTrace.debug && Trace.ON) theTrace.debugm("In addFinalLine...");
        Line2DFloat firstLine = (Line2DFloat)lines.elementAt(0);
        addLine(firstLine.firstEndpoint().x(), firstLine.firstEndpoint().y());

        // now calculate bounding lines
        boundingLines = new Vector();
        boundingDist = dist;

        // this is the point which we calculated as part of the last bounding line
        // that would be the start of this one
        Point2DFloat nextBoundStart = null;

        // walk through lines
        for (int i = 0; i < lines.size(); i++) {
            // calculate offNormal for this line
            Line2DFloat thisLine = (Line2DFloat)lines.elementAt(i);

            // first calculate start join, if needed
            if (nextBoundStart == null) {
                // we haven't calculated the previous end, therefore we are just starting
                Line2DFloat prevLine = (Line2DFloat)lines.elementAt(lines.size() - 1);
                // calculate the join point(s)
                Vector prevJoins = calcJoinPoints(prevLine, thisLine, dist, dist);
                // use the last join point as the beginning point here
                nextBoundStart = (Point2DFloat)prevJoins.elementAt(prevJoins.size() - 1);
            }

        // calculate the join for this line with the next line
            Line2DFloat nextLine = null;
            if (i == lines.size() - 1)
                nextLine = (Line2DFloat)lines.elementAt(0);
            else
                nextLine = (Line2DFloat)lines.elementAt(i+1);

            // calculate the join vector
            Vector joins = calcJoinPoints(thisLine, nextLine, dist, dist);

            // add a new bounding line which starts at the last-calculated starting spot
            // and ends at the first entry in the joins vector
            boundingLines.addElement(new Line2DFloat(nextBoundStart, 
                (Point2DFloat)joins.elementAt(0)));

            if (theTrace.debug && Trace.ON) theTrace.debugm("  Adding bounding line "+nextBoundStart+" to "+(Point2DFloat)joins.elementAt(0));

            // set up nextBoundStart with the last entry in the joins vector
            nextBoundStart = (Point2DFloat)joins.elementAt(joins.size() - 1);
        }
        if (theTrace.debug && Trace.ON) theTrace.debugm("  DONE adding bounding lines.");
    }

    // Return a vector of join points.
    // In the common case there will be just one.
    // If there are two, then the first is the endpoint of the first line, and
    // the second is the startpoint of the second line.
    // cutoff is the distance from the lines' intersections to the join beyond which
    // the join will get mitered.  (and how does THIS happen, pray tell?)
    public Vector calcJoinPoints (Line2DFloat firstLine, Line2DFloat secondLine,
        float dist, float cutoff) 
        throws ZeroLengthException {
        Vector ret = new Vector();
        Point2DFloat firstStart = firstLine.firstEndpoint();
        Point2DFloat firstEnd = firstLine.secondEndpoint();
        Point2DFloat secondStart = secondLine.firstEndpoint();
        Point2DFloat secondEnd = secondLine.secondEndpoint();

        if (!firstEnd.equals(secondStart)) {
            EStdio.err().println("!!!!!!!!!!!!!!!!!!!!!  OOPS, LINES NOT COINCIDENT!");
        }
        
        Point2DFloat firstStartOff = firstLine.offNormal(firstStart, dist);
        Point2DFloat firstEndOff = firstLine.offNormal(firstEnd, dist);
        Point2DFloat secondStartOff = secondLine.offNormal(secondStart, dist);
        Point2DFloat secondEndOff = secondLine.offNormal(secondEnd, dist);
        
        // OK, make a line from these off points.
        Line2DFloat firstOffLine = new Line2DFloat(firstStartOff, firstEndOff);
        Line2DFloat secondOffLine = new Line2DFloat(secondStartOff, secondEndOff);

        // Get their intersection.
        try {
            Point2DFloat offIntersection = firstOffLine.intersection(secondOffLine);
            // For now, don't miter anything or handle cutoff, just return one point.
            ret.addElement(offIntersection);
        } catch (ParallelException e) {
            EStdio.err().println("!!!!!!!!!!!!!!!!!!!!! OOPS, BOUNDING LINES PARALLEL!");
        }

        return ret;
    }
        


    /*
     * derived constructors
     * (i.e. methods returning other Polygon2DFloats "based on" this one)
     */

    // return a new Polygon2DFloat that is "expanded" by the given amount
    // this uses the bounding lines calculated in addFinalLine
    // since each bounding line may or may not be coincident with one of its
    // adjacent bounding lines, we make sure we never add duplicate points
    public Polygon2DFloat makeExpanded (float expandAmount) 
        throws ZeroLengthException, NotClosedPolygonException {
        if (lines.size() == 0)
            throw new ZeroLengthException();
        if (boundingLines == null) {
            throw new NotClosedPolygonException();
        }

        if (theTrace.debug && Trace.ON) theTrace.debugm("makeExpanded for "+this+" by "+expandAmount);
        Polygon2DFloat ret = null;
        for (int i = 0; i < lines.size(); i++) {
            // get current line
            Line2DFloat boundingLine = (Line2DFloat)boundingLines.elementAt(i);
            Point2DFloat first = boundingLine.firstEndpoint();
            if (i == 0) {
                // start polygon off with first endpoint of first bounding line
                ret = new Polygon2DFloat(first.x(), first.y());
            } else if (!first.equals(ret.endPoint())) {
                // only add first point if it's not the same as previous endpoign
                ret.addLine(first.x(), first.y());
            }
            Point2DFloat second = boundingLine.secondEndpoint();
            if (i < lines.size() - 1 || !second.equals(ret.startPoint()))
                // only add second point if we're either not at the last line or
                // the last line's endpoint is different from the first line's start point
                ret.addLine(second.x(), second.y());
        }

        // complete it
        ret.addFinalLine(boundingDist);

        return ret;
    }

    // return a polygon which consists of this polygon plus the points from the other
    // polygon
    // does NOT side-effect this polygon
    public Polygon2DFloat makeAppended (Polygon2DFloat other) 
        throws ZeroLengthException {
        Polygon2DFloat ret = new Polygon2DFloat(this, 0, 0);
        if (other.lines.size() == 0)
            ret.addLine(other.startPoint());
        else for (int i = 0; i < other.lines.size(); i++) {
            // get current line
            Line2DFloat line = (Line2DFloat)other.lines.elementAt(i);
            if (i == 0) {
                ret.addLine(line.firstEndpoint().x(),   line.firstEndpoint().y());
            }
            ret.addLine(line.secondEndpoint().x(), line.secondEndpoint().y());
        }
        return ret;
    }

    /*
     * queries
     */

    // return a vector of intersecting lines from this polygon
    // return null if none (so we don't need to allocate in common non-intersection case)
    // first checks bounding box
    // then does line-segment/line-segment intersection tests
    public Vector linesIntersecting (Line2DFloat other) {
        if (lines.isEmpty())
            return null;

        // first check bounding box
        if ((other.getX1() < xmin && other.getX2() < xmin)
                || (other.getX1() > xmax && other.getX2() > xmax)
                || (other.getY1() < ymin && other.getY2() < ymin)
                || (other.getY1() > ymax && other.getY2() > ymax))
            return null;

        Vector ret = null;
        // walk through all lines, checking intersection
        for (int i = 0; i < lines.size(); i++) {
            Line2DFloat line = (Line2DFloat)lines.elementAt(i);
            if (line.intersects(other)) {
                if (ret == null)
                    ret = new Vector();
                ret.addElement(new Integer(i));
            }
        }

        return ret;
    }

    // query whether a line segment overlaps this polygon
    // do so efficiently, without allocating
    public boolean lineIntersects (Line2DFloat other) {
        if (theTrace.debug && Trace.ON) theTrace.debugm("intersecting "+this);
        if (theTrace.debug && Trace.ON) theTrace.debugm("   with "+other+".");
        if (lines.isEmpty()) {
            if (theTrace.debug && Trace.ON) theTrace.debugm("      No lines in this poly, ergo no intersection.");
            return false;
        }

        // first check bounding box
        if ((other.getX1() < xmin && other.getX2() < xmin)
                || (other.getX1() > xmax && other.getX2() > xmax)
                || (other.getY1() < ymin && other.getY2() < ymin)
                || (other.getY1() > ymax && other.getY2() > ymax)) {
            if (theTrace.debug && Trace.ON) theTrace.debugm("      Line falls outside bounding box, ergo no intersection.");
            return false;
        }

        Vector ret;
        // walk through all lines, checking intersection
        for (int i = 0; i < lines.size(); i++) {
            Line2DFloat line = (Line2DFloat)lines.elementAt(i);
            if (line.intersects(other)) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("    Detected intersection with "+line+".");
                return true;
            }
        }

        if (theTrace.debug && Trace.ON) theTrace.debugm("      No lines intersected, ergo no intersection!");
        return false;
    }

    // return whether other polygon overlaps this
    // simple-minded for now
    public boolean polygonIntersects (Polygon2DFloat other) {
        for (int i = 0; i < other.size(); i++) {
            Line2DFloat otherLine = other.lineAt(i);
            if (lineIntersects(otherLine))
                return true;
        }
        return false;
    }

    // determine whether given line intersects this polygon
    // if it does, return the (closest) point of intersection, and the line to boot
    // if not, return null
    public PolyIntersection2DFloat closestIntersection (Line2DFloat line) {
        if (theTrace.debug && Trace.ON) theTrace.debugm("  In Polygon::closestIntersection.");
        Vector intersections = linesIntersecting(line);
        if (theTrace.debug && Trace.ON) theTrace.debugm("   Intersection indices: "+intersections+".");
        // for each line, calculate intersection point
        // only keep closest intersection point
        PolyIntersection2DFloat ret = null;
        float distSquared = 0;
        for (int i = 0; i < intersections.size(); i++) {
            int lineIndex = ((Integer)intersections.elementAt(i)).intValue();
            Line2DFloat candidateLine = (Line2DFloat)lines.elementAt(lineIndex);
            // calculate intersection point
            try {
                Point2DFloat candidate = line.intersection(candidateLine);
                // calculate distance from start of line to candidate point
                Point2DFloat delta = candidate.minus(line.firstEndpoint());
                float candidateDistSquared = delta.dot(delta);
                if (theTrace.debug && Trace.ON) theTrace.debugm("   Distancesquared: "+candidateDistSquared);

                if (ret == null || candidateDistSquared < distSquared) {
                    if (theTrace.debug && Trace.ON) theTrace.debugm("   We have a new winner!");
                    distSquared = candidateDistSquared;
                    ret = new PolyIntersection2DFloat(candidate, lineIndex, this);
                }
            } catch (ParallelException e) {
            // if lines are parallel, assume they don't intersect
            }
        }

        if (theTrace.debug && Trace.ON) theTrace.debugm("   Closest intersection calculated, it's "+ret);
        return ret;
    }

}



// this exception is thrown by PolygonSet2DFloat when no route can be found
class NoRouteException extends Exception {
}

// class defining a set of 2-D polygons
// one is the "exterior" polygon, defining the limits of the plane
// all others are "interior" polygons, defining exclusion spaces
// this is the class which supports route planning, by taking a line
// segment representing the desired route, and returning a vector of line
// segments denoting an achievable route without intersecting any interior
// polygons.
// in order to work well, this requires that none of the interior polygons
// come within avoidance_dist of each other.  this can be verified by making
// expanded versions and overlap-checking them at move or add time.
public class PolygonSet2DFloat {
    /*
     * state
     */

    // the exterior polygon
    private Polygon2DFloat exterior;
    // the vector of interior polygons, as originally added
    private Hashtable interiors;
    // the offsets for each interior polygon
    // should line up with interiors
    private Hashtable offsets;
    // the vector of interior polygons, offset according to offsets
    // this duplicates all the interior state but makes comparisons much quicker
    // offsetInteriors.elementAt(x) == interiors.elementAt(x).offset(offsets.elementAt(x))
    private Hashtable offsetInteriors;
    
    static private Trace theTrace = new Trace("polygonset2dfloat");

    /* 
     * constructors
     */

    // a new empty one
    public PolygonSet2DFloat (Polygon2DFloat exteriorArg) {
        // copy the passed-in polygon
        exterior = new Polygon2DFloat(exteriorArg, 0, 0);
        interiors = new Hashtable();
        offsets = new Hashtable();
        offsetInteriors = new Hashtable();
    }

    /*
     * mutators
     */

    public void addInteriorPolygon (Object key, Polygon2DFloat interior, Point2DFloat offset) {
        interiors.put(key, new Polygon2DFloat(interior, 0, 0));
        offsets.put(key, offset);
        offsetInteriors.put(key, new Polygon2DFloat(interior, offset.x(), offset.y()));
    }
    
    public void removePolygonIfPresent (Object key) {
        if (interiors.contains(key)) {
            interiors.remove(key);
            offsets.remove(key);
            offsetInteriors.remove(key);
        }
    }
        

    /*
     * accessors
     */

    // temporary!
    public Polygon2DFloat exteriorPoly () {
        return exterior;
    }
    public Hashtable interiorPolys () {
        return offsetInteriors;
    }
    public String toString () {
        return "PolygonSet2DFloat exterior ["+exterior+"] interior count "+offsetInteriors.size();
    }

    /* 
     * queries
     */

    // return whether you can get from start to dest without intersecting exterior or
    // any interior.
    public boolean intersectsAnything (Point2DFloat start, Point2DFloat dest)
        throws ZeroLengthException {
        // INEFFICIENT!!! REWORK!
        Line2DFloat line = new Line2DFloat(start, dest);
        if (exterior.lineIntersects(line))
            return true;
        Enumeration e = offsetInteriors.elements();
        while (e.hasMoreElements()) {
            Polygon2DFloat test = (Polygon2DFloat)e.nextElement();
            if (test.lineIntersects(line))
                return true;
        }
        return false;
    }
    
    // return whether this path intersects anything.
    public boolean intersectsAnything (Polygon2DFloat path) {
        Enumeration e = offsetInteriors.elements();
        while (e.hasMoreElements()) {
            Polygon2DFloat test = (Polygon2DFloat)e.nextElement();
            if (test.polygonIntersects(path))
                return true;
        }
        return false;
    }

    // make an optimal path plan
    // takes a starting point, an ending point, and a distance-by-which-to-avoid-obstacles
    // (for robust behavior, all interior polygons must be separated by 2x dist)
    // tries to get to the destination point from the end point via getPast
    // then truncates each returned path and picks shortest one as winner
    // if no way to get there, returns null
    // (eventually should throw exception with reason why not)
    public Vector makePath (Point2DFloat start, Point2DFloat dest, float dist) 
        throws ZeroLengthException {

        if (theTrace.debug && Trace.ON) theTrace.debugm("");
        if (theTrace.debug && Trace.ON) theTrace.debugm("Making path!!!");

        // starting path
        Polygon2DFloat startPath = new Polygon2DFloat(start.x(), start.y());

        Vector candidatePaths = getPast(startPath, dest, dist);

        if (theTrace.debug && Trace.ON) theTrace.debugm("");
        if (theTrace.debug && Trace.ON) theTrace.debugm("Done making path, candidates are "+candidatePaths);

        return candidatePaths;
/*
        // just pick first path for now....
        if (candidatePaths.size() > 0)
            return (Polygon2DFloat)candidatePaths.elementAt(0);
        else
            return null;
*/

    }       

    // calculate the polygon which intersects this most closely
    // and the line within that polygon which intersects most closely
    public PolyIntersection2DFloat intersectingPoly (Line2DFloat line) {
        PolyIntersection2DFloat ret = null;
        // distance-squared from start to crashPoint.point
        float distSquared = 0;

        if (theTrace.debug && Trace.ON) theTrace.debugm("In PolygonSet::intersectingPoly.");
        // start with exterior
        if (exterior.lineIntersects(line)) {
            if (theTrace.debug && Trace.ON) theTrace.debugm(" Exterior intersects, determining closest.");
            ret = exterior.closestIntersection(line);
            Point2DFloat distVec = ret.point.minus(line.firstEndpoint());
            distSquared = distVec.dot(distVec);
            if (theTrace.debug && Trace.ON) theTrace.debugm(" Intersected exterior at "+ret+", dist "+distSquared+".");
        }

        // iterate over interiors
        Enumeration e = offsetInteriors.elements();
        while (e.hasMoreElements()) {
            Polygon2DFloat testCrash = (Polygon2DFloat)e.nextElement();
            if (testCrash.lineIntersects(line)) {
                if (theTrace.debug && Trace.ON) theTrace.debugm(" Interior "+testCrash+" intersects, determining closest.");
                PolyIntersection2DFloat testCrashPoint = testCrash.closestIntersection(line);
                Point2DFloat newDistVec = testCrashPoint.point.minus(line.firstEndpoint());
                float newDistSquared = newDistVec.dot(newDistVec);
                if (theTrace.debug && Trace.ON) theTrace.debugm(" Intersected interior at "+testCrashPoint+", dist "+newDistSquared+".");
                if (ret == null) {
                    ret = testCrashPoint;
                    distSquared = newDistSquared;
                } else if (newDistSquared < distSquared) {
                    if (theTrace.debug && Trace.ON) theTrace.debugm(" We have a new POLYGON winner!");
                    // we found a new closest crash
                    ret = testCrashPoint;
                    distSquared = newDistSquared;
                } else {
                    if (theTrace.debug && Trace.ON) theTrace.debugm(" Rejecting, not closest.");
                }               
            }
        }
        return ret;
    }


    // calculate a "way past" the next polygon
    // this takes a path-so-far and a desired endpoint
    // it determines whether the endpoint can be reached
    // if so, it returns a vector containing a single path which is pathSoFar + dest
    // if not, it "walks around" the intervening polygon, then recurses
    // it returns a vector of paths found, since you can walk around each intervener in two ways
    // if there is no path around, it returns an empty vector
    public Vector getPast (Polygon2DFloat pathSoFar, Point2DFloat dest, float dist) 
        throws ZeroLengthException {

        if (theTrace.debug && Trace.ON) theTrace.debugm("PolygonSet::getPast, pathSoFar "+pathSoFar+"...");

        // get the starting point (i.e. the endpoint of the path)
        Point2DFloat start = pathSoFar.endPoint();

        // iterate over all polygons
        // determine the closest one with which we collide
        Line2DFloat line = new Line2DFloat(start, dest);

        // closest polygon which intersects line, if any        
        PolyIntersection2DFloat crash = intersectingPoly(line);
        
        // did we find a crash point?  if not, return a vector containing a clone
        // of the passed-in path (!!! analyze for optimal allocation !!!)
        if (crash == null) {
            Vector ret = new Vector();
            Polygon2DFloat finalPath = new Polygon2DFloat(pathSoFar, 0, 0);
            finalPath.addLine(dest);
            ret.addElement(finalPath);
            return ret;
        }

        if (theTrace.debug && Trace.ON) theTrace.debugm("Ran into polygon "+crash);
        
        // OK, time to walk around it
        // first backwards, then forwards
        if (theTrace.debug && Trace.ON) theTrace.debugm("Walking clockwise first...");
        Polygon2DFloat clockwiseWalk = wayAround(start, dest, crash, 1, dist);
        if (theTrace.debug && Trace.ON) theTrace.debugm("Now walking counterwise...");
        Polygon2DFloat counterWalk = wayAround(start, dest, crash, -1, dist);

        Vector ret = new Vector();
        Vector clockwisePaths = null;
        Vector counterPaths = null;

        // if both are null, there's no way, return empty vector
        if (clockwiseWalk == null && counterWalk == null) {
            if (theTrace.debug && Trace.ON) theTrace.debugm("Couldn't get around 'im!  Returning empty vector.");
            return ret;
        }

        // for each walk, determine if it hits exterior
        // don't recurse on it if so
        if (theTrace.debug && Trace.ON) theTrace.debugm("Walks done, clockwise is "+clockwiseWalk+", counter is "+counterWalk);
        if (clockwiseWalk != null && !exterior.polygonIntersects(clockwiseWalk)) {
            // recurse on this one
            if (theTrace.debug && Trace.ON) theTrace.debugm("***Appending clockwise walk "+clockwiseWalk+" to path "+pathSoFar);
            Polygon2DFloat newPath = pathSoFar.makeAppended(clockwiseWalk);
            if (theTrace.debug && Trace.ON) theTrace.debugm("Recursing getPast on clockwise path.");
            clockwisePaths = getPast(newPath, dest, dist);
            for (int i = 0; i < clockwisePaths.size(); i++) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("Final getPast result: "+clockwisePaths);
                ret.addElement(clockwisePaths.elementAt(i));
            }
        }
        if (counterWalk != null && !exterior.polygonIntersects(counterWalk)) {
            Polygon2DFloat newPath = pathSoFar.makeAppended(counterWalk);
        if (theTrace.debug && Trace.ON) theTrace.debugm("Recursing getPast on counter path.");
            counterPaths = getPast(newPath, dest, dist);
            for (int i = 0; i < counterPaths.size(); i++) 
                ret.addElement(counterPaths.elementAt(i));
        }
        return ret;
    }

    // calculate a "way around" the given polygon
    // given a starting point, a destination, a collision point of intersection,
    // a destination, a direction, and a distance,
    // walk from the starting point around the collision polygon (distance away from it) in
    //      that direction  until you can reach the destination without colliding with
    //      this polygon.
    // if you wind up back at the starting point, throw.
    // otherwise, return the path to get around.
    public Polygon2DFloat wayAround (Point2DFloat start, Point2DFloat dest,
        PolyIntersection2DFloat obstacle, int direction, float distance) 
        throws ZeroLengthException {
        if (theTrace.debug && Trace.ON) theTrace.debugm("  PolygonSet::wayAround, starting at "+start+", getting around "+obstacle);
        // start out by moving distance units from dest towards start
        // (i.e. back off of the collision point a little)
        Point2DFloat obToStart = obstacle.point.minus(start);
        Point2DFloat normalized = obToStart.normalize();
        Point2DFloat realStart = obstacle.point.minus(normalized.times(distance));
        Polygon2DFloat ret = new Polygon2DFloat(realStart.x(), realStart.y());

        // now get index of collided-with line in obstacle
        int lineIndex = obstacle.lineIndex;

        // start walking around
        while (true) {
            if (theTrace.debug && Trace.ON) theTrace.debugm("    Iterating in wayAround, lineIndex "+lineIndex);

            // get bounding line
            Line2DFloat line;
            try {
                line = obstacle.polygon.boundingLineAt(lineIndex);
            } catch (NotClosedPolygonException e) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("!!!!!!!!!! OOPS, not closed polygon!!!");
                // if we ain't got bounding lines, the hell with everything
                return null;
            }
            // get point
            Point2DFloat point;
            // if clockwise, pick start point; if counter, pick end point
            if (direction < 0)
                point = line.firstEndpoint();
            else
                point = line.secondEndpoint();

            // if it's not already on path, add it to path
            if (!ret.endPoint().equals(point)) {
                ret.addLine(point.x(), point.y());
                if (theTrace.debug && Trace.ON) theTrace.debugm("    Adding "+point+" to path.");
                // now offPoint is the next point on the path.

                // test if we can get to dest from offPoint without colliding with this
                Line2DFloat testLine = new Line2DFloat(point, dest);
                if (!obstacle.polygon.lineIntersects(testLine)) {
                    if (theTrace.debug && Trace.ON) theTrace.debugm("    Got past obstacle!  Returning.");
                    // we won!
                    return ret;
                }
            } else {
                // this may actually legitimately happen if we get a real start point that is
                // coincident with a bounding line endpoint
                if (theTrace.debug && Trace.ON) theTrace.debugm("");
                if (theTrace.debug && Trace.ON) theTrace.debugm("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  OOPS, same point!");
                EStdio.err().println();
            }

            // it did intersect, so just keep going....
            // get index of next/previous line segment
            int nextIndex = lineIndex + direction;
            if (nextIndex >= obstacle.polygon.size())
                nextIndex = 0;
            if (nextIndex < 0)
                nextIndex = obstacle.polygon.size() - 1;
            if (theTrace.debug && Trace.ON) theTrace.debugm("    Setting nextIndex to "+nextIndex);

            // if we wrapped, we lost
            if (nextIndex == obstacle.lineIndex) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("    Went all the way around, couldn't get past, terminating.");
                return null;
            }

            lineIndex = nextIndex;
            try {
                line = obstacle.polygon.boundingLineAt(lineIndex);
            } catch (NotClosedPolygonException e) {
                if (theTrace.debug && Trace.ON) theTrace.debugm("!!!!!!!!!! OOPS, not closed polygon!!!");
                // if we ain't got bounding lines, the hell with everything
                return null;
            }

            if (theTrace.debug && Trace.ON) theTrace.debugm("    Moving on to adjacent end of next line.");
            // try opposite endpoint of the new line
            if (direction < 0)
                point = line.secondEndpoint();
            else
                point = line.firstEndpoint();

            // add it to path if it's not there already
            // (it will be there already if the joins are coincident)
            if (!ret.endPoint().equals(point)) {
                ret.addLine(point.x(), point.y());
                if (theTrace.debug && Trace.ON) theTrace.debugm("    Adding "+point+" to path.");
                // now offPoint is the next point on the path.

                // test if we can get to dest from offPoint without colliding with this
                Line2DFloat testLine = new Line2DFloat(point, dest);
                if (!obstacle.polygon.lineIntersects(testLine)) {
                    if (theTrace.debug && Trace.ON) theTrace.debugm("    Got past obstacle!  Returning.");
                    // we won!
                    return ret;
                }
            } else {
                if (theTrace.debug && Trace.ON) theTrace.debugm("    New point is already on path, ignoring.");
            }

            // else continue and fall through into the other end of this new next line.
        }
    }

    // given a path, truncate unneeded points from it.
    // an unneeded point is a point not necessary for polygon avoidance.
    // the current stupid algorithm is:  use two points, start and end.
    // set start to path start and end to path end.
    // if you can get from start to end, remove all points between start and end and
    // set start to end and end to path end.
    // otherwise, set end to the path point just before end.
    // repeat until end reaches start.
    public Polygon2DFloat truncate (Polygon2DFloat path) 
        throws ZeroLengthException {
        if (theTrace.debug && Trace.ON) theTrace.debugm("");
        if (theTrace.debug && Trace.ON) theTrace.debugm("Truncating path "+path);

        Point2DFloat startPoint = path.startPoint();
        Polygon2DFloat ret = new Polygon2DFloat(startPoint);
        // start is the index of the line whose start point we are working with
        int start = 0;
        // end is the index of the line whose end point we want to try to reach
        int end = path.size() - 1;
        // if there is less than one line, quit
        if (end < 0) {
            if (theTrace.debug && Trace.ON) theTrace.debugm("No lines, returning.");
            return ret;
        }
        // if there is only one line, quit
        if (end == 0) {
            if (theTrace.debug && Trace.ON) theTrace.debugm("Only one line, returning.");
            ret.addLine(path.lineAt(end).secondEndpoint());
            return ret;
        }

        while (start < path.size()) {
            startPoint = path.lineAt(start).firstEndpoint();
            Point2DFloat endPoint = path.lineAt(end).secondEndpoint();

            if (theTrace.debug && Trace.ON) theTrace.debugm("  Iterating, start "+start+", end "+end+", startPoint "+startPoint+", endPoint "+endPoint);

            if (end == start) {
                // we have reached our starting line.
                // this means that this line is the shortest way past things.
                // so add this line.
                // then advance past it.
                if (theTrace.debug && Trace.ON) theTrace.debugm("  Start == end, adding endpoint.");
                ret.addLine(endPoint);
                start = end + 1;
                end = path.size() - 1;
                continue;
            }

            // OK, start and end are different, so see if we can get there from here.
            if (intersectsAnything(startPoint, endPoint)) {
                // ...nope.  So, move end back one.
                end -= 1;
                if (theTrace.debug && Trace.ON) theTrace.debugm("    Oops, intersects something.");
                continue;
            }

            // YES, we CAN get there!
            // So, add endPoint to ret.
            if (theTrace.debug && Trace.ON) theTrace.debugm("    Doesn't intersect anything, adding endpoint.");
            ret.addLine(endPoint);
            // And set start past end.
            start = end + 1;
            // Set end to the end again.
            end = path.size() - 1;
            // Keep going!
        }

        return ret;
    }
}
