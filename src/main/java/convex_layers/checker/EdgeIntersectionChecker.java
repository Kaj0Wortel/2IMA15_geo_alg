package convex_layers.checker;

import convex_layers.*;
import convex_layers.math.Edge;
import convex_layers.visual.Visual;
import convex_layers.visual.Visualizer;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Checker implementation which checks whether no edges intersect. <br>
 * Runs in {@code O(|E|}<sup>{@code 2}</sup>{@code )}.
 * <table border="1">
 *     <tr><th>Case</th><th>Runtime</th></tr>
 *     <tr><td align="right">Worst case</td>{@code |E|}<sup>{@code 2}</sup></tr>
 *     <tr><td align="right">Average case</td>{@code |E|}<sup>{@code 2}</sup></tr>
 * </table>
 */
public class EdgeIntersectionChecker
        implements Checker {
    
    @Override
    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        CheckerError err = new CheckerError();
        OutputEdge[] arr = sol.toArray(new OutputEdge[sol.size()]);
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i].hasEqualEndpointWith(arr[j])) continue;
                Edge e1 = new Edge(arr[i].getV1().getV(), arr[i].getV2().getV());
                Edge e2 = new Edge(arr[j].getV1().getV(), arr[j].getV2().getV());
                if (e1.intersects(e2)) {
                    err.addEdge(arr[i]);
                    err.addEdge(arr[j]);
                }
            }
        }
        return err;
    }
    
    
}
