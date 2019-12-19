package convex_layers.hull;

import convex_layers.BaseInputVertex;
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
public class VectorYEdge<IV extends BaseInputVertex> {
    /** The first node of the edge. */
    private final VectorYNode<IV> iv1;
    /** The second node of the edge. */
    private final VectorYNode<IV> iv2;


    /**
     * @return An edge representation of this input edge.
     */
    public Edge toEdge() {
        return new Edge(iv1.getIv(), iv2.getIv());
    }
    
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[" + iv1 + ", " + iv2 + "]";
    }
    
    
}
