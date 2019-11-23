package convex_layers;

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
public class Vertex
        implements PublicCloneable {
    /** The ID of the vertex. */
    protected final long id;
    /** The x-coordinate of the vertex. */
    protected double x;
    /** The x-coordinate of the vertex. */
    protected double y;
    /** Whether the vertex is part of a hull. */
    protected boolean hulled = false;
    /** The previous vertex on the hull. */
    protected Vertex prev; // TODO: we certainly do NOT want this.
    /** The next vertex on the hull. */
    protected Vertex next; // TODO: we certainly do NOT want this.

    @Override
    public Vertex clone() {
        return new Vertex(id, x, y, hulled, prev, next);
    }

    public Vertex add(Vertex v) {
        return new Vertex(-1, x + v.x, y + v.y, false, null, null);
    }

    public Vertex sub(Vertex v) {
        return new Vertex(-1, x - v.x, y - v.y, false, null, null);
    }
    
    
}
