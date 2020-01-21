package convex_layers.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.MultiTool;
import tools.PublicCloneable;

/**
 * Mathematical vector representation class with tool functions.
 */
@AllArgsConstructor
@Getter
@Setter
public class Vector
        implements PublicCloneable {
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The x-coordinate of the vertex. */
    private double x;
    /** The x-coordinate of the vertex. */
    private double y;
    
    
    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
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
    
    /**
     * Adds the given vector to this vector.
     * 
     * @param v The vector to add.
     * @return The result of the addition, i.e. {@code this + v}.
     */
    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y);
    }
    
    /**
     * Subtracts the given vector from this vector.
     *
     * @param v The vector to subtract.
     * @return The result of the subtraction, i.e. {@code this - v}.
     */
    public Vector sub(Vector v) {
        return new Vector(x - v.x, y - v.y);
    }
    
    /**
     * Subtracts the given vector to this vector.
     *
     * @param v The vector to subtract from.
     * @return The result of the subtraction, i.e. {@code y - this}.
     */
    public Vector isub(Vector v) {
        return new Vector(v.x - x, v.y - y);
    }

    /**
     * Computes the dot-product of this vector with the given vector.
     * 
     * @param v The vector compute the dot-product with.
     * @return The dot product of the two vectors, i.e. {@code this dot v}.
     */
    public double dot(Vector v) {
        return x*v.x + y*v.y;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) return false;
        Vector v = (Vector) obj;
        return x == v.x && y == v.y;
    }
    
    @Override
    public int hashCode() {
        return MultiTool.calcHashCode(new Object[] {x, y});
    }
    
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Vector clone() {
        return new Vector(x, y);
    }
    
    
}
