package convex_layers.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.PublicCloneable;

@Setter
@Getter
@AllArgsConstructor
public class Edge
            implements PublicCloneable {
    private Vector v1;
    private Vector v2;

    /**
     * @return The length of the edge.
     */
    public double length() {
        double x = v2.x() - v1.x();
        double y = v2.y() - v1.y();
        return Math.sqrt(x*x + y*y);
    }

    public boolean intersects(Edge e) {
        return relOri(e.v1) * relOri(e.v2) < 0;
    }

    /**
     * Calculates the relative orientation of the given point.
     * 
     * @param v3 The point to check.
     * @return {@code < 0} if the point lies to the left, {@code 0} if the point lies on the line,
     *     or {@code > 0} if the point lies to the right.
     */
    public double relOri(Vector v3) {
        Mat2 m = new Mat2(
                v2.x() - v1.x(), v3.x() - v1.x(),
                v2.y() - v1.y(), v3.y() - v1.y()
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
        if (ans > 1) return 1;
        return 0;
    }

    /**
     * Calculates the angle {@code v2 v1 v3}. Note that the angle is always positive.
     * Use {@link #relOriRounded(Vector)} for determining on which side a point lies.
     * 
     * @param v3 The point to calculate the angle for.
     * @return The angle {@code v2 v1 v3}.
     * 
     * @see #iangle(Vector) 
     */
    public double angle(Vector v3) {
        double angle1 = Math.atan2(v1.y() - v2.y(), v1.x() - v2.x());
        double angle2 = Math.atan2(v1.y() - v3.y(), v1.x() - v3.x());
        return Math.abs(angle1) - Math.abs(angle2);
    }

    /**
     * Calculates the angle {@code v1 v2 v3}. Note that the angle is always positive.
     * Use {@link #relOriRounded(Vector)} for determining on which side a point lies.
     *
     * @param v3 The point to calculate the angle for.
     * @return The angle {@code v1 v2 v3}.
     * 
     * @see #angle(Vector) 
     */
    public double iangle(Vector v3) {
        double angle1 = Math.atan2(v2.y() - v1.y(), v2.x() - v1.x());
        double angle2 = Math.atan2(v2.y() - v3.y(), v2.x() - v3.x());
        return Math.abs(angle1) - Math.abs(angle2);
    }
    
    @Override
    public Edge clone() {
        return new Edge(v1.clone(), v2.clone());
    }
    
}
