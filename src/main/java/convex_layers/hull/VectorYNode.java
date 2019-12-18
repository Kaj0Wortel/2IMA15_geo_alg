package convex_layers.hull;

import convex_layers.BaseInputVertex;
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
public class VectorYNode<IV extends BaseInputVertex>
        extends LinkedRBKey<VectorYNode<IV>> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The underlying input vertex. */
    private final IV iv;
    
    private ConvexHull<IV> hull;
    
    private boolean isLeft;


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

    /**
     * @return Whether this vector is part of the left or right hull.
     */
    public boolean isLeft() {
        return isLeft;
    }

    /**
     * @param isLeft Whether this vector is part of the left or the right hull.
     */
    void setLeft(boolean isLeft) {
        this.isLeft = isLeft;
    }

    /**
     * @return The hull of this vector.
     */
    public ConvexHull<IV> getHull() {
        return hull;
    }
    
    @Override
    public int compareTo(VectorYNode vyn) {
        double yDiff = getVec().y() - vyn.getVec().y();
        if (yDiff < 0) return Math.min(-1, (int) yDiff);
        else if (yDiff > 0) return Math.max(1, (int) yDiff);
        else if (yDiff == 0) {
            if (hull.getMinX() == null || hull.getMaxX() == null) return 0;
            Vector split = (isLeft ? hull.getMinX() : hull.getMaxX()).getV();
            double xDiff = getVec().x() - vyn.getVec().x();
            if (getVec().x() < split.x() == isLeft) {
                if (xDiff < 0) return Math.max(1, (int) xDiff);
                else if (xDiff > 0) return Math.min(-1, (int) xDiff);

            } else if (getVec().x() > split.x() == isLeft) {
                if (xDiff < 0) return Math.min(-1, (int) xDiff);
                else if (xDiff > 0) return Math.max(1, (int) xDiff);
            }
        }
        return 0;
    }


    @SuppressWarnings("unchecked")@Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorYNode)) return false;
        return iv.equals(((VectorYNode<IV>) obj).iv);
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
