package convex_layers.hull;

import convex_layers.hull.ConvexHull;
import convex_layers.hull.NearIntersection;
import convex_layers.hull.VectorYNode;
import convex_layers.math.Edge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Edge storing two input vertices.
 */
@Getter
@Setter
@AllArgsConstructor
public class VectorYEdge {
    /** The first node of the edge. */
    private final VectorYNode iv1;
    /** The second node of the edge. */
    private final VectorYNode iv2;


    /**
     * @return An edge representation of this input edge.
     */
    public Edge toEdge() {
        return new Edge(iv1.getIv(), iv2.getIv());
    }
    
    /**
     * @param ori     The orientation of the edge relative to the originating hull.
     * @param relEdge The edge relative to which the second node should be returned.
     *
     * @return The first node of the edge, relative to the given edge.
     */
    public VectorYNode getFirst(NearIntersection.Orientation ori, Edge relEdge) {
        switch (ori) {
            case LEFT:
            case RIGHT:
                if (iv1.getIv().getY() > iv2.getIv().getY()) return iv1;
                else return iv2;
            case BOTTOM:
            case TOP:
                double relDist1 = relEdge.relOriRounded(iv1.getVec()) * relEdge.distance(iv1.getVec());
                double relDist2 = relEdge.relOriRounded(iv2.getVec()) * relEdge.distance(iv2.getVec());
                if (relDist1 < relDist2) return iv1;
                else return iv2;
            default:
                throw new IllegalStateException();
        }
    }
    
    /**
     * @param ori     The orientation of the edge relative to the originating hull.
     * @param relEdge The edge relative to which the second node should be returned.
     * 
     * @return The second node of the edge, relative to the given edge.
     */
    public VectorYNode getSecond(NearIntersection.Orientation ori, Edge relEdge) {
        if (getFirst(ori, relEdge) == iv1) return iv2;
        else return iv1;
    }
    
    
}
