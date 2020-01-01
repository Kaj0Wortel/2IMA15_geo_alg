package convex_layers.checker;

import convex_layers.BaseInputVertex;
import convex_layers.InputVertex;
import convex_layers.OutputEdge;
import convex_layers.Problem2;
import convex_layers.math.Edge;
import convex_layers.math.Vector;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;


public class ConvexChecker
        implements Checker {
    
    @RequiredArgsConstructor
    private static class Node
            implements Iterable<OutputEdge> {
        
        @NonNull
        @Getter
        private final BaseInputVertex biv;
        @NonNull
        private final List<OutputEdge> edges = new ArrayList<>();

        /**
         * Adds the given edge to this node.
         * 
         * @param e The edge to add.
         */
        public void add(OutputEdge e) {
            edges.add(e);
        }
        
        @Override
        public Iterator<OutputEdge> iterator() {
            Edge vertEdge = new Edge(biv.getV(), new Vector(biv.getX(), biv.getY() + 100));
            edges.sort((e1, e2) -> {
                BaseInputVertex other1 = (e1.getV1().equals(biv) ? e1.getV2() : e1.getV2());
                BaseInputVertex other2 = (e2.getV1().equals(biv) ? e2.getV2() : e2.getV2());
                if (other1.equals(other2)) return 0;
                
                double ori1 = vertEdge.relOri(other1.getV());
                double ori2 = vertEdge.relOri(other2.getV());
                if (ori1 < 0 && ori2 > 0) return 1;
                else if (ori2 < 0 && ori1 > 0) return -1;
                else if (ori1 == 0 && ori2 == 0) {
                    double diff = other1.getY() - other2.getY();
                    if (diff < 0) return -1;
                    else if (diff > 0) return 1;
                    else return 0;
                } else {
                    Edge e = new Edge(biv.getV(), other1.getV());
                    return e.relOriRounded(other2.getV()); 
                }
            });
            
            return edges.iterator();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false;
            return ((Node) obj).biv.equals(biv);
        }
        
        @Override
        public int hashCode() {
            return biv.hashCode();
        }
        
        
    }
    
    @Override
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        CheckerError err = new CheckerError();

        Map<BaseInputVertex, Node> nodes = new HashMap<>();
        for (BaseInputVertex biv : problem) {
            nodes.put(biv, new Node(biv));
        }
        
        for (OutputEdge e : sol) {
            nodes.get(e.getV1()).add(e);
            nodes.get(e.getV2()).add(e);
        }
        
        for (Node node : nodes.values()) {
            OutputEdge first = null;
            OutputEdge prev = null;
            for (OutputEdge e : node) {
                if (first == null) {
                    prev = first = e;
                    continue;
                }
                checkEdges(prev, e, node.getBiv(), err);
                prev = e;
            }
            if (first == prev) {
                err.addPoint(node.getBiv());
                if (first != null) err.addEdge(first);
                
            } else {
                checkEdges(first, prev, node.getBiv(), err);
            }
        }
        return err;
    }

    /**
     * Checks whether the angle between the two edges is less or equal to 180 degrees,
     * assuming clockwise rotation.
     * 
     * @param e1     The first edge to check.
     * @param e2     The second edge to check.
     * @param target The target vertex both edges have in common.
     * @param err    The checker error to update.
     */
    private void checkEdges(OutputEdge e1, OutputEdge e2, BaseInputVertex target, CheckerError err) {
        BaseInputVertex other1 = (e1.getV1().equals(target) ? e1.getV2() : e1.getV1());
        BaseInputVertex other2 = (e2.getV1().equals(target) ? e2.getV2() : e2.getV1());
        if (other1.equals(other2)) {
            err.addEdges(List.of(e1, e2));
            err.addPoint(target);
            return;
        }
        Edge e = new Edge(target.getV(), other1.getV());
        double ori = e.relOri(other2.getV());
        if (ori < 0) {
            err.addEdges(List.of(e1, e2));
            err.addPoint(target);
        }
    }
    
    
}
