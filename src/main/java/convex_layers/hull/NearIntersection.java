package convex_layers.hull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.Var;


/**
 * Data class for a near intersection generated from a {@link ConvexHull} class.
 */
@Getter
@AllArgsConstructor
public class NearIntersection {
    
    /* ----------------------------------------------------------------------
     * Inner classes.
     * ----------------------------------------------------------------------
     */
    /**
     * Enum for the source tree.
     */
    public enum Tree {
        /** The vector is in the left tree. */
        LEFT,
        /** The vector is in the right tree. */
        RIGHT,
        /** The vector is the top of the tree. */
        TOP,
        /** The vector is the bottom of the tree. */
        BOTTOM;
    }
    
    
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
    
    /** The originating tree of {@link #v1}. */
    protected final Tree t1;
    /** The originating tree of {@link #v2}. */
    protected final Tree t2;
    /** The originating tree of {@link #v3}. */
    protected final Tree t3;
    /** The originating tree of {@link #v4}. */
    protected final Tree t4;


    /* ----------------------------------------------------------------------
     * Constructors.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new near intersection from the given four points and their originating tree.
     * Additionally sets the trees to {@link Tree#BOTTOM} or {@link Tree#TOP} if needed.
     * 
     * @param v1 The upper left vertex of the near intersection.
     * @param v2 The lower left vertex of the near intersection.
     * @param v3 The upper right vertex of the near intersection.
     * @param v4 The lower right vertex of the near intersection.
     * @param t1 The originating tree of {@link #v1}
     * @param t2 The originating tree of {@link #v2}.
     * @param t3 The originating tree of {@link #v3}.
     * @param t4 The originating tree of {@link #v4}.
     * @param top    The top element of the trees.
     * @param bottom The bottom element of the trees.
     *               
     * @see #NearIntersection(VectorYNode, VectorYNode, VectorYNode, VectorYNode, Tree, Tree, Tree, Tree)
     */
    public NearIntersection(VectorYNode v1, VectorYNode v2, VectorYNode v3, VectorYNode v4,
                            Tree t1, Tree t2, Tree t3, Tree t4,
                            VectorYNode top, VectorYNode bottom) {
        this(v1, v2, v3, v4,
                (v1 == bottom ? Tree.BOTTOM : (v1 == top ? Tree.TOP : t1)),
                (v2 == bottom ? Tree.BOTTOM : (v2 == top ? Tree.TOP : t2)),
                (v3 == bottom ? Tree.BOTTOM : (v3 == top ? Tree.TOP : t3)),
                (v4 == bottom ? Tree.BOTTOM : (v4 == top ? Tree.TOP : t4))
        );
    }


    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[" + Var.LS +
                "    v1: " + (v1 == null ? "null" : v1.toString()) + "," + Var.LS +
                "    v2: " + (v2 == null ? "null" : v2.toString()) + "," + Var.LS +
                "    v3: " + (v3 == null ? "null" : v3.toString()) + "," + Var.LS +
                "    v4: " + (v4 == null ? "null" : v4.toString()) + Var.LS +
                "]";
    }
    
        
}
