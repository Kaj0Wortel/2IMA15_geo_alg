package convex_layers;

import convex_layers.math.Vector;
import lombok.Getter;
import lombok.Setter;
import tools.PublicCloneable;

/**
 * class representing a point in the plane.
 */
@Getter
@Setter
public class InputVertex
        extends BaseInputVertex
        implements PublicCloneable {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** Whether the vertex is part of a hull. */
    private boolean hulled = false;
    /** The previous vertex on the hull in clockwise order. */
    private InputVertex prev;
    /** The next vertex on the hull in clockwise order. */
    private InputVertex next;
    
    
    /* ----------------------------------------------------------------------
     * Constructor.
     * ----------------------------------------------------------------------
     */
    /**
     * Creates a new non-linked input vertex.
     * 
     * @param id The id of the input vertex.
     * @param x  The x-coordinate of the input vertex.
     * @param y  The y-coordinate of the input vertex.
     */
    public InputVertex(long id, double x, double y) {
        this(id, x, y, false, null, null);
    }
    
    /**
     * Data constructor which creates a vertex for the underlying structure from the given x and y.
     * 
     * @param id     The id of the input vertex.
     * @param x      The x-coordinate of the vertex.
     * @param y      The y-coordinate of the vertex.
     * @param hulled Whether the vertex is part of the hull.
     * @param prev   The previous vertex on the hull.
     * @param next   The next vertex on the hull.
     */
    public InputVertex(long id, double x, double y, boolean hulled, InputVertex prev, InputVertex next) {
        this(id, new Vector(x, y), hulled, prev, next);
    }

    /**
     * Data constructor which initializes the input vertex.
     *
     * @param id     The id of the input vertex.
     * @param v      The vertex.
     * @param hulled Whether the vertex is part of the hull.
     * @param prev   The previous vertex on the hull.
     * @param next   The next vertex on the hull.
     */
    public InputVertex(long id, Vector v, boolean hulled, InputVertex prev, InputVertex next) {
        super(id, v);
        this.hulled = hulled;
        this.prev = prev;
        this.next = next;
    }
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Adds two vertices and creates a new input vertex.
     * 
     * @param iv The vertex to add.
     * 
     * @return A new input vertex with id {@code -1} and the sum of both vectors.
     */
    @Deprecated
    public InputVertex add(InputVertex iv) {
        return new InputVertex(-1, iv.getV().add(getV()), false, null, null);
    }
    
    /**
     * Subtracts the given vertex from this vertex.
     *
     * @param iv The vertex to subtract.
     *
     * @return A new input vertex with id {@code -1} and the subtraction of the two vectors.
     */
    public InputVertex sub(InputVertex iv) {
        return new InputVertex(-1, getV().sub(iv.getV()), false, null, null);
    }
    
    @Override
    public InputVertex clone() {
        return new InputVertex(getId(), getV().clone(), hulled, prev, next);
    }
    
    
}
