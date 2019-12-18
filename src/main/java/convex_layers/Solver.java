package convex_layers;

import convex_layers.data.Range2DSearch;
import convex_layers.visual.Visualizer;

import java.util.Collection;

/**
 * Interface to be implemented by the problem solvers.
 */
public interface Solver {
    
    /**
     * Solves the given problem using the given with the given visualizer.
     * 
     * @param p The problem to solve.
     * @param vis     The visualizer used to visualize the steps of the algorithm.
     * 
     * @return The solution of the given problem.
     */
    Collection<OutputEdge> solve(Problem2 p, Visualizer vis);
    
    
}
