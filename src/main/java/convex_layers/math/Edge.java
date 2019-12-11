package convex_layers.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.MultiTool;
import tools.PublicCloneable;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
public class Edge
            implements PublicCloneable {
    /** The source of the edge. */
    private Vector v1;
    /** The destination of the edge. */
    private Vector v2;


    /**
     * @return The starting point of the edge.
     */
    public Vector v1() {
        return v1;
    }

    /**
     * @return The ending point of the edge.
     */
    public Vector v2() {
        return v2;
    }
    
    /**
     * @return The x-coordinate of the first point.
     */
    public double x1() {
        return v1.x();
    }
    
    /**
     * @return The x-coordinate of the second point.
     */
    public double x2() {
        return v2.x();
    }
    
    /**
     * @return The y-coordinate of the first point.
     */
    public double y1() {
        return v1.y();
    }
    
    /**
     * @return The y-coordinate of the second point.
     */
    public double y2() {
        return v2.y();
    }

    /**
     * Sets the starting point of the edge.
     * 
     * @param v The new starting point of the edge.
     */
    public void v1(Vector v) {
        this.v1 = v;
    }

    /**
     * Sets the ending point of the edge.
     * 
     * @param v The new ending point of the edge.
     */
    public void v2(Vector v) {
        this.v2 = v;
    }
    
    /**
     * @return The length of the edge.
     */
    public double length() {
        double x = v2.x() - v1.x();
        double y = v2.y() - v1.y();
        return Math.sqrt(x*x + y*y);
    }
    
    /**
     * Tests whether the given edge intersects another edge.
     * 
     * @param e The edge to test for.
     * @return Whether the two edges intersect.
     */
    public boolean intersects(Edge e) {
        double o1 = relOriRounded(e.v1);
        double o2 = relOriRounded(e.v2);
        double o3 = e.relOriRounded(v1);
        double o4 = e.relOriRounded(v2);
        System.out.println(o1 + ". " + o2 + ". " + o3 + ". " + o4);
        if (o1 != o2 && o3 != o4) return true;
        return o1 == 0 && o2 == 0 && o3 == 0 && o4 == 0 &&
                Math.min(v1.x(), v2.x()) <= Math.max(e.v1.x(), e.v2.x()) &&
                Math.max(v1.x(), v2.x()) >= Math.min(e.v1.x(), e.v2.x()) &&
                Math.min(v1.y(), v2.y()) <= Math.max(e.v1.y(), e.v2.y()) &&
                Math.max(v1.y(), v2.y()) >= Math.min(e.v1.y(), e.v2.y());
    }

    /**
     * The euclidean distance from the line to a point.
     * 
     * @param p The point to calculate the distance from.
     * 
     * @return The euclidean distance from this line to the given point.
     */
    public double distance(Vector p) {
        return Math.abs(normal().dot(p) + x2()*y1() - y2()*x1()) / length();
    }

    /**
     * @return The normal vector associated with this edge.
     */
    public Vector normal() {
        return new Vector(y2() - y1(), x2() - x1());
    }
    
    /**
     * Calculates the relative orientation of the given point. It is assume that the line is directed from
     * {@code v1} to {@code v2}.
     * 
     * @param v3 The point to check.
     * @return {@code < 0} if the point lies to the left, {@code 0} if the point lies on the line,
     *     or {@code > 0} if the point lies to the right.
     */
    public double relOri(Vector v3) {
        Mat2 m = new Mat2(
                v1.x() - v2.x(), v3.x() - v2.x(),
                v1.y() - v2.y(), v3.y() - v2.y()
        );
        return m.det();
    }
    
    /**
     * Same as {@link #relOri(Vector)}, but now rounds the returned value to {@code -1} if it was {@code < 0},
     * to {@code 1} if it was {@code > 0} and to {@code 0} if it was {@code = 0}.
     *
     * @param v3 The point to check.
     * @return {@code -1} if the point lies to the left, {@code 0} if the point lies on the line,
     *     or {@code 1} if the point lies to the right.
     * 
     * @see #relOri(Vector) 
     */
    public int relOriRounded(Vector v3) {
        double ans = relOri(v3);
        if (ans < 0) return -1;
        if (ans > 0) return 1;
        return 0;
    }
    
    /**
     * Calculates the angle {@code v2 v1 v3}. The angle will be positive if the point lies on the right side,
     *      * and negative if the point lies on the left side.
     * Use {@link #relOriRounded(Vector)} for determining on which side a point lies, as this is much faster.
     * 
     * @param v3 The point to calculate the angle for.
     * @return The angle {@code v2 v1 v3}.
     * 
     * @see #iangle(Vector) 
     */
    public double angle(Vector v3) {
        double angle1 = Math.atan2(v2.y() - v1.y(), v2.x() - v1.x());
        double angle2 = Math.atan2(v3.y() - v1.y(), v3.x() - v1.x());
        if (angle1 < 0) angle1 += 2*Math.PI;
        if (angle2 < 0) angle2 += 2*Math.PI;
        double result = angle2 - angle1;
        if (result > Math.PI) result -= 2*Math.PI;
        return result;
    }
    
    /**
     * Calculates the angle {@code v1 v2 v3}. The angle will be positive if the point lies on the right side,
     * and negative if the point lies on the left side.
     * Use {@link #relOriRounded(Vector)} for determining on which side a point lies, as this is much faster.
     *
     * @param v3 The point to calculate the angle for.
     * @return The angle {@code v1 v2 v3}.
     * 
     * @see #angle(Vector) 
     */
    public double iangle(Vector v3) {
        double angle1 = Math.atan2(v1.y() - v2.y(), v1.x() - v2.x());
        double angle2 = Math.atan2(v3.y() - v2.y(), v3.x() - v2.x());
        double result = angle2 - angle1;
        if (result > Math.PI) result -= 2*Math.PI;
        return -result;
    }
    
    @Override
    public int hashCode() {
        return MultiTool.calcHashCode(new Object[] {v1, v2});
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge)) return false;
        Edge e = (Edge) obj;
        return Objects.equals(e.v1, v1) && Objects.equals(e.v2, v2);
    }
    
    @Override
    public Edge clone() {
        return new Edge(v1.clone(), v2.clone());
    }
    
    @Override
    public String toString() {
        return "{" + v1.toString() + "," + v2.toString() + "}";
    }
    
}
