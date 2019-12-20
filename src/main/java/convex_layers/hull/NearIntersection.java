package convex_layers.hull;

import convex_layers.BaseInputVertex;
import convex_layers.visual.Visual;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.Var;


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
    /** The first outer vertex of the intersection. */
    protected final VectorYNode<IV> n1;
    /** The first inner vertex of the intersection. */
    protected final VectorYNode<IV> n2;
    /** The second inner vertex of the intersection. */
    protected final VectorYNode<IV> n3;
    /** The second outer vertex of the intersection. */
    protected final VectorYNode<IV> n4;
    /** Whether the inner part is defined in clockwise order. */
    protected final boolean clockwise;
    /** The first point of the line on the inner hull. */
    protected final VectorYNode<IV> innerVec1;
    /** The second point of the line on the inner hull. */
    protected final VectorYNode<IV> innerVec2;
    
    protected final boolean hullOnLeftSide;
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[" + Var.LS +
                "    n1 : " + n1 + "," + Var.LS +
                "    n2 : " + n2 + "," + Var.LS +
                "    n3 : " + n3 + "," + Var.LS +
                "    n4 : " + n4 + "," +  Var.LS +
                "    clockwise: " + clockwise + Var.LS +
                "    v1: " + innerVec1 + "," + Var.LS +
                "    v2: " + innerVec2 + "," + Var.LS +
                "]";
    }
    
    /**
     * Removes all nodes in the inner part of the intersections.
     * 
     * @param hull The hull the vertices belong to.
     */
    public void removeMiddleNodes(ConvexHull<IV> hull, Visual vis) {
        VectorYNode<IV> node = n2;
        while (node != null && node != n3) {
            VectorYNode<IV> rem = node;
            node = (clockwise
                    ? hull.clockwise(node)
                    : hull.counterClockwise(node)
            );
            hull.remove(rem);
            vis.redraw();
        }
        hull.remove(n3);
        
    }
    
    
}
