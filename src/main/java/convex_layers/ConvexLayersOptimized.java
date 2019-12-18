package convex_layers;

import convex_layers.data.Range2DSearch;
import convex_layers.data.prior_tree.PriorTreeSearch;
import convex_layers.hull.ConvexHull;
import convex_layers.hull.NearIntersection;
import convex_layers.hull.VectorYEdge;
import convex_layers.hull.VectorYNode;
import convex_layers.math.Edge;
import convex_layers.visual.Visualizer;

import lombok.RequiredArgsConstructor;

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
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The class of the 2D search structure to use. */
    @SuppressWarnings("rawtypes")
    private final Class<? extends Range2DSearch> searchClass;
    
    
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
     * Resets the visualizer to the default settings.
     * 
     * @param vis  The visualizer to reset.
     * @param p    The problem to get the data from.
     * @param in   The inner convex hull.
     * @param out  The outer convex hull.
     * @param sol  The solution set.
     */
    private void resetVis(Visualizer vis, Problem2 p, ConvexHull<BaseInputVertex> in, ConvexHull<BaseInputVertex> out,
                          Collection<OutputEdge> sol) {
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
     * @param begin     The starting edge of the intersection.
     * @param first     Whether to process the first or second point of the intersection.
     * @param vis       The visualizer.
     */
    private void fixOuterHull(ConvexHull<BaseInputVertex> innerHull, ConvexHull<BaseInputVertex> outerHull,
                              Collection<OutputEdge> sol, NearIntersection<BaseInputVertex> ni,
                              VectorYNode<BaseInputVertex> begin,
                              boolean first, Visualizer vis) {
        if (innerHull.isEmpty()) return;
        NearIntersection.Orientation ori = ni.getOri();
        VectorYNode<BaseInputVertex> cur = begin;
        VectorYNode<BaseInputVertex> prev = null;
        boolean dir = (ori == NearIntersection.Orientation.BOTTOM || ori == NearIntersection.Orientation.LEFT);
        boolean clockwise = (dir == first);
        VectorYNode<BaseInputVertex> outerNode = (first
                ? ni.getOuterNode1()
                : ni.getOuterNode2()
        );
        VectorYNode<BaseInputVertex> innerNode = (first
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
    private void fixInnerHull(ConvexHull<BaseInputVertex> innerHull, Collection<BaseInputVertex> remaining,
                              NearIntersection<BaseInputVertex> ni,
                              Visualizer vis) {
        Collection<BaseInputVertex> toRemove = new HashSet<>();
        for (BaseInputVertex in : remaining) {
            toRemove.add(in);
            List<BaseInputVertex> del = innerHull.addAndUpdate(in);
            toRemove.removeAll(del);
            if (del.size() != 1 || del.get(0) != in) vis.redraw();
        }
        remaining.removeAll(toRemove);
    }
    
    /**
     * Solves the given problem and outputs it to the output file.
     */
    //public synchronized void solve(File source, File target) {
    public synchronized Collection<OutputEdge> solve(Problem2 p, Visualizer vis) {
        // Create the solution set.
        Set<OutputEdge> sol = new HashSet<>();

        // Initialize the inner and outer hulls, and the search structure for the remaining vertices..
        ConvexHull<BaseInputVertex> outerHull;
        ConvexHull<BaseInputVertex> innerHull;
        Range2DSearch<BaseInputVertex> search = create2DSearch();
        Collection<BaseInputVertex> remaining = new HashSet<>(p.getVertices()); // TODO: move inwards. v
        {
            outerHull = ConvexHull.createConvexHull(remaining);
            remaining.removeAll(outerHull);
            innerHull = ConvexHull.createConvexHull(remaining);
            remaining.removeAll(innerHull);
            search.init(remaining);
        }
        
        // Reset visualizer.
        resetVis(vis, p, innerHull, outerHull, sol);
        
        // Add the outer hull to the solution set.
        addHullToSol(sol, outerHull);
        vis.redraw();
        
        // BEGIN ALGORITHM LOGIC
        int i = 0; // TODO: remove.
        while (!innerHull.isEmpty()) {
            if (innerHull.size() == 1) {
                BaseInputVertex iv = innerHull.get(0);
                sol.addAll(outerHull.getInnerPointConnections(iv));
                vis.redraw();
                break;
            }
            
            // Compute intersection with outer hull.
            //VectorYEdge<BaseInputVertex> vye = innerHull.getRandomEdge(); // TODO: place back.
            // TODO: fix this case!
            int x;
            int y;
            if (i == 0) {
                x = 4;
                y = 0;
                
            } else if (i == 1) {
                x = 0;
                y = 1;
                 
            } else if (i == 2) {
                x = 1;
                y = 2;
                
            } else {
                x = 0;
                y = 1;
            }
            i++;
            VectorYEdge<BaseInputVertex> vye = new VectorYEdge<>(innerHull.getNode(x), innerHull.getNode(y));
            Edge e = vye.toEdge();
            boolean hasLeft;
            {
                double ori = 0;
                // The remaining points always lie within the inner hull.
                if (!search.isEmpty()) {
                    ori = e.relOri(search.iterator().next().getV());
                }
                // Iterate over the hull, and try to find an vertex which lies on either side.
                if (ori == 0) {
                    for (BaseInputVertex iv : innerHull) {
                        ori = e.relOri(iv.getV());
                        if (ori == 0) break;
                    }
                }
                hasLeft = (ori < 0);
            }
            
            NearIntersection<BaseInputVertex> ni = outerHull.getPointsNearLine(e, hasLeft);
            NearIntersection.Orientation ori = ni.getOri();
            {
                List<BaseInputVertex> intersect = List.of(
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
            VectorYNode<BaseInputVertex> first = vye.getFirst(ori, minMaxEdge);
            VectorYNode<BaseInputVertex> second = vye.getSecond(ori, minMaxEdge);
            sol.add(new OutputEdge(first.getIv(), second.getIv()));
            vis.redraw();
            
            ni.removeMiddleNodes(outerHull);
            vis.redraw();
            
            // Fix the outer hull.
            fixOuterHull(innerHull, outerHull, sol, ni, first, true, vis);
            fixOuterHull(innerHull, outerHull, sol, ni, second, false, vis);
            
            // Fix the inner hull.
            if (!innerHull.isEmpty()) {
                fixInnerHull(innerHull, remaining, ni, vis); // TODO
            }
            // Reset visualizer.
            resetVis(vis, p, innerHull, outerHull, sol);
        }
        // END ALGORITHM LOGIC
        
        vis.clear();
        vis.addData(List.of(p.getVertices()));
        vis.setEdges(List.of(Visualizer.toEdge(sol)));
        vis.redraw();
        return sol;
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
        
        Problem2 problem = ProblemIO.readProblem(inFile);
        Visualizer vis = new Visualizer();
        Solver solver = new ConvexLayersOptimized(PriorTreeSearch.class);
        
        Collection<OutputEdge> sol = solver.solve(problem, vis);
        //ProblemIO.saveSolution(outFile, sol, problem);
    }
    
    
}
