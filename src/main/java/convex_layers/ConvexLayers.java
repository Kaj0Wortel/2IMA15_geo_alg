package convex_layers;

import java.util.HashSet;
import java.util.Set;

/**
 * Main project class.
 */
public class ConvexLayers {
    //Set that keeps track of all variables that still need to be checked.
    Set<Vertex> todo;
    //Set containing the edges of the resulting convex partitioning
    Set<Edge> sol = new HashSet<Edge>();
    //Linked list of the inner convex hull
    Vertex in;
    //Linked list of the outer convex hull
    Vertex out;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        new ConvexLayers().solve();
    }

    /**
     * Finish hulling on set from a point v in clockwise direction
     * @param v
     * @param set
     */
    public static void finishHull(Vertex v, Set<Vertex> set) {
        do {
            v.hulled = true;
            Vertex next = nextCH(v, set);
            v.next = next;
            next.prev = v;
            v = next;
        } while (!v.hulled);
    }

    public void solve() {
        todo = readInput();

        // Build complete outer convex hull
        out = rightMost(todo);
        finishHull(out, todo);


        // Add edges of outer CH to solution and delete these vertices from todo
        Vertex prev = out;
        while (! prev.equals(out.prev)) {
            Vertex next = prev.next;
            sol.add(new Edge(prev, next));
            todo.remove(prev);
            prev = prev.next;
        }

        // Build complete inner convex hull
        in = rightMost(todo);
        finishHull(in, todo);

        // Recurse
        boolean done = false;
        while (! done) {
            Vertex[] intersect = hullLineIntersect(out, in, in.next);

            Vertex inRight = in;
            Vertex inLeft = in.next;
            sol.add(new Edge(intersect[2], inRight));
            sol.add(new Edge(intersect[1], inLeft));

            while (angle(inRight.next, inRight, intersect[3]) > angle(inRight.next, inRight, inRight.prev)) {
                inRight = inRight.prev;
                sol.add(new Edge(intersect[2], inRight));
            }
            while (angle(intersect[0], inLeft, inLeft.prev) > angle(inLeft.next, inLeft, inLeft.prev)) {
                inLeft = inLeft.next;
                sol.add(new Edge(intersect[1], inLeft));
            }

            sol.add(new Edge(intersect[0], inLeft));
            sol.add(new Edge(intersect[3], inRight));

            // Remove the inner part from todo while adding their edges to sol
            do {
                todo.remove(inRight);
                if (!inRight.equals(inLeft)) {
                    sol.add(new Edge(inRight, inRight.next));
                }
                inRight = inRight.next;
            }
            while (! inRight.equals(inLeft));

            // Update the outer and inner, if still not done
            in = inRight.prev;
            if (in.equals(inLeft)) {
                done = true;
            } else {
                inRight.prev.next = null;
                inLeft.next.prev = null;

                intersect[0].prev = inLeft;
                inLeft.next = intersect[0];
                intersect[3].next = inRight;
                inRight.prev = intersect[3];
                out = intersect[3];
            }

            if (!done) {
                finishHull(in, todo);
            }
        }

        output(sol);
    }

    private Set<Vertex> readInput() {
        // TODO
        return new HashSet<Vertex>();
    }

    private void output(Set<Edge> set) {
        for (Edge e : set) {
            System.out.println(e.v1.id + " " + e.v2.id);
        }
    }

    /**
     * Whether a set of points is convex
     * @param set
     * @return
     */
    private static boolean convex(Set<Vertex> set) {
        // todo, but probablt not even needed
        return false;
    }

    /**
     * Find the 4 vertices of a hull that intersect a line line1->line2 though two points
     * @param hull A vertex forming hull by next/prev
     * @param l1 First point denoting line
     * @param l2 Second point denoting line
     * @return [(right of line), (left of line), (left of line), (right of line)]
     * where the first edge is the one furthest in the direction the line points in
     */
    private static Vertex[] hullLineIntersect(Vertex hull, Vertex l1, Vertex l2) {
        Vertex e11 = null;
        Vertex e12 = null;
        Vertex e21 = null;
        Vertex e22 = null;
        Vertex v = hull;
        while (!v.equals(hull.prev)) {
            Vertex next = v.next;
            if (intersects(v, next, l1, l2)) {
                if (e11 == null) {
                    e11 = v;
                    e12 = next;
                } else {
                    e21 = v;
                    e22 = next;
                    break;
                }
            }
            v = next;
        }

        // TODO order correctly
        return new Vertex[]{e11, e12, e22, e21};
    }

    /**
     * Whether line segment [e1, e2] intersects the line through points l1 and l2
     * @param e1
     * @param e2
     * @param l1
     * @param l2
     * @return
     */
    private static boolean intersects(Vertex e1, Vertex e2, Vertex l1, Vertex l2) {
        return leftOfLine(e1, l1, l2) ^ leftOfLine(e2, l1, l2);
    }

    /**
     * Whether the vertex v is left of the line l1->l2
     * @param v
     * @param l1
     * @param l2
     * @return
     */
    private static boolean leftOfLine(Vertex v, Vertex l1, Vertex l2) {
        return angle(l1, l2, v) > 0;
    }

    /**
     * Find a vertex with maximal x
     * @param set
     * @return
     */
    private Vertex rightMost(Set<Vertex> set) {
        Vertex maxX = set.iterator().next();
        for (Vertex v : set) {
            if (v.x > maxX.x) {
                maxX = v;
            }
        }
        return maxX;
    }

    /**
     * Finds the next point on the convex hull assuming checking in clockwise order in range [-PI,PI]
     * @param v Last point on the already found hull in clockwise order
     * @param set Set to find the next point from
     * @return
     */
    public static Vertex nextCH(Vertex v, Set<Vertex> set) {
        double maxAngle = Double.MIN_VALUE;
        Vertex bestVertex = null;
        for (Vertex candidate : set) {
            double angle = angle(v, candidate);
            if (angle > maxAngle) {
                maxAngle = angle;
                bestVertex = candidate;
            }
        }
        return bestVertex;
    }

    /**
     * Angle of a point inbetween two lines.
     * Taking line v1 -> v2 -> v3, the given angle is the angle the right side of this line
     * @param v1
     * @param v2
     * @param v3
     * @return
     */
    private static double angle(Vertex v1, Vertex v2, Vertex v3) {
        return angle(v1.sub(v2), v3.sub(v2));
    }

    /**
     * Angle between two points interpreted as lines from source to the points
     * @param v1
     * @param v2
     * @return
     */
    private static double angle(Vertex v1, Vertex v2) {
        return angle(v2) - angle(v1);
    }

    /**
     * Angle w.r.t. origin of a vertex interpreted as a line from source to the point
     * @param v
     * @return
     */
    private static double angle(Vertex v) {
        return Math.atan2(v.y, v.x);
    }
}
