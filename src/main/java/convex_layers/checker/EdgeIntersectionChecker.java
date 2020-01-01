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

public class EdgeIntersectionChecker
        implements Checker {

    /**
     * {@inheritDoc}
     * 
     * @apiNote Runs in {@code O(n}<sup>{@code 2}</sup>{@code )}.
     */
    @Override
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        CheckerError err = new CheckerError();
        for (OutputEdge oe1 : sol) {
            for (OutputEdge oe2 : sol) {
                if (oe1.hasEqualEndpointWith(oe2)) continue;
                Edge e1 = new Edge(oe1.getV1().getV(), oe1.getV2().getV());
                Edge e2 = new Edge(oe2.getV1().getV(), oe2.getV2().getV());
                if (e1.intersects(e2)) {
                    err.addEdge(oe1);
                    err.addEdge(oe2);
                }
            }
        }
        return err;
    }
    
    public static void main(String[] args) {
        Logger.setDefaultLogger(new StreamLogger(System.out));
        EdgeIntersectionChecker eic = new EdgeIntersectionChecker();
        List<BaseInputVertex> biv = Arrays.asList(
                new BaseInputVertex(0, 0, 0),
                new BaseInputVertex(1, 1, 0),
                new BaseInputVertex(2, 1, 1),
                new BaseInputVertex(3, 0, 1),
                new BaseInputVertex(4, -1, 0.5),
                new BaseInputVertex(5, 2, 0.5)
        );
        Problem2 p = new Problem2("name", biv);
        Collection<OutputEdge> sol = List.of(
                new OutputEdge(biv.get(0), biv.get(1)),
                new OutputEdge(biv.get(1), biv.get(2)),
                new OutputEdge(biv.get(2), biv.get(3)),
                new OutputEdge(biv.get(3), biv.get(0)),
                new OutputEdge(biv.get(4), biv.get(5))
        );
        Visual vis = new Visualizer();
        vis.addEdge(Visual.toEdge(sol));
        vis.addPoint(Visual.toVec(biv));
        vis.addLabel(Visual.toLabel(biv));
        vis.redraw();
        
        CheckerError err = eic.check(p, sol);
        err.draw(vis);
        vis.redraw();
        Logger.write(err);
    }
    
    
}
