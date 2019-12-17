package convex_layers;

import convex_layers.math.Edge;
import convex_layers.visual.Visualizer;
import tools.Var;
import tools.data.array.ArrayTools;
import tools.log.FileLogger;
import tools.log.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main project class. <br>
 * This class delegates the work for solving the problem.
 */
public class ConvexLayers {

    private final Visualizer vis = new Visualizer(
            new ArrayList<>(),
            new ArrayList<>()
    );
    private final boolean visualize = true;
    Set<InputVertex> allVertices = new HashSet<>();
    Set<OutputEdge> solution = new HashSet<>();

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
     * @param v   A vertex of the to-be-created hull. If the hull is currently only of size 1, this vertex has to be
     *            the rightmost vertex of the resulting set
     * @param set The set from which to sample points for hulling, including all vertices already in the hull.
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
     * @param hull A vertex of the hull
     */
    public static void printHull(InputVertex hull) {
        printHull(hull, true);
    }

    /**
     * Go over the hull to return all points. Optionally also print to console.
     *
     * @param hull a vertex of the hull
     */
    public static List<InputVertex> printHull(InputVertex hull, boolean print) {
        HashSet<InputVertex> seen = new HashSet<>();
        List<InputVertex> hullOrder = new ArrayList<>();
        InputVertex v = hull;
        do {
            System.out.print(v.getId() + ">");
            if (seen.contains(v)) {
                System.out.print("Stopped because the hull loops but not onto the starting point. _□. Started at: ");
                break;
            }
            seen.add(v);
            hullOrder.add(v);
            v = v.getNext();
        } while (!v.equals(hull));
        hullOrder.add(v);
        System.out.println(hull.getId());
        return hullOrder;
    }

    /**
     * Solve the problem of a given input file and write it to the given output file
     */
    public void solve(File inFile, File outFile) {
        // One vertex of the inner convex hull
        InputVertex innerVertex = null;

        // One vertex of the outer convex hull
        InputVertex outerVertex = null;

        // Set that keeps track of all vertices that are part of or inside the inner hull
        Set<InputVertex> remaining;

        // Read input file for relevant data
        Problem problem = readInput(inFile);
        remaining = problem.getVertices();
        String instanceName = problem.getName();

        allVertices.addAll(problem.getVertices());

        // Draw the input
        if (visualize) {
            vis.setPoints(List.of(Visualizer.toVec(remaining)));
            vis.addLabel(Visualizer.toLabel(remaining));
            vis.redraw();
        }


        // Build complete outer convex hull
        Logger.write("Building initial outer hull");
        outerVertex = rightMost(remaining);
        finishHull(outerVertex, remaining);
        printHull(outerVertex);

        // Add edges of outer CH to solution and delete these vertices from remaining
        Logger.write("Adding outer hull to solution");
        InputVertex prev = outerVertex;
        do {
            InputVertex next = prev.getNext();
            solution.add(new OutputEdge(prev, next));
            remaining.remove(prev);
            prev = next;
        } while (!prev.equals(outerVertex));

        // Check for case where outer convex hull contains all points of problem, else build inner hull
        if (!remaining.isEmpty()) {
            // Build complete inner convex hull
            Logger.write("Building initial inner hull");
            innerVertex = rightMost(remaining);
            finishHull(innerVertex, remaining);
            printHull(innerVertex);
        } else {
            Logger.write("Initial outer hull contains all points of problem");
        }


        // Loop until the convex partition is finished
        while (!remaining.isEmpty()) {

            // Pick one edge (two neighbouring vertices) on the inner hull
            // NB: inLeft is further than inRight in the clockwise direction!
            InputVertex inRight = innerVertex;
            InputVertex inLeft = innerVertex.getNext();

            visualize(remaining, innerVertex, outerVertex, inLeft, inRight);

            if (remaining.size() == 1) {
                // One remaining, finish up by connecting three
                solution.add(new OutputEdge(innerVertex, outerVertex));
                InputVertex outIntersect = outerVertex.getNext();
                while (angle(outerVertex, innerVertex, outIntersect) > Math.PI) {
                    outIntersect = outIntersect.getNext();
                }
                solution.add(new OutputEdge(innerVertex, outIntersect.getPrev()));
                solution.add(new OutputEdge(innerVertex, outIntersect));

                break;
            }

            // Find all vertices on the outer hull that intersect with the line
            InputVertex[] intersect = hullLineIntersect(outerVertex, inRight, inLeft);
            System.out.println(intersect[0].getId());
            System.out.println(intersect[1].getId());
            System.out.println(intersect[2].getId());
            System.out.println(intersect[3].getId());
            Logger.write(intersect[0].getId() + ", " + intersect[1].getId() + ", " + intersect[2].getId() + ", " + intersect[3].getId());

            // Add the edges from the middle two points on the outer hull
            // to the two points of the edge of the inner hull
            solution.add(new OutputEdge(intersect[2], inRight));
            solution.add(new OutputEdge(intersect[1], inLeft));

            // We want to also add edges from the outer two reported outer-hull points to some points on the inner hull
            // closest to the edge of the inner hull. Thus, walk left/right from this edge until we can add an edge
            // to the inner hull without intersecting other edges.
            if (!inRight.getPrev().equals(inLeft)) { // We haven't gone full circle
                System.out.println("Walking LR");
                // TODO Replace with left-right test
                // While the edge from inRight to the outerHull-point intersects the hull, move along while adding edges
                System.out.println("Next:");
                System.out.println(inRight.getNext());
                while (angle(inRight.getNext(), inRight, intersect[3]) >
                        angle(inRight.getNext(), inRight, inRight.getPrev())) {
                    Logger.write("Walk counterclockwise");
                    inRight = inRight.getPrev();
                    solution.add(new OutputEdge(intersect[2], inRight));
                }
                while (angle(intersect[0], inLeft, inLeft.getPrev()) >
                        angle(inLeft.getNext(), inLeft, inLeft.getPrev())) {
                    Logger.write("Walk clockwise");
                    inLeft = inLeft.getNext();
                    solution.add(new OutputEdge(intersect[1], inLeft));
                }
            }

            // We can now 'see' inLeft and inRight from the outer edges on the outer hull, so add these edges.
            solution.add(new OutputEdge(intersect[0], inLeft));
            solution.add(new OutputEdge(intersect[3], inRight));

            // Remove the inner part between inLeft and inRight from remaining while adding their edges to the solution
            InputVertex inRemove = inRight;
            while (!inRemove.equals(inLeft)) {
                remaining.remove(inRemove);
                solution.add(new OutputEdge(inRemove, inRemove.getNext()));
                inRemove = inRemove.getNext();
            }
            remaining.remove(inLeft);

            // Remove the part between inLeft and inRight from the inner hull
            innerVertex = inRight.getPrev();
            if (innerVertex.equals(inLeft)) {
                // From inRight going ccw brings us to inLeft, so we are removing the entire hull.
                if (remaining.isEmpty()) {
                    // There are no other points after this hull, done
                    break;
                } else {
                    // There are more points inside the just-removed hull, find a new point in there
                    innerVertex = rightMost(remaining);
                }
            } else {
                // The case where at least one node of the inner hull is not removed (namely innerVertex)

                // Detach this segment from the inner hull
                inRight.getPrev().setNext(null);
                inLeft.getNext().setPrev(null);

                // Add this segment to the outer hull
                intersect[0].setPrev(inLeft);
                inLeft.setNext(intersect[0]);
                intersect[3].setNext(inRight);
                inRight.setPrev(intersect[3]);
                outerVertex = intersect[3];
            }

            // Finish/fill the inner hull again
            finishHull(innerVertex, remaining);
        }

        visualize(remaining, null, null, null, null);

        Logger.write("Writing output to file");
        saveToFile(solution, outFile, instanceName);
    }

    private void visualize(Set<InputVertex> remaining, InputVertex innerVertex, InputVertex outerVertex, InputVertex inLeft, InputVertex inRight) {
        // Draw the outer hull, inner hull, selected edge of inner hull
        if (visualize) {
            // Set to all points
            vis.setData(List.of(allVertices));

            // Different color on remaining points
            vis.addData(List.of(remaining));

            // Do not draw all those auto-implied edges, only solution edges
            Set<Edge> solEdges = new HashSet<>();
            for (OutputEdge e : solution) {
                solEdges.add(new Edge(e.getV1(), e.getV2()));
            }
            vis.setEdges(List.of(solEdges));

            if (innerVertex != null && outerVertex != null) {
                // Color the inner and outer hull differently
                List<InputVertex> inHullVertices = printHull(innerVertex, false);
                List<InputVertex> outHullVertices = printHull(outerVertex, false);
                vis.addData(List.of(inHullVertices));
                vis.addData(List.of(outHullVertices));

                // And the edges of the inner hull in different color
                Set<Edge> inHullEdges = new HashSet<>();
                for (int i = 0; i < inHullVertices.size(); i++) {
                    inHullEdges.add(new Edge(
                            inHullVertices.get(i),
                            inHullVertices.get((i + 1) % inHullVertices.size())
                    ));
                }
                vis.addEdge(inHullEdges);
            }

            // Color inLeft and inRight differently
            if (inLeft != null && inRight != null) {
                HashSet<InputVertex> leftRight = new HashSet<>();
                leftRight.add(inLeft);
                leftRight.add(inRight);
                vis.addData(List.of(leftRight));

                // Separate color for the selected edge of inner hull
                Set<Edge> selectedEdge = new HashSet<>();
                selectedEdge.add(new Edge(inLeft, inRight));
                vis.addEdge(selectedEdge);
            }

            // Draw
            vis.redraw();
        }
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

        if (hull == null) {
            throw new IllegalArgumentException("Hull is null");
        }
        if (l1 == null || l2 == null) {
            throw new IllegalArgumentException("An input vertex is null");
        }

        System.out.println("Intersecting ["+ l1 + ", " + l2 + "] with hull:");
        printHull(hull);

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

        InputVertex[] out = new InputVertex[]{e1, e2, e3, e4};
        for (InputVertex e : out) {
            if (e == null) {
                throw new IllegalStateException("Did not find a proper intersection");
            }
        }
        return out;
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
        if (!data.iterator().hasNext()) {
            throw new IllegalArgumentException("Cannot get right most vertex of empty set");
        }
        InputVertex maxX = data.iterator().next();
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
            // TODO Make it a line side (LR) check
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
     * Angle of a point in between two lines [0, 2*PI]
     * Taking line v1 -> v2 -> v3, the given angle is the angle the right side of this line
     *
     * @param v1
     * @param v2
     * @param v3
     * @return
     */
    private static double angle(InputVertex v1, InputVertex v2, InputVertex v3) {
        return angle(v1.sub(v2), v3.sub(v2));
//        return Math.PI - new Edge(v2, v1).angle(v3.getV());
        // TODO Use Kaj's angle. Currently breaks the code if used like above
    }

    /**
     * Angle from first point to 2nd point [0, 2*PI]
     * @param v1
     * @param v2
     * @return
     */
    private static double angle(InputVertex v1, InputVertex v2) {
        return (angle(v2) - angle(v1) + 2 * Math.PI) % (2 * Math.PI);
    }

    /**
     * Angle w.r.t. origin of a vertex interpreted as a line from source to the point [-PI, PI]
     * @param v
     * @return
     */
    private static double angle(InputVertex v) {
        return Math.atan2(v.getY(), v.getX());
    }


}
