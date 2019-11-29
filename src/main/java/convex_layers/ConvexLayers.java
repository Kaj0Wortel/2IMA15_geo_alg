package convex_layers;

import tools.Pair;
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
    /** The input file */
    private File input;
    /** The output file. */
    private File output;
    /** Set that keeps track of all variables that still need to be checked. */
    private Set<InputVertex> remaining = new HashSet<InputVertex>();
    /** Set containing the edges of the resulting convex partitioning. */
    private Set<OutputEdge> sol = new HashSet<OutputEdge>();
    
    /** Linked list of the inner convex hull. */
    //private DoublyLinkedList<Vertex> in;
    private InputVertex in;
    /** Linked list of the outer convex hull. */
    //private DoublyLinkedList<Vertex> out;
    private InputVertex out;

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
//        if (args == null || args.length < 2) {
//            Logger.write("Expected 2 arguments.", Logger.Type.ERROR);
//            System.exit(1);
//        }
        
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
    public static void finishHull(InputVertex v, Set<InputVertex> set) {
        if (v.getPrev() == null) {
            v.setPrev(new InputVertex(-1, v.getX() + 10, v.getY(), false, null, null));
        }
        do {
            v.setHulled(true);
            InputVertex next = nextCH(v.getPrev() , v, set);
//            System.out.println("Next from " + v.id + " is " + next.id);
            v.setNext(next);
            next.setPrev(v);
            v = next;
        } while (!v.isHulled());
    }

    /**
     * Print a hull to the console for debugging purposes
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
     * Solve the problem of a given input file
     */
    public void solve(File inFile, File outFile) {
        Pair<Set<InputVertex>, String> input = readInput(inFile);
        remaining = input.getFirst();
        String instanceName = input.getSecond();

        // Build complete outer convex hull
        System.err.println("Building initial outer hull");
        out = rightMost(remaining);
        finishHull(out, remaining);
        printHull(out);


        // Add edges of outer CH to solution and delete these vertices from remaining
        System.err.println("Adding outer hull to solution");
        InputVertex prev = out;
        do {
            InputVertex next = prev.getNext();
            sol.add(new OutputEdge(prev, next));
            remaining.remove(prev);
            prev = next;
        } while (!prev.equals(out));

        boolean done = false;
        // Also check case where outer convex hull contains all points of problem, else build inner hull
        if (!remaining.isEmpty()) {
            // Build complete inner convex hull
            System.err.println("Building initial inner hull");
            in = rightMost(remaining);
            finishHull(in, remaining);
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
            if (remaining.isEmpty()) {
                System.err.println("Nothing left to do!");
                done = true;
                break;
            }
            // Case 1 dot left
            if (remaining.size() == 1) {
                System.err.println("TODO has only 1 left!");
                // TODO Add edge to a random one of outer,
                //  check intersect,
                //  add two more
                // actually this should work fine
//                done = true;
//                break;
            }

            InputVertex inRight = in;
            InputVertex inLeft = in.getNext();
            InputVertex[] intersect = hullLineIntersect(out, inRight, inLeft);
            System.err.println(intersect[0].getId() + ", " + intersect[1].getId() + ", " + intersect[2].getId() + ", " + intersect[3].getId());
            sol.add(new OutputEdge(intersect[2], inRight));
            sol.add(new OutputEdge(intersect[1], inLeft));

            if (!inRight.getPrev().equals(inLeft)) {
                while (angle(inRight.getNext(), inRight, intersect[3]) > angle(inRight.getNext(), inRight, inRight.getPrev())) {
                    System.err.println("Walk right");
                    inRight = inRight.getPrev();
                    sol.add(new OutputEdge(intersect[2], inRight));
                }
                while (angle(intersect[0], inLeft, inLeft.getPrev()) > angle(inLeft.getNext(), inLeft, inLeft.getPrev())) {
                    System.err.println("Walk left");
                    inLeft = inLeft.getNext();
                    sol.add(new OutputEdge(intersect[1], inLeft));
                }
            }

//            if (true) return;

            sol.add(new OutputEdge(intersect[0], inLeft));
            sol.add(new OutputEdge(intersect[3], inRight));

            // Remove the inner part from todo while adding their edges to sol
            InputVertex inRemove = inRight;
            while (!inRemove.equals(inLeft)) {
                remaining.remove(inRemove);
                if (!inRemove.equals(inLeft)) {
                    sol.add(new OutputEdge(inRemove, inRemove.getNext()));
                }
                inRemove = inRemove.getNext();
            }
            remaining.remove(inLeft);

            // Update the outer and inner, if still not done
            in = inRight.getPrev();
            if (in.equals(inLeft)) {
                done = true;
            } else {
                inRight.getPrev().setNext(null);
                inLeft.getNext().setPrev(null);

                intersect[0].setPrev(inLeft);
                inLeft.setNext(intersect[0]);
                intersect[3].setNext(inRight);
                inRight.setPrev(intersect[3]);
                out = intersect[3];
            }

            if (!done) {
                finishHull(in, remaining);
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
    private Pair<Set<InputVertex>, String> readInput(File file) {
        System.out.println("Reading " + file.getAbsolutePath());
        JSONObject json = null;
        try {
            json = new JSONObject(new JSONTokener(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Set<InputVertex> set = new HashSet<>();
        String instanceName = json.getString("name");
        JSONArray points = json.getJSONArray("points");
        for (int i = 0; i < points.length(); i++) {
            JSONObject point = points.getJSONObject(i);
            int id = point.getInt("i");
            double x = point.getDouble("x");
            double y = point.getDouble("y");
            System.out.println("id: " + id + ", x: " + x + ", y : " + y);
            set.add(new InputVertex(id, x, y, false, null, null));
        }

        return new Pair<>(set, instanceName);
    }

    /**
     * Output a solution to a file
     */
    private static void saveToFile(Set<OutputEdge> sol, File file, String instanceName) {
        System.err.println("Saving solution of " + sol.size() + " edges to " + file.getAbsolutePath());
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
    private static void printSolution(Set<OutputEdge> sol) {
        for (OutputEdge e : sol) {
            System.out.println(e.getV1().getId() + " " + e.getV2().getId());
        }
    }

    /**
     * Whether a set of points is convex
     * @param set
     * @return
     */
    private static boolean convex(Set<InputVertex> set) {
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
    private static InputVertex[] hullLineIntersect(InputVertex hull, InputVertex l1, InputVertex l2) {
        System.err.println("Intersecting hull " + hull.getId() + " with line " + l1.getId() + ", " + l2.getId());

        InputVertex e1 = null;
        InputVertex e2 = null;
        InputVertex e3 = null;
        InputVertex e4 = null;
        printHull(hull);
        int testCounter = 0;
        InputVertex v = hull;
        do {
            InputVertex next = v.getNext();
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

        return new InputVertex[]{e1, e2, e3, e4};
    }

    /**
     * Whether line segment [e1, e2] intersects the line through points l1 and l2
     * @param e1
     * @param e2
     * @param l1
     * @param l2
     * @return
     */
    private static boolean intersects(InputVertex e1, InputVertex e2, InputVertex l1, InputVertex l2) {
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
    private static boolean leftOfLine(InputVertex v, InputVertex l1, InputVertex l2) {
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
    private InputVertex rightMost(Set<InputVertex> set) {
        InputVertex maxX = set.iterator().next();
        for (InputVertex v : set) {
            if (v.getX() > maxX.getX()) {
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
    public static InputVertex nextCH(InputVertex prev, InputVertex v, Set<InputVertex> set) {
//        System.out.println("Finding extCH of " + v.id + " from " + prev.id);
        double maxAngle = -Double.MAX_VALUE;
        InputVertex bestVertex = v;
        for (InputVertex candidate : set) {
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
    private static double angle(InputVertex v1, InputVertex v2, InputVertex v3) {
        return angle(v1.sub(v2), v3.sub(v2));
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
