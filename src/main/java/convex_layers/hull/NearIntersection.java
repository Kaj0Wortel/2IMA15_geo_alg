package convex_layers.hull;

import convex_layers.InputVertex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.Var;
import tools.data.collection.rb_tree.LinkedRBTree;
import tools.data.collection.rb_tree.RBTree;
import tools.log.Logger;

import java.lang.reflect.Method;
import java.util.List;


/**
 * Data class for a near intersection generated from a {@link ConvexHull} class.
 */
@Getter
@AllArgsConstructor
public class NearIntersection {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The upper left vertex of the near intersection. */
    protected final VectorYNode v1;
    /** The lower left vertex of the near intersection. */
    protected final VectorYNode v2;
    /** The upper right vertex of the near intersection. */
    protected final VectorYNode v3;
    /** The lower right vertex of the near intersection. */
    protected final VectorYNode v4;
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
        BOTTOM;
        
        
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
    public void removeMiddleNodes(ConvexHull hull) {
        Logger.write("ORI: " + ori);
        if (ori == Orientation.LEFT || ori == Orientation.RIGHT) {
            VectorYNode node = getInnerNode1();
            VectorYNode target = getInnerNode2();
            
            while (node != null && node != target) {
                VectorYNode rem = node;
                node = node.prev();
                hull.remove(rem);
            }
            if (node != target) {
                Logger.write("Target was not reached in removal loop!", Logger.Type.WARNING);
            }
            hull.remove(target);
            
        } else {
            VectorYNode n1 = getInnerNode1();
            VectorYNode n2 = getInnerNode2();
            boolean top = (ori == Orientation.TOP);
            while (n1 != null && n1 != n2) {
                VectorYNode rem = n1;
                if (top) n1 = n1.next();
                else n1 = n1.prev();
                hull.remove(rem);
            }
            if (n1 == n2) {
                hull.remove(n1);
            } else {
                while (n2 != null) {
                    VectorYNode rem = n2;
                    if (top) n2 = n2.next();
                    else n2 = n2.prev();
                    hull.remove(rem);
                }
            }
        }
    }

    /**
     * @return The first inner node.
     */
    public VectorYNode getInnerNode1() {
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
    public VectorYNode getInnerNode2() {
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
    public VectorYNode getOuterNode1() {
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
    public VectorYNode getOuterNode2() {
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
