package convex_layers.evaluate;

import convex_layers.*;
import convex_layers.hull.ConvexHull;

import java.util.Collection;

public class ScoreCalculator {

    /**
     * Calculate the score of a solution to a problem
     * The score is defined as score = f/(2n-c-2), where:
     * - f = num convex faces of convex partitioning of nodes
     * - n = num of nodes of problem
     * - c = num of nodes on the convex hull of problem
     *
     * @param problem
     * @param sol
     * @return
     */
    public static double calculateScore(Problem2 problem, Collection<OutputEdge> sol) {
        Collection<BaseInputVertex> nodes = problem.getVertices();

        int numNodes = nodes.size();
        int numEdges = sol.size();
        // F + V - E = 2
        int numFaces = 2 + numEdges - numNodes - 1; // Exclude the outer face

        int numNodesOnHull = ConvexHull.createConvexHull(nodes).size();

        int divider = 2 * numNodes - numNodesOnHull - 2;
        double score = numFaces / (double) divider;

        return score;
    }

    /**
     * Calculate a lower bound on the score of a problem
     * Namely the score resulting from the fact that each node on the hull has to have at least degree 2,
     * and any node inside the hull at least degree 3 (as we assume no colinear points)
     * @param problem
     * @return
     */
    public static double calculateLowerBoundScore(Problem2 problem) {
        Collection<BaseInputVertex> nodes = problem.getVertices();

        int numNodes = nodes.size();
        int numNodesOnHull = ConvexHull.createConvexHull(nodes).size();
        int numNodesInsideHull = numNodes - numNodesOnHull;

        int minNumEdges = (numNodesOnHull * 2 + numNodesInsideHull * 3) / 2;
        int minNumFaces = 2 + minNumEdges - numNodes - 1; // Exclude the outer face

        int divider = 2 * numNodes - numNodesOnHull - 2;
        double score = minNumFaces / (double) divider;

        return score;
    }

}
