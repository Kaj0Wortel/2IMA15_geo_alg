package convex_layers.checker;

import convex_layers.OutputEdge;
import convex_layers.Problem2;

import java.util.Collection;

/**
 * Interface for checking a solution.
 */
public interface Checker {
    
    /**
     * Checks whether the solution to the problem is valid.
     * 
     * @param problem The problem to check.
     * @param sol     The solution of the problem.
     * 
     * @return {@code true} if the 
     */
    CheckerError check(Problem2 problem, Collection<OutputEdge> sol);
    
    
}
