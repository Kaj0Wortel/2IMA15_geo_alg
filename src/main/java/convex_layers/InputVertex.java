package convex_layers;

import convex_layers.math.Vector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.MultiTool;
import tools.PublicCloneable;

import java.util.Objects;

/**
 * class representing a point in the plane.
 */
@Getter
@Setter
@AllArgsConstructor
public class InputVertex
        implements PublicCloneable {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The ID of the vertex. */
    private final long id;
    /** The x-coordinate of the vertex. */
    private final Vector v;
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
        this.id = id;
        this.v = new Vector(x, y);
        this.hulled = hulled;
        this.prev = prev;
        this.next = next;
    }


    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * @return The ID of the input vertex.
     */
    public long id() {
        return id;
    }
    
    /**
     * @return The vector of the input vertex.
     */
    public Vector v() {
        return v;
    }

    /**
     * @return The x-coordinate of the vertex.
     */
    public double getX() {
        return v.x();
    }

    /**
     * @return The y-coordinate of the vertex.
     */
    public double getY() {
        return v.y();
    }
    
    /**
     * Adds two vertices and creates a new input vertex.
     * 
     * @param iv The vertex to add.
     * 
     * @return A new input vertex with id {@code -1} and the sum of both vectors.
     */
    @Deprecated
    public InputVertex add(InputVertex iv) {
        return new InputVertex(-1, iv.v.add(v), false, null, null);
    }
    
    /**
     * Subtracts the given vertex from this vertex.
     *
     * @param iv The vertex to subtract.
     *
     * @return A new input vertex with id {@code -1} and the subtraction of the two vectors.
     */
    public InputVertex sub(InputVertex iv) {
        return new InputVertex(-1, v.sub(iv.v), false, null, null);
    }
    
    @Override
    public InputVertex clone() {
        return new InputVertex(id, v.clone(), hulled, prev, next);
    }
    
    @Override
    public int hashCode() {
        return MultiTool.calcHashCode(new Object[] {id, v.hashCode()});
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InputVertex)) return false;
        InputVertex iv = (InputVertex) obj;
        return id == iv.id && Objects.equals(v, iv.v);
    }
    
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[ID: " + id + ", vec: " + v.toString() + "]";
    }
    
    
}
