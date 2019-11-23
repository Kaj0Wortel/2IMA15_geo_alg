package convex_layers;

import convex_layers.math.Vector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.PublicCloneable;

/**
 * class representing a point in the plane.
 */
@Getter
@Setter
@AllArgsConstructor
public class InputVertex
        implements PublicCloneable {
    /** The ID of the vertex. */
    private final long id;
    /** The x-coordinate of the vertex. */
    private Vector v;
    /** Whether the vertex is part of a hull. */
    private boolean hulled = false;
    /** The previous vertex on the hull. */
    private InputVertex prev; // TODO: we certainly do NOT want this.
    /** The next vertex on the hull. */
    private InputVertex next; // TODO: we certainly do NOT want this.

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
    
    public double getX() {
        return v.x();
    }
    
    public double getY() {
        return v.y();
    }

    @Override
    public InputVertex clone() {
        return new InputVertex(id, v.clone(), hulled, prev, next);
    }

    public InputVertex add(InputVertex iv) {
        return new InputVertex(-1, iv.v.add(v), false, null, null);
    }

    public InputVertex sub(InputVertex iv) {
        return new InputVertex(-1, v.sub(iv.v), false, null, null);
    }
    
    
}
