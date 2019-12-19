package convex_layers.hull;

import convex_layers.BaseInputVertex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.Var;
import tools.log.Logger;


/**
 * Data class for a near intersection generated from a {@link ConvexHull} class.
 */
@Getter
@AllArgsConstructor
public class NearIntersection<IV extends BaseInputVertex> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The upper left vertex of the near intersection. */
    protected final VectorYNode<IV> v1;
    /** The lower left vertex of the near intersection. */
    protected final VectorYNode<IV> v2;
    /** The upper right vertex of the near intersection. */
    protected final VectorYNode<IV> v3;
    /** The lower right vertex of the near intersection. */
    protected final VectorYNode<IV> v4;
    /** The orientation of the intersection.. */
    protected final Orientation ori;
    
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Enum for the orientation of the intersection.
     */
    public enum Orientation {
        /** Part to replace is on the left side.*/
        LEFT,
        /** Part to replace is on the right side. */
        RIGHT,
        /** Part to replace is on the top side. */
        TOP,
        /** Part to replace is on the bottom side. */
        BOTTOM
        
        
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[" + Var.LS +
                "    v1 : " + v1 + "," + Var.LS +
                "    v2 : " + v2+ "," + Var.LS +
                "    v3 : " + v3 + "," + Var.LS +
                "    v4 : " + v4 + "," +  Var.LS +
                "    ori: " + ori + Var.LS +
                "]";
    }

    /**
     * Removes all nodes in the inner part of the intersections.
     * 
     * @param hull The hull the vertices belong to.
     */
    public void removeMiddleNodes(ConvexHull<IV> hull) {
        Logger.write("ORI: " + ori);
        VectorYNode<IV> node = getInnerNode1();
        VectorYNode<IV> target = getInnerNode2();
        if (ori == Orientation.LEFT || ori == Orientation.RIGHT) {
            Logger.write("  " + node);
            Logger.write("  " + target);
            while (node != null && node != target) {
                VectorYNode<IV> rem = node;
                node = (ori == Orientation.LEFT
                        ? hull.counterClockwise(node)
                        : hull.clockwise(node)
                );
                hull.remove(rem);
            }
            if (node != target) {
                Logger.write("Target was not reached in removal loop!", Logger.Type.WARNING);
            }
            hull.remove(target);
            
        } else {
            Logger.write("NODE: " + node);
            while (node != null && node != target) {
                VectorYNode<IV> rem = node;
                node = (ori == Orientation.TOP
                        ? hull.clockwise(node)
                        : hull.counterClockwise(node)
                );
                hull.remove(rem);
            }
        }
    }

    /**
     * @return The first inner node.
     */
    public VectorYNode<IV> getInnerNode1() {
        switch (ori) {
            case LEFT:
            case BOTTOM:
            case RIGHT:
                return v2;
            case TOP:
                return v1;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @return The second inner node.
     */
    public VectorYNode<IV> getInnerNode2() {
        switch (ori) {
            case LEFT:
            case BOTTOM:
            case RIGHT:
                return v4;
            case TOP:
                return v3;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @return The first outer node.
     */
    public VectorYNode<IV> getOuterNode1() {
        switch (ori) {
            case LEFT:
            case BOTTOM:
            case RIGHT:
                return v1;
            case TOP:
                return v2;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @return The second outer node.
     */
    public VectorYNode<IV> getOuterNode2() {
        switch (ori) {
            case LEFT:
            case BOTTOM:
            case RIGHT:
                return v3;
            case TOP:
                return v4;
            default:
                throw new IllegalStateException();
        }
    }
    
    
}
