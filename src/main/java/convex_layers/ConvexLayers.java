package convex_layers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Main project class.
 */
public class ConvexLayers {

    private class Pair<A, B> {
        A fst;
        B snd;

        public Pair(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }

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

        String folder = "challenge_1";
        String type = "uniform";
        String name = "uniform-0000015-1";
        String path = "data/"+ folder + "/" + type + "/" + name ;
        File inFile = new File(path + ".instance.json");
        File outFile = new File(path + ".solution.json");
        new ConvexLayers().solve(inFile, outFile);
    }

    /**
     * Finish hulling on set from a point v in clockwise direction
     * @param v
     * @param set
     */
    public static void finishHull(Vertex v, Set<Vertex> set) {
        if (v.prev == null) {
            v.prev = new Vertex(-1, v.x + 10, v.y);
        }
        do {
            v.hulled = true;
            Vertex next = nextCH(v.prev, v, set);
//            System.out.println("Next from " + v.id + " is " + next.id);
            v.next = next;
            next.prev = v;
            v = next;
        } while (!v.hulled);
    }

    public static void printHull(Vertex hull) {
        Vertex v = hull;
        do {
            System.out.print(v.id + ">");
            v = v.next;
        } while (!v.equals(hull));
        System.out.println(hull.id);
    }

    /**
     * Solve the problem of a given input file
     */
    public void solve(File inFile, File outFile) {
        Pair<Set<Vertex>, String> input = readInput(inFile);
        todo = input.fst;
        String instanceName = input.snd;

        // Build complete outer convex hull
        System.err.println("Building initial outer hull");
        out = rightMost(todo);
        finishHull(out, todo);
        printHull(out);


        // Add edges of outer CH to solution and delete these vertices from todo
        System.err.println("Adding outer hull to solution");
        Vertex prev = out;
        do {
            Vertex next = prev.next;
            sol.add(new Edge(prev, next));
            todo.remove(prev);
            prev = next;
        } while (!prev.equals(out));

        boolean done = false;
        // Also check case where outer convex hull contains all points of problem, else build inner hull
        if (!todo.isEmpty()) {
            // Build complete inner convex hull
            System.err.println("Building initial inner hull");
            in = rightMost(todo);
            finishHull(in, todo);
            printHull(in);
        } else {
            done = true;
        }

        // Recurse
        while (! done) {
            System.err.println("Loop. ");
            System.out.print("Inner in loop: ");
            printHull(in);

            // Case nothing left, done
            if (todo.isEmpty()) {
                System.err.println("Nothing left to do!");
                done = true;
                break;
            }
            // Case 1 dot left
            if (todo.size() == 1) {
                System.err.println("TODO has only 1 left!");
                // TODO Add edge to a random one of outer,
                //  check intersect,
                //  add two more
                // actually this should work fine
//                done = true;
//                break;
            }

            Vertex inRight = in;
            Vertex inLeft = in.next;
            Vertex[] intersect = hullLineIntersect(out, inRight, inLeft);
            System.err.println(intersect[0].id + ", " + intersect[1].id + ", " + intersect[2].id + ", " + intersect[3].id);
            sol.add(new Edge(intersect[2], inRight));
            sol.add(new Edge(intersect[1], inLeft));

            if (!inRight.prev.equals(inLeft)) {
                while (angle(inRight.next, inRight, intersect[3]) > angle(inRight.next, inRight, inRight.prev)) {
                    System.err.println("Walk right");
                    inRight = inRight.prev;
                    sol.add(new Edge(intersect[2], inRight));
                }
                while (angle(intersect[0], inLeft, inLeft.prev) > angle(inLeft.next, inLeft, inLeft.prev)) {
                    System.err.println("Walk left");
                    inLeft = inLeft.next;
                    sol.add(new Edge(intersect[1], inLeft));
                }
            }

//            if (true) return;

            sol.add(new Edge(intersect[0], inLeft));
            sol.add(new Edge(intersect[3], inRight));

            // Remove the inner part from todo while adding their edges to sol
            Vertex inRemove = inRight;
            while (!inRemove.equals(inLeft)) {
                todo.remove(inRemove);
                if (!inRemove.equals(inLeft)) {
                    sol.add(new Edge(inRemove, inRemove.next));
                }
                inRemove = inRemove.next;
            }
            todo.remove(inLeft);

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

        System.err.println("Outputting");
        printSolution(sol);
        saveToFile(sol, outFile, instanceName);
    }

    /**
     * Read input file
     * @param file
     * @return
     */
    private Pair<Set<Vertex>, String> readInput(File file) {
        System.out.println("Reading " + file.getAbsolutePath());
        JSONObject json = null;
        try {
            json = new JSONObject(new JSONTokener(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Set<Vertex> set = new HashSet<>();
        String instanceName = json.getString("name");
        JSONArray points = json.getJSONArray("points");
        for (int i = 0; i < points.length(); i++) {
            JSONObject point = points.getJSONObject(i);
            int id = point.getInt("i");
            double x = point.getDouble("x");
            double y = point.getDouble("y");
            System.out.println("id: " + id + ", x: " + x + ", y : " + y);
            set.add(new Vertex(id, x, y));
        }

        return new Pair<>(set, instanceName);
    }

    /**
     * Output a solution to a file
     */
    private static void saveToFile(Set<Edge> sol, File file, String instanceName) {
        System.err.println("Saving solution of " + sol.size() + " edges to " + file.getAbsolutePath());
        JSONObject json = new JSONObject();
        json.put("type", "Solution");
        json.put("instance_name", instanceName);
        JSONObject meta = new JSONObject();
        meta.put("comment", "This is generated by the messy algorithm");
        json.put("meta", meta);

        JSONArray edges = new JSONArray();
        for (Edge edge : sol) {
            JSONObject e = new JSONObject();
            e.put("i", edge.v1.id);
            e.put("j", edge.v2.id);
            edges.put(e);
        }
        json.put("edgs", edges);

        String jsonString = json.toString(2);

        try (PrintWriter out = new PrintWriter(file)) {
            out.println(json.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Output a solution to screen
     * @param sol
     */
    private static void printSolution(Set<Edge> sol) {
        for (Edge e : sol) {
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
        System.err.println("Intersecting hull " + hull.id + " with line " + l1.id + ", " + l2.id);

        Vertex e1 = null;
        Vertex e2 = null;
        Vertex e3 = null;
        Vertex e4 = null;
        printHull(hull);
        int testCounter = 0;
        Vertex v = hull;
        do {
            Vertex next = v.next;
            if (intersects(v, next, l1, l2)) {
                testCounter ++;
                if (e1 == null & leftOfLine(v, l1, l2)) {
                    e1 = next;
                    e2 = v;
                } else {
                    e3 = next;
                    e4 = v;
                }

            }
            v = next;
        } while (!v.equals(hull));

        // TODO Handle more properly, but shouldn't happen anyways
        if (testCounter != 2) {
            System.out.println("ERROR! Found " + testCounter + " intersections");
            System.exit(-1);
        }

        return new Vertex[]{e1, e2, e3, e4};
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
//        System.err.println("Testing intersection of segment " + e1.id + " " + e2.id +
//                " with line " + l1.id + " " + l2.id);
        boolean e1left = leftOfLine(e1, l1, l2);
        boolean e2left = leftOfLine(e2, l1, l2);
        boolean intersect = e1left ^ e2left; // XOR
//        System.err.println("As " + e1.id + (e1left ? " left" : " right") +
//                ", " + e2.id + (e2left ? " left" : " right")  +" it's " + intersect);
        return  intersect;
    }

    /**
     * Whether the vertex v is left of the line l1->l2, otherwise right
     * Assumes no three collinear
     * @param v
     * @param l1
     * @param l2
     * @return
     */
    private static boolean leftOfLine(Vertex v, Vertex l1, Vertex l2) {
//        double angle = angle(l1, l2, v);
//        System.err.println("Since angle " + l1.id + " " + l2.id + " " + v.id + " = " + angle +
//                ", " + v.id + (angle > Math.PI ? " left" : " right") + " of line " + l1.id +" " + l2.id);
        return angle(l1, l2, v) > Math.PI;
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
    public static Vertex nextCH(Vertex prev, Vertex v, Set<Vertex> set) {
//        System.out.println("Finding extCH of " + v.id + " from " + prev.id);
        double maxAngle = -Double.MAX_VALUE;
        Vertex bestVertex = v;
        for (Vertex candidate : set) {
            if (!candidate.equals(v)) {
                double angle = angle(prev, v, candidate);
//                System.out.println("Angle with " + candidate.id + ": " + angle);
                if (angle > maxAngle) {
                    maxAngle = angle;
                    bestVertex = candidate;
                }
            }
        }
        return bestVertex;
    }

    /**
     * Angle of a point inbetween two lines [0, 2*PI]
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
     * Angle from first point to 2nd point [0, 2*PI]
     * @param v1
     * @param v2
     * @return
     */
    private static double angle(Vertex v1, Vertex v2) {
        return (angle(v2) - angle(v1) + 2 * Math.PI) % (2 * Math.PI);
    }

    /**
     * Angle w.r.t. origin of a vertex interpreted as a line from source to the point [-PI, PI]
     * @param v
     * @return
     */
    private static double angle(Vertex v) {
        return Math.atan2(v.y, v.x);
    }
}
