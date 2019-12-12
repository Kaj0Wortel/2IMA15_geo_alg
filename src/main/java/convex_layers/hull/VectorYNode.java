package convex_layers.hull;

import convex_layers.InputVertex;
import convex_layers.math.Vector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.data.collection.rb_tree.LinkedRBKey;

/**
 * Linked red black tree key node class for sorting vertices on y-coordinate.
 */
@Getter
@Setter
@AllArgsConstructor
public class VectorYNode
        extends LinkedRBKey<VectorYNode> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The underlying input vertex. */
    private final InputVertex iv;


    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */

    /**
     * @return The vector from the input vertex.
     */
    public Vector getVec() {
        return iv.getV();
    }
    
    @Override
    public int compareTo(VectorYNode vyn) {
        double res = (iv.getY() - vyn.iv.getY());
        if (res == 0) return 0;
        else if (res < 0) return Math.min(-1, (int) res);
        else return Math.max(1, (int) res);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorYNode)) return false;
        return iv.equals(((VectorYNode) obj).iv);
    }
    
    @Override
    public int hashCode() {
        return iv.getV().hashCode();
    }
    
    @Override
    public String toString() {
        return iv.toString();
    }
    
    
}
