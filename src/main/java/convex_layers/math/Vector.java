package convex_layers.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.PublicCloneable;

@AllArgsConstructor
@Getter
@Setter
public class Vector
        implements PublicCloneable {
    /** The x-coordinate of the vertex. */
    private double x;
    /** The x-coordinate of the vertex. */
    private double y;
    
    /**
     * Shorthand for {@link #getX()}.
     * 
     * @return The x-coordinate of the vector.
     */
    public double x() {
        return x;
    }
    
    /**
     * Shorthand for {@link #getY()}.
     *
     * @return The y-coordinate of the vector.
     */
    public double y() {
        return y;
    }
    
    /**
     * Shorthand for {@link #setX(double)}.
     */
    public void x(double x) {
        this.x = x;
    }
    
    /**
     * Shorthand for {@link #setX(double)}.
     */
    public void y(double y) {
        this.y = y;
    }
    
    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }
    
    public Vector sub(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }

    public Vector isub(Vector v) {
        return new Vector(v.x - x, v.y - y);
    }
    
    public double dot(Vector v) {
        return x*v.y + y*v.x;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) return false;
        Vector v = (Vector) obj;
        return x == v.x && y == v.y;
    }
    
    @Override
    public Vector clone() {
        return new Vector(x, y);
    }
    
    
}
