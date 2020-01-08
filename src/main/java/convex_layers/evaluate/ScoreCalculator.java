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

        int numNodes = problem.getVertices().size();
        int numEdges = sol.size();
        // F + V - E = 2
        int numFaces = 2 + numEdges - numNodes - 1; // Exlude the outer face

        int numNodesOnHull = ConvexHull.createConvexHull(nodes).size();

        int dividor = 2 * numNodes - numNodesOnHull - 2;
        double score = numFaces / (double) dividor;

        return score;
    }
}
