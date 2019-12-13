package convex_layers;

import convex_layers.math.Edge;
import tools.Var;
import tools.log.FileLogger;
import tools.log.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Main project class. <br>
 * This class delegates the work for solving the problem.
 */
public class ConvexLayers {

    private static void initLogger() {
        try {
            Logger.setDefaultLogger(new FileLogger(Var.LOG_FILE));

        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * The main function used to initialize the program.
     *
     * @param args The runtime arguments.
     */
    public static void main(String[] args) {
        initLogger();

        String folder = "challenge_1";
        String type = "uniform";
        String name = "uniform-0000015-1";
        String path = "data/" + folder + "/" + type + "/" + name;
        File inFile = new File(path + ".instance.json");
        File outFile = new File(path + ".solution.json");
        new ConvexLayers().solve(inFile, outFile);

    }

    /**
     * Finish hulling on set from a point v in clockwise direction
     *
     * @param v   Assume that v is the vertex with the rightmost x coordinate in the set
     * @param set
     */
    public static void finishHull(InputVertex v, Set<InputVertex> set) {
        if (v.getPrev() == null) {
            // use a fake vertex to begin constructing the hull, works because v has the maximum x coordinate in the set
            v.setPrev(new InputVertex(-1, v.getX() + 10, v.getY(), false, null, null));
        }
        // Do loop until the hull is complete
        do {
            v.setHulled(true);
            InputVertex next = nextCH(v.getPrev(), v, set);
            v.setNext(next);
            next.setPrev(v);
            v = next;
        } while (!v.isHulled());
    }

    /**
     * Print a hull to the console for debugging purposes
     *
     * @param hull a vertex of the hull
     */
    public static void printHull(InputVertex hull) {
        InputVertex v = hull;
        do {
            System.out.print(v.getId() + ">");
            v = v.getNext();
        } while (!v.equals(hull));
        System.out.println(hull.getId());
    }

    /**
     * Solve the problem of a given input file and write it to the given output file
     */
    public void solve(File inFile, File outFile) {
        /** Set containing the edges of the resulting convex partitioning. */
        Set<OutputEdge> solution = new HashSet<>();

        /** Vertex of the inner convex hull. */
        InputVertex innerVertex = null;

        /** Vertex of the outer convex hull. */
        InputVertex outerVertex = null;

        /** Set that keeps track of all variables that still need to be checked. */
        Set<InputVertex> remaining;

        // Read input file for relevant data
        Problem problem = readInput(inFile);
        remaining = problem.getVertices();
        String instanceName = problem.getName();

        // Build complete outer convex hull
        Logger.write("Building initial outer hull");
        outerVertex = rightMost(remaining);
        finishHull(outerVertex, remaining);

        // Add edges of outer CH to solution and delete these vertices from remaining
        Logger.write("Adding outer hull to solution");
        InputVertex prev = outerVertex;
        do {
            InputVertex next = prev.getNext();
            solution.add(new OutputEdge(prev, next));
            remaining.remove(prev);
            prev = next;
        } while (!prev.equals(outerVertex));

        // Also check case where outer convex hull contains all points of problem, else build inner hull
        if (!remaining.isEmpty()) {
            // Build complete inner convex hull
            Logger.write("Building initial inner hull");
            innerVertex = rightMost(remaining);
            finishHull(innerVertex, remaining);
            printHull(innerVertex);
        }

        // Loop until the convex partition is finished
        while (!remaining.isEmpty()) {

            // Note inLeft is further than inRight in the clockwise direction!
            InputVertex inRight = innerVertex;
            InputVertex inLeft = innerVertex.getNext();

            // Find all vertices on the outer hull that intersect with the line
            InputVertex[] intersect = hullLineIntersect(outerVertex, inRight, inLeft);
            Logger.write(intersect[0].getId() + ", " + intersect[1].getId() + ", " + intersect[2].getId() + ", " + intersect[3].getId());

            // Trivially by the algorithm, these edges can be added
            solution.add(new OutputEdge(intersect[2], inRight));
            solution.add(new OutputEdge(intersect[1], inLeft));

            // Find the next edges we can add to the outer hull according to the algorithm, unless we have a line
            if (!inRight.getPrev().equals(inLeft)) {
                while (angle(inRight.getNext(), inRight, intersect[3]) > angle(inRight.getNext(), inRight, inRight.getPrev())) {
                    Logger.write("Walk counterclockwise");
                    inRight = inRight.getPrev();
                    solution.add(new OutputEdge(intersect[2], inRight));
                }
                while (angle(intersect[0], inLeft, inLeft.getPrev()) > angle(inLeft.getNext(), inLeft, inLeft.getPrev())) {
                    Logger.write("Walk clockwise");
                    inLeft = inLeft.getNext();
                    solution.add(new OutputEdge(intersect[1], inLeft));
                }
            }

            // By above loop we know we have to add these edges
            solution.add(new OutputEdge(intersect[0], inLeft));
            solution.add(new OutputEdge(intersect[3], inRight));

            // Remove the inner part from remaining while adding their edges to the solution
            InputVertex inRemove = inRight;
            while (!inRemove.equals(inLeft)) {
                remaining.remove(inRemove);
                solution.add(new OutputEdge(inRemove, inRemove.getNext()));
                inRemove = inRemove.getNext();
            }
            remaining.remove(inLeft);

            // Update the outer and inner, if still not done
            innerVertex = inRight.getPrev();
            if (innerVertex.equals(inLeft)) {
                break;
            } else {
                inRight.getPrev().setNext(null);
                inLeft.getNext().setPrev(null);

                intersect[0].setPrev(inLeft);
                inLeft.setNext(intersect[0]);
                intersect[3].setNext(inRight);
                inRight.setPrev(intersect[3]);
                outerVertex = intersect[3];
            }

            finishHull(innerVertex, remaining);
        }

        Logger.write("Writing output to file");
        saveToFile(solution, outFile, instanceName);
    }

    /**
     * Read input file
     *
     * @param file The input file
     * @return The problem statement in its own class
     */
    private Problem readInput(File file) {
        Logger.write("Reading " + file.getAbsolutePath());
        JSONObject json = null;
        try {
            json = new JSONObject(new JSONTokener(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Set<InputVertex> vertices = new HashSet<>();
        String name = json.getString("name");
        JSONArray points = json.getJSONArray("points");
        for (int i = 0; i < points.length(); i++) {
            JSONObject point = points.getJSONObject(i);
            int id = point.getInt("i");
            double x = point.getDouble("x");
            double y = point.getDouble("y");
            System.out.println("id: " + id + ", x: " + x + ", y : " + y);
            vertices.add(new InputVertex(id, x, y, false, null, null));
        }

        return new Problem(name, vertices);
    }

    /**
     * Output a solution to a file
     */
    private static void saveToFile(Set<OutputEdge> sol, File file, String instanceName) {
        Logger.write("Saving solution of " + sol.size() + " edges to " + file.getAbsolutePath());
        JSONObject json = new JSONObject();
        json.put("type", "Solution");
        json.put("instance_name", instanceName);
        JSONObject meta = new JSONObject();
        meta.put("comment", "This is generated by the messy algorithm");
        json.put("meta", meta);

        JSONArray edges = new JSONArray();
        for (OutputEdge edge : sol) {
            JSONObject e = new JSONObject();
            e.put("i", edge.getV1().getId());
            e.put("j", edge.getV2().getId());
            edges.put(e);
        }
        json.put("edgs", edges);

        try (PrintWriter out = new PrintWriter(file)) {
            out.println(json.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Find the 4 vertices of a hull that intersect a line line1->line2 though two points
     *
     * @param hull A vertex forming hull by next/prev
     * @param l1   First point denoting line
     * @param l2   Second point denoting line
     * @return [(right of line), (left of line), (left of line), (right of line)]
     * where the first edge is the one furthest in the direction the line points in
     */
    private static InputVertex[] hullLineIntersect(InputVertex hull, InputVertex l1, InputVertex l2) {
        Logger.write("Intersecting hull " + hull.getId() + " with line " + l1.getId() + ", " + l2.getId());

        InputVertex e1 = null;
        InputVertex e2 = null;
        InputVertex e3 = null;
        InputVertex e4 = null;
        InputVertex v = hull;
        do {
            InputVertex next = v.getNext();
            if (intersects(v, next, l1, l2)) {
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

        return new InputVertex[]{e1, e2, e3, e4};
    }

    /**
     * Whether line segment [e1, e2] intersects the line through points l1 and l2
     *
     * @param e1
     * @param e2
     * @param l1
     * @param l2
     * @return
     */
    private static boolean intersects(InputVertex e1, InputVertex e2, InputVertex l1, InputVertex l2) {
        boolean e1left = leftOfLine(e1, l1, l2);
        boolean e2left = leftOfLine(e2, l1, l2);
        boolean intersect = e1left ^ e2left; // XOR
        return intersect;
    }

    /**
     * Whether the vertex v is left of the line l1->l2, otherwise right
     * Assumes no three collinear.
     *
     * @param v
     * @param l1
     * @param l2
     * @return
     */
    private static boolean leftOfLine(InputVertex v, InputVertex l1, InputVertex l2) {
        return new Edge(l1, l2).relOri(v.getV()) < 0;
        //return angle(l1, l2, v) > Math.PI;
    }

    /**
     * Find a vertex with maximal x-coordinate in the given data.
     *
     * @param data The data to search in.
     * @return The vertex with the maximal x-coordinate.
     */
    private InputVertex rightMost(Iterable<InputVertex> data) {
        InputVertex maxX = null;
        for (InputVertex v : data) {
            if (v.getX() > maxX.getX()) {
                maxX = v;
            }
        }
        return maxX;
    }

    /**
     * Method based on giftWrapping.
     * Finds the next point on the convex hull assuming checking in clockwise order in range [-PI,PI]
     *
     * @param v   Last point on the already found hull in clockwise order
     * @param set Set to find the next point from
     * @return
     */
    public static InputVertex nextCH(InputVertex prev, InputVertex v, Set<InputVertex> set) {
        double maxAngle = -Double.MAX_VALUE;
        InputVertex bestVertex = v;
        for (InputVertex candidate : set) {
            // to avoid adding vertex itself
            if (!candidate.equals(v)) {
                double angle = angle(prev, v, candidate);
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
     *
     * @param v1
     * @param v2
     * @param v3
     * @return
     */
    private static double angle(InputVertex v1, InputVertex v2, InputVertex v3) {
        //return angle(v1.sub(v2), v3.sub(v2));
        return new Edge(v2, v1).angle(v3.getV());
    }

//    /**
//     * Angle from first point to 2nd point [0, 2*PI]
//     * @param v1
//     * @param v2
//     * @return
//     */
//    private static double angle(InputVertex v1, InputVertex v2) {
//        return (angle(v2) - angle(v1) + 2 * Math.PI) % (2 * Math.PI);
//    }
//
//    /**
//     * Angle w.r.t. origin of a vertex interpreted as a line from source to the point [-PI, PI]
//     * @param v
//     * @return
//     */
//    private static double angle(InputVertex v) {
//        return Math.atan2(v.getY(), v.getX());
//    }


}
