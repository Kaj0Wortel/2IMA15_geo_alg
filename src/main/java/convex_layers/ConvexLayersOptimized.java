package convex_layers;

import convex_layers.checker.*;
import convex_layers.data.IgnoreRangeSearch;
import convex_layers.data.Node2D;
import convex_layers.data.Range2DSearch;
import convex_layers.data.prior_tree.PriorTreeSearch;
import convex_layers.hull.ConvexHull;
import convex_layers.hull.NearIntersection;
import convex_layers.hull.VectorYEdge;
import convex_layers.hull.VectorYNode;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import convex_layers.visual.NullVisualizer;
import convex_layers.visual.Visual;

import convex_layers.visual.VisualRender;
import convex_layers.visual.Visualizer;
import lombok.*;

import tools.MultiTool;
import tools.Var;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Optimized version of the convex partition problem.
 * 
 * @see ConvexLayers
 */
@RequiredArgsConstructor
public class ConvexLayersOptimized
            implements Solver {
    
    /* ----------------------------------------------------------------------
     * Constants.
     * ----------------------------------------------------------------------
     */
    /** Folder storing the user generated data. */
    public static final String GEN_DATA = System.getProperty("user.dir") + Var.FS + "gen_data" + Var.FS;
    
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The class of the 2D search structure to use. */
    @SuppressWarnings("rawtypes")
    private final Class<? extends Range2DSearch> searchClass;
    
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    @Getter
    @RequiredArgsConstructor
    private static class MinMax {
        private final boolean unboundedLeft;
        private final boolean unboundedBottom;
        private Double minX;
        private Double maxX;
        private Double minY;
        private Double maxY;
        
        
        /**
         * Updates the minimum and maximum values used in the range to search for.
         * 
         * @param v The vector to apply.
         */
        private void apply(Vector v) {
            Logger.write("Applying: " + v);
            if (minX == null) {
                minX = maxX = v.x();
                minY = maxY = v.y();
                
            } else {
                if (v.x() < minX) minX = v.x();
                if (v.x() > maxX) maxX = v.x();
                if (v.y() < minY) minY = v.y();
                if (v.y() > maxY) maxY = v.y();
            }
        }
        
        @Override
        public String toString() {
            return getClass().getCanonicalName() + "[" + Var.LS +
                    "    x: [" + minX + "," + maxX + "]," + Var.LS +
                    "    y: [" + minY + "," + maxY + "]" + Var.LS +
                    "]";
        }
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * @return A new 2D search structure from initial class.
     */
    @SuppressWarnings("unchecked")
    public Range2DSearch<BaseInputVertex> create2DSearch() {
        try {
            return searchClass.getDeclaredConstructor().newInstance();
            
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            Logger.write(e);
            System.exit(-1);
            return null;
        }
    }
    
    /**
     * Searches in the given 2D range search structure using the given ranges in {@link MinMax}.
     * 
     * @param search The 2D range search structure.
     * @param minMax The ranges to search for.
     * 
     * @param <T>    The type of nodes.
     * 
     * @return The nodes in the specified range.
     */
    private static <T extends Node2D<T>> Collection<T> search(Range2DSearch<T> search, MinMax minMax) {
        Logger.write(minMax);
        return search.getRange(minMax.minX, minMax.maxX, minMax.minY, minMax.maxY,
                minMax.unboundedLeft, minMax.unboundedBottom);
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
    private void resetVis(Visual vis, Problem2 p, ConvexHull<BaseInputVertex> in, ConvexHull<BaseInputVertex> out,
                          Collection<OutputEdge> sol) {
        vis.clear();
        vis.setData(List.of(p.getVertices()));
        vis.setEdges(List.of(Visual.toEdge(sol)));
        vis.addData(List.of(out.getLeftInput(), out.getRightInput(), in.getLeftInput(), in.getRightInput()));
        //vis.addData(List.of(out, in));
    }
    

    /**
     * Adds the given hull to the solution collection.
     * 
     * @param sol  The solution collection.
     * @param hull The hull to add.
     */
    private void addHullToSol(Collection<OutputEdge> sol, ConvexHull<BaseInputVertex> hull) {
        BaseInputVertex prev = null;
        BaseInputVertex first = null;
        for (BaseInputVertex iv : hull) {
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
     * @param first     Whether to process the first or second point of the intersection.
     * @param vis       The visualizer.
     */
    private void fixOuterHull(ConvexHull<BaseInputVertex> innerHull, ConvexHull<BaseInputVertex> outerHull,
                              Collection<OutputEdge> sol, NearIntersection<BaseInputVertex> ni, MinMax minMax,
                              boolean first, Visual vis) {
        if (innerHull.isEmpty()) return;
        VectorYNode<BaseInputVertex> cur = (first ? ni.getInnerVec1() : ni.getInnerVec2());
        VectorYNode<BaseInputVertex> prev = null;
        VectorYNode<BaseInputVertex> outerNode = (first ? ni.getN1() : ni.getN4());
        VectorYNode<BaseInputVertex> innerNode = (first ? ni.getN2() : ni.getN3());
        //minMax.apply(cur.getVec());
        
        Edge e;
        do {
            if (prev != null) {
                sol.add(new OutputEdge(prev.getIv(), cur.getIv()));
            }
            prev = cur;
            cur = (ni.isClockwise() != first
                    ? innerHull.clockwise(cur)
                    : innerHull.counterClockwise(cur)
            );
            e = (ni.isClockwise() != first
                    ? new Edge(prev.getVec(), outerNode.getVec())
                    : new Edge(outerNode.getVec(), prev.getVec())
            );
            
            innerHull.remove(prev.getIv());
            outerHull.add(prev.getIv());
            sol.add(new OutputEdge(prev.getIv(), innerNode.getIv()));
            minMax.apply(cur.getVec());
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
     * @param search    The 2D range search structure to use.
     * @param minMax    The data needed for the search.
     * @param vis       The visualizer used to display the progress.
     */
    private void fixInnerHull(ConvexHull<BaseInputVertex> innerHull, Range2DSearch<BaseInputVertex> search,
                              MinMax minMax, Visual vis) {
        Collection<BaseInputVertex> toConsider = search(search, minMax);
        Collection<BaseInputVertex> toRemove = new HashSet<>();
        for (BaseInputVertex iv : toConsider) {
            toRemove.add(iv);
            List<BaseInputVertex> del = innerHull.addAndUpdate(iv);
            toRemove.removeAll(del);
            if (del.size() != 1 || del.get(0) != iv) vis.redraw();
        }
        search.removeAll(toRemove);
    }
    
    /**
     * Solves the given problem and outputs it to the output file.
     */
    public synchronized Collection<OutputEdge> solve(Problem2 p, Visual vis) {
        // Create the solution set.
        Set<OutputEdge> sol = new HashSet<>();
        
        // Initialize the inner and outer hulls, and the search structure for the remaining vertices..
        ConvexHull<BaseInputVertex> outerHull;
        ConvexHull<BaseInputVertex> innerHull;
        Range2DSearch<BaseInputVertex> search = create2DSearch();
        {
            Collection<BaseInputVertex> remaining = new HashSet<>(p.getVertices()); // TODO: move inwards.
            outerHull = ConvexHull.createConvexHull(remaining);
            remaining.removeAll(outerHull);
            innerHull = ConvexHull.createConvexHull(remaining);
            remaining.removeAll(innerHull);
            search.init(remaining);
        }
        
        // Reset the visual.
        resetVis(vis, p, innerHull, outerHull, sol);
        
        // Add the outer hull to the solution set.
        addHullToSol(sol, outerHull);
        vis.redraw();
        
        int i = 0; // TODO: remove.
        int[] coords = new int[] {
                0, 1,
                6, 7,
                5, 6,
                5, 6,
                5, 6,
                0, 1,
                7, 8
        };

        // BEGIN ALGORITHM LOGIC
        while (!innerHull.isEmpty()) {
            if (innerHull.size() == 1) {
                BaseInputVertex iv = innerHull.get(0);
                sol.addAll(outerHull.getInnerPointConnections(iv));
                vis.redraw();
                break;
            }
            
            // Select random edge and compute intersection with outer hull.
//            int x = coords[i++];
//            int y = coords[i++];
//            Logger.write("x: " + x + ", y: " + y);
//            VectorYEdge<BaseInputVertex> vye = new VectorYEdge<>(innerHull.getNode(x), innerHull.getNode(y));
            
            VectorYEdge<BaseInputVertex> vye = innerHull.getRandomEdge(); // TODO: place back.
            NearIntersection<BaseInputVertex> ni;
            { // Find the intersections with the outer hull
                boolean hullOnLeftSide = innerHull.counterClockwise(vye.getIv1()).equals(vye.getIv2());
                ni = outerHull.getPointsNearLine(vye, hullOnLeftSide);
            }
            { // Add intersections to the visual and reset it afterwards.
                List<BaseInputVertex> intersect = List.of(
                        ni.getN1().getIv(),
                        ni.getN2().getIv(),
                        ni.getN3().getIv(),
                        ni.getN4().getIv(),
                        ni.getN1().getIv()
                );
                vis.addData(List.of(intersect));
                vis.addEdge(List.of(vye.toEdge()));
                vis.redraw();
                resetVis(vis, p, innerHull, outerHull, sol);
            }

            // Generate the bounding box to search for during the inner hull repair phase.
            boolean unboundedLeft;
            boolean unboundedBottom;
            {
                Vector v = ni.getInnerVec1().getVec();
                Edge e = innerHull.getBottomTopEdge();
                unboundedLeft = e.relOri(v) <= 0;
                e = new Edge(innerHull.getMinX().getV(), innerHull.getMaxX().getV());
                unboundedBottom = e.relOri(v) >= 0;
            }
            MinMax minMax = new MinMax(unboundedLeft, unboundedBottom);
            
            // For the inner part, add the output edges, add the two vertices from the inner hull
            // to the outer hull, and remove all unneeded nodes from the outer hull.
//            VectorYNode<BaseInputVertex> first = ni.getInnerVec1();
//            VectorYNode<BaseInputVertex> second = ni.getInnerVec2();
            sol.add(new OutputEdge(ni.getInnerVec1().getIv(), ni.getInnerVec2().getIv()));
            vis.redraw();
            
            ni.removeMiddleNodes(outerHull, vis);
            vis.redraw();
            
            // Fix the outer hull.
            minMax.apply(ni.getInnerVec1().getVec());
            minMax.apply(ni.getInnerVec2().getVec());
            fixOuterHull(innerHull, outerHull, sol, ni, minMax, true, vis);
            fixOuterHull(innerHull, outerHull, sol, ni, minMax, false, vis);
            
            // Fix the inner hull.
            if (!search.isEmpty()) {
                fixInnerHull(innerHull, search, minMax, vis); // TODO
            }
            // Reset Visual.
            vis.redraw();
            resetVis(vis, p, innerHull, outerHull, sol);
        }
        // END ALGORITHM LOGIC
        
        vis.clear();
        vis.addData(List.of(p.getVertices()));
        vis.setEdges(List.of(Visual.toEdge(sol)));
        vis.redraw();
        return sol;
    }
    
    
    /**
     * The main function used to initialize the program.
     *
     * @param args The runtime arguments.
     */
    public static void main(String[] args) {
//        MultiTool.initLogger(Var.LOG_FILE);
        Logger.setDefaultLogger(new StreamLogger(System.out));

        String folder = "challenge_1";
        String type = "uniform";
//        String name = "uniform-0000015-1";
//        String name = "uniform-0000040-1";
//        String name = "uniform-0000060-1";
        String name = "uniform-0001000-1";
        String path = "data" + Var.FS + folder + Var.FS + type + Var.FS + name;
        
        File inFile = new File(path + ".instance.json");
//        File inFile = new File(GEN_DATA + "0000_0017.json");
        File outFile = new File(path + ".solution.json");
        
        Visual vis = new Visualizer();
//        Visual vis = new NullVisualizer();
        Problem2 problem = ProblemIO.readProblem(inFile);
        Solver solver = new ConvexLayersOptimized(IgnoreRangeSearch.class);
        Checker checker = new MultiChecker(new EdgeIntersectionChecker(), new ConvexChecker());
        
        Collection<OutputEdge> sol = solver.solve(problem, vis);
        
        // TODO: tmp
//        Visual v = new Visualizer();
//        v.setEdges(List.of(Visual.toEdge(sol)));
//        v.setPoints(List.of(Visual.toVec(problem.getVertices())));
//        v.setLabels(List.of(Visual.toLabel(problem.getVertices())));
//        v.redraw();
        
        //sol.remove(sol.iterator().next());
        CheckerError err = checker.check(problem, sol);
        Logger.write(err);
        Visual errorVis = new VisualRender();
        errorVis.addPoint(Visual.toVec(problem.getVertices()));
        errorVis.addLabel(Visual.toLabel(problem.getVertices()));
        errorVis.addEdge(Visual.toEdge(sol));
        err.draw(errorVis);
        
        //ProblemIO.saveSolution(outFile, sol, problem); // TODO: place back to save solution.
    }
    
    
}
