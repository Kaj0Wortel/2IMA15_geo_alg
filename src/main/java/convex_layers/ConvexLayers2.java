package convex_layers;

import convex_layers.hull.ConvexHull;
import convex_layers.hull.NearIntersection;
import convex_layers.hull.VectorYNode;
import convex_layers.math.Edge;
import convex_layers.visual.Visualizer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import tools.Var;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.io.*;
import java.util.*;

/**
 * Optimized version of the convex partition problem.
 * 
 * @see ConvexLayers
 */
public class ConvexLayers2 {
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Read input file
     *
     * @param file The input file
     * @return The problem statement in its own class
     */
    private static Problem2 readInput(File file) {
        Logger.write("Reading " + file.getAbsolutePath());
        JSONObject json = null;
        try {
            json = new JSONObject(new JSONTokener(new FileInputStream(file)));
            
        } catch (FileNotFoundException e) {
            Logger.write(e);
            System.exit(-1);
        }
        
        Collection<InputVertex> vertices = new ArrayList<>();
        String name = json.getString("name");
        JSONArray points = json.getJSONArray("points");
        for (int i = 0; i < points.length(); i++) {
            JSONObject point = points.getJSONObject(i);
            int id = point.getInt("i");
            double x = point.getDouble("x");
            double y = point.getDouble("y");
            Logger.write("id: " + id + ", x: " + x + ", y : " + y);
            vertices.add(new InputVertex(id, x, y, false, null, null));
        }
        
        return new Problem2(name, vertices);
    }
    
    /**
     * Output a solution to a file
     */
    private static void saveToFile(Collection<OutputEdge> sol, File file, String instanceName) {
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
            
        } catch (IOException e) {
            Logger.write(e);
            System.exit(-1);
        }
    }

    /**
     * Resets the visualizer to the default settings.
     * 
     * @param vis  The visualizer to reset.
     * @param p    The problem to get the data from.
     * @param in   The inner convex hull.
     * @param out  The outer convex hull.
     * @param sol  The solution set.
     */
    private void resetVis(Visualizer vis, Problem2 p, ConvexHull in, ConvexHull out, Collection<OutputEdge> sol) {
        vis.clear();
        vis.setData(List.of(p.getVertices()));
        vis.setEdges(List.of(Visualizer.toEdge(sol)));
        //vis.addData(List.of(out.getLeftInput(), out.getRightInput(), in.getLeftInput(), in.getRightInput()));
        vis.addData(List.of(out, in));
    }
    

    /**
     * Adds the given hull to the solution collection.
     * 
     * @param sol  The solution collection.
     * @param hull The hull to add.
     */
    private void addHullToSol(Collection<OutputEdge> sol, ConvexHull hull) {
        InputVertex prev = null;
        InputVertex first = null;
        for (InputVertex iv : hull) {
            if (prev == null) {
                first = prev = iv;
                continue;
            }

            sol.add(new OutputEdge(prev, iv));
            prev = iv;
        }
        if (first != null && first != prev) {
            sol.add(new OutputEdge(prev, first));
        }
    }
    
    /**
     * Fixes the outer hull around the first or second point of the intersection.
     * 
     * @param innerHull The current inner hull.
     * @param outerHull The current outer hull.
     * @param sol       The current solution collection.
     * @param ni        The intersection to process.
     * @param begin     The starting edge of the intersection.
     * @param first     Whether to process the first or second point of the intersection.
     * @param vis       The visualizer.
     */
    private void fixOuterHull(ConvexHull innerHull, ConvexHull outerHull, Collection<OutputEdge> sol,
                             NearIntersection ni, VectorYNode begin, boolean first, Visualizer vis) {
        if (innerHull.isEmpty()) return;
        NearIntersection.Orientation ori = ni.getOri();
        VectorYNode cur = begin;
        VectorYNode prev = null;
        boolean dir = (ori == NearIntersection.Orientation.BOTTOM || ori == NearIntersection.Orientation.LEFT);
        boolean clockwise = (dir == first);
        VectorYNode outerNode = (first
                ? ni.getOuterNode1()
                : ni.getOuterNode2()
        );
        VectorYNode innerNode = (first
                ? ni.getInnerNode1()
                : ni.getInnerNode2()
        );
        
        Edge e;
        do {
            if (prev != null) {
                sol.add(new OutputEdge(prev.getIv(), cur.getIv()));
                vis.redraw();
            }
            
            prev = cur;
            cur = (clockwise
                    ? innerHull.clockwise(cur)
                    : innerHull.counterClockwise(cur)
            );
            e = (clockwise
                    ? new Edge(prev.getVec(), outerNode.getVec())
                    : new Edge(outerNode.getVec(), prev.getVec())
            );
            
            innerHull.remove(prev.getIv());
            vis.redraw();
            outerHull.add(prev.getIv());
            vis.redraw();
            sol.add(new OutputEdge(prev.getIv(), innerNode.getIv()));
            vis.redraw();
            
        } while (prev != cur && e.relOri(cur.getVec()) <= 0 && !innerHull.isEmpty());
        sol.add(new OutputEdge(prev.getIv(), outerNode.getIv()));
        vis.redraw();
    }

    /**
     * Repairs the inner hull.
     * TODO Improve algorithm speed.
     *   Current average time : O(n)
     *   Improved average time: O(log(n))
     * 
     * @param innerHull The inner hull.
     * @param remaining The remaining nodes.
     * @param ni        The intersection which was processed.
     */
    private void fixInnerHull(ConvexHull innerHull, Collection<InputVertex> remaining, NearIntersection ni,
                              Visualizer vis) {
        Collection<InputVertex> toRemove = new HashSet<>();
        int i = 0;
        for (InputVertex in : remaining) {
            toRemove.add(in);
            List<InputVertex> del = innerHull.addAndUpdate(in);
            toRemove.removeAll(del);
            if (del.size() != 1 || del.get(0) != in) vis.redraw();
        }
        remaining.removeAll(toRemove);
    }
    
    /**
     * Solves the given problem and outputs it to the output file.
     */
    public synchronized void solve(File source, File target) {
        // Initialize the visualizer.
        Visualizer vis = new Visualizer();
        
        // Read the problem.
        Problem2 p = readInput(source);
        
        // Initialize the solution set and inner/outer hulls.
        Set<OutputEdge> sol = new HashSet<>();
        Collection<InputVertex> remaining = new HashSet<>(p.getVertices()); // TODO: use other data structure.
        
        ConvexHull outerHull = ConvexHull.createConvexHull(remaining);
        remaining.removeAll(outerHull);
        ConvexHull innerHull = ConvexHull.createConvexHull(remaining);
        remaining.removeAll(innerHull);

        // Reset visualizer.
        resetVis(vis, p, innerHull, outerHull, sol);
        
        // Add the outer hull to the solution set.
        addHullToSol(sol, outerHull);
        vis.redraw();
        
        // BEGIN ALGO LOGIC
        int i = 0; // TODO: TMP 
        while (!innerHull.isEmpty()) { // TODO: fix this
            if (innerHull.size() == 1) {
                InputVertex iv = innerHull.get(0);
                sol.addAll(outerHull.getInnerPointConnections(iv));
                vis.redraw();
                break;
            }
            
            // Compute intersection.
            VectorYEdge vye = innerHull.getRandomEdge(); // TODO: place back when done.
//            VectorYEdge vye;
//            // 3/4
//            // -1/1
//            // 1/2
//            int x;
//            int y;
//            if (i == 0) {
//                x = 3;
//                y = 4;
//            } else if (i == 1) {
//                x = innerHull.size() - 1;
//                y = 0;
//            } else {
//                x = 1;
//                y = 2;
//            }
//            i++;
//            vye = new VectorYEdge(innerHull.getNode(x), innerHull.getNode(y));
            //VectorYEdge vye = new VectorYEdge(innerHull.getNode(innerHull.size() - 1), innerHull.getNode(0)); // TODO: DONE
            //VectorYEdge vye = new VectorYEdge(innerHull.getNode(0), innerHull.getNode(1)); // TODO: DONE
            //int index = Math.min(2, innerHull.size() - 1);
            //VectorYEdge vye = new VectorYEdge(innerHull.getNode(index - 1), innerHull.getNode(index)); // TODO: DONE
            Edge e = vye.toEdge();
            boolean hasLeft;
            {
                double ori = 0;
                // The remaining points always lie within the inner hull.
                if (!remaining.isEmpty()) {
                    ori = e.relOri(remaining.iterator().next().getV());
                }
                // Iterate over the hull, and try to find an vertex which lies on either side.
                if (ori == 0) {
                    for (InputVertex iv : innerHull) {
                        ori = e.relOri(iv.getV());
                        if (ori == 0) break;
                    }
                }
                hasLeft = (ori < 0);
            }
            
            NearIntersection ni = outerHull.getPointsNearLine(e, hasLeft);
            NearIntersection.Orientation ori = ni.getOri();
            {
                List<InputVertex> intersect = List.of(
                        ni.getInnerNode1().getIv(),
                        ni.getInnerNode2().getIv(),
                        ni.getOuterNode2().getIv(),
                        ni.getOuterNode1().getIv(),
                        ni.getInnerNode1().getIv()
                );
                vis.addData(List.of(intersect));
                vis.addEdge(List.of(vye.toEdge()));
                vis.redraw();
                resetVis(vis, p, innerHull, outerHull, sol);
            }
            
            // For the inner part, add the output edges, add the two vertices from the inner hull
            // to the outer hull, and remove all unneeded nodes from the outer hull.
            Edge minMaxEdge = outerHull.getBottomTopEdge();
            VectorYNode first = vye.getFirst(ori, minMaxEdge);
            VectorYNode second = vye.getSecond(ori, minMaxEdge);
            sol.add(new OutputEdge(first.getIv(), second.getIv()));
            vis.redraw();
            
            ni.removeMiddleNodes(outerHull);
            vis.redraw();
            
            // Fix the outer hull.
            fixOuterHull(innerHull, outerHull, sol, ni, first, true, vis);
            fixOuterHull(innerHull, outerHull, sol, ni, second, false, vis);
            
            // Fix the inner hull.
            if (!innerHull.isEmpty()) {
                fixInnerHull(innerHull, remaining, ni, vis);
            }
            // Reset visualizer.
            resetVis(vis, p, innerHull, outerHull, sol);
        }
        // END ALGO LOGIC
        
        vis.clear();
        vis.addData(List.of(p.getVertices()));
        vis.setEdges(List.of(Visualizer.toEdge(sol)));
        vis.redraw();
        if (true) return;
        saveToFile(sol, target, p.getName());
    }
    
    /**
     * The main function used to initialize the program.
     *
     * @param args The runtime arguments.
     */
    public static void main(String[] args) {
        //MultiTool.initLogger(Var.LOG_FILE);
        Logger.setDefaultLogger(new StreamLogger(System.out));

        String folder = "challenge_1";
        String type = "uniform";
        String name = "uniform-0000015-1";
        String path = "data/" + folder + Var.FS + type + Var.FS + name;
        File inFile = new File(path + ".instance.json");
        File outFile = new File(path + ".solution.json");
        new ConvexLayers2().solve(inFile, outFile);
    }
    
    
}
