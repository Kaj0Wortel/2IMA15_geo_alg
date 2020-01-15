package convex_layers.hull;

import convex_layers.BaseInputVertex;
import convex_layers.math.Vector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.data.collection.rb_tree.LinkedRBKey;
import tools.data.collection.rb_tree.LinkedRBTree;
import tools.log.Logger;

import java.util.Objects;

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
    /** The convex hull this vector lies in. */
    private ConvexHull<IV> hull;
    /** Denotes whether the node lies in the left or the right hull. */
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
            int xDiff = Double.compare(getVec().x(), vyn.getVec().x());
            if (xDiff == 0) return 0;
            
            if (getVec().y() < split.y() || vyn.getVec().y() < split.y()) {
                return (isLeft ? -xDiff : xDiff);
                
            } else if (getVec().y() > split.y() || vyn.getVec().y() > split.y()) {
                return (isLeft ? xDiff : -xDiff);
                
            } else {
                if (getVec().y() == hull.getBottom().getY()) {
                    return (isLeft ? -xDiff : xDiff);
                    
                } else if (getVec().y() == hull.getTop().getY()) {
                    return (isLeft ? -xDiff : xDiff);
                    
                } else {
                    return 0;
                }
            }
        }
        throw new IllegalStateException("Invalid y-coordinate difference: " + yDiff);
    }
    
    @Override
    @SuppressWarnings("unchecked")
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
