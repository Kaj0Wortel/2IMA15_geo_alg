package convex_layers;

import convex_layers.data.Node2D;
import convex_layers.math.Vector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tools.MultiTool;

import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
public class BaseInputVertex
        implements Node2D<BaseInputVertex> {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The ID of the vertex. */
    private final long id;
    /** The vertex. */
    private final Vector v;
    
    
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
    public BaseInputVertex(long id, double x, double y) {
        this(id, new Vector(x, y));
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
    @Override
    public double getX() {
        return v.x();
    }
    
    /**
     * @return The y-coordinate of the vertex.
     */
    @Override
    public double getY() {
        return v.y();
    }
    
    @Override
    public int hashCode() {
        return MultiTool.calcHashCode(new Object[] {id, v.hashCode()});
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseInputVertex)) return false;
        BaseInputVertex iv = (BaseInputVertex) obj;
        return id == iv.id && Objects.equals(v, iv.v);
    }
    
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[ID: " + id + ", vec: " + v.toString() + "]";
    }
    
    public String toShortString() {
        return "(ID=" + id + ",x=" + v.x() + ",y=" + v.y() + ")";
    }
    
    
}
