package convex_layers.checker;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.Problem2;
import convex_layers.math.Edge;
import convex_layers.visual.Visual;
import convex_layers.visual.VisualRender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * Computes the intersections of the edges using a line sweep algorithm.
 * An linked hash set is used as state, instead of a heap, which results
 * in an {@code O(n<sup>2</sup>)} algorithm instead of an {@code O(n log(n))}
 * algorithm. However, it is a lot faster compared to
 * {@link EdgeIntersectionChecker} and much easier to implement without a heap.
 */
public class FastEdgeIntersectionChecker
        implements Checker {
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Node wrapper class which keeps track of an {@link OutputEdge}.
     */
    @Getter
    @Setter
    private static class Node {
        /** The current edge of the node. */
        private final OutputEdge edge;
        /** The edge representing the output edge. */
        private Edge e;
        
        
        /**
         * Creates a new node with the given output edge.
         * 
         * @param edge The edge to create the node for.
         */
        public Node(OutputEdge edge) {
            this.edge = edge;
            e = new Edge(edge.getV1(), edge.getV2());
        }


        /**
         * @return The edge corresponding to this node.
         */
        public Edge asEdge() {
            return e;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) return false;
            return Objects.equals(((Node) obj).edge, edge);
        }
        
        @Override
        public int hashCode() {
            return edge.hashCode();
        }
        
        @Override
        public String toString() {
            return edge.toString();
        }
        
    }
    
    
    /**
     * Interface for the events used in the queue.
     */
    private interface Event
            extends Comparable<Event> {
        
        /**
         * @return Returns the y-coordinate of the event.
         */
        double getY();
        
        /**
         * The priority of the event. A lower value means a higher priority.
         * 
         * @return The priority of the event.
         */
        int getPriority();
        
        /**
         * Processes the event.
         */
        void handleEvent(CheckerError err, Collection<Node> state);
        
        @Override
        default int compareTo(Event e) {
            double diff = getY() - e.getY();
            if (diff < 0) return Math.min(-1, (int) diff);
            else if (diff > 0) return Math.max(1, (int) diff);
            else if (diff == 0) {
                return getPriority() - e.getPriority();
            }
            throw new IllegalStateException();
        }
        
        
    }
    
    
    /**
     * Event denoting the beginning of an edge.
     */
    @Getter
    @RequiredArgsConstructor
    private static class BeginEdge
            implements Event {
        private final Node node;
        
        @Override
        public double getY() {
            return Math.min(node.getEdge().getV1().getY(), node.getEdge().getV2().getY());
        }
        
        @Override
        public int getPriority() {
            return 1;
        }
        
        @Override
        public void handleEvent(CheckerError err, Collection<Node> state) {
            for (Node n : state) {
                if (node.asEdge().intersects(n.asEdge())) {
                    if (!node.getEdge().hasEqualEndpointWith(n.getEdge())) {
                        err.addEdge(node.getEdge());
                        err.addEdge(n.getEdge());
                    }
                }
            }
            state.add(node);
        }
        
        
    }
    
    
    /**
     * Event denoting the ending of an edge.
     */
    @Getter
    @RequiredArgsConstructor
    private static class EndEdge
            implements Event {
        private final Node node;

        @Override
        public double getY() {
            return Math.max(node.getEdge().getV1().getY(), node.getEdge().getV2().getY());
        }

        @Override
        public int getPriority() {
            return 3;
        }
        
        @Override
        public void handleEvent(CheckerError err, Collection<Node> state) {
            state.remove(node);
        }
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        CheckerError err = new CheckerError();
        
        // Create and populate queue.
        Queue<Event> eventQueue = new PriorityQueue<>();
        for (OutputEdge e : sol) {
            Node n = new Node(e);
            eventQueue.add(new BeginEdge(n));
            eventQueue.add(new EndEdge(n));
        }
        
        Collection<Node> status = new LinkedHashSet<>();
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();
            event.handleEvent(err, status);
        }
        
        return err;
    }
    
    
}
