package convex_layers.math;

import tools.MultiTool;
import tools.PublicCloneable;
import tools.Var;

public class Mat2
            implements PublicCloneable {
    
    private double [][] mat;
    
    public Mat2(double m00, double m01, double m10, double m11) {
        mat = new double[][] {
                new double[] {m00, m01},
                new double[] {m10, m11}
        };
    }
    
    /**
     * Shorthand for {@code #get(0, 0)}.
     * @return The value at (0, 0).
     * @see #get(int, int)
     */
    public double m00() {
        return mat[0][0];
    }
    
    /**
     * Shorthand for {@code #get(1, 0)}.
     * @return The value at (1, 0).
     * @see #get(int, int)
     */
    public double m01() {
        return mat[0][1];
    }
    
    /**
     * Shorthand for {@code #get(0, 1)}.
     * @return The value at (0, 1).
     * @see #get(int, int)
     */
    public double m10() {
        return mat[1][0];
    }
    
    /**
     * Shorthand for {@code #get(1, 1)}.
     * @return The value at (1, 1).
     * @see #get(int, int)
     */
    public double m11() {
        return mat[1][1];
    }
    
    /**
     * Shorthand for {@code #set(0, 0, m)}.
     * @see #set(int, int, double)
     */
    public void m00(double m) {
        mat[0][0] = m;
    }
    
    /**
     * Shorthand for {@code #set(1, 0, m)}.
     * @see #set(int, int, double)
     */
    public void m01(double m) {
        mat[0][1] = m;
    }
    
    /**
     * Shorthand for {@code #set(0, 1, m)}.
     * @see #set(int, int, double)
     */
    public void m10(double m) {
        mat[1][0] = m;
    }
    
    /**
     * Shorthand for {@code #set(1, 1, m)}.
     * @see #set(int, int, double)
     */
    public void m11(double m) {
        mat[1][1] = m;
    }
    
    public double get(int x, int y) {
        return mat[y][x];
    }
    
    public void set(int x, int y, double m) {
        mat[y][x] = m;
    }
    
    /**
     * @return A fresh identity matrix.
     */
    public static Mat2 i() {
        return new Mat2(1, 0, 0, 1);
    }

    /**
     * @return The determinant of this matrix.
     */
    public double det() {
        return m00()*m11() - m10()*m01();
    }

    /**
     * Adds the given matrix to this matrix.
     *
     * @param m The matrix to add.
     * @return The result of the subtraction, i.e. {@code this + m}.
     */
    public Mat2 add(Mat2 m) {
        return new Mat2(
                m00() + m.m00(), m01() + m.m01(),
                m10() + m.m10(), m11() + m.m11()
        );
    }

    /**
     * Subtracts the given matrix from this matrix.
     * 
     * @param m The matrix to subtract.
     * @return The result of the subtraction, i.e. {@code this - m}.
     */
    public Mat2 sub(Mat2 m) {
        return new Mat2(
                m00() - m.m00(), m01() - m.m01(),
                m10() - m.m10(), m11() - m.m11()
        );
    }

    /**
     * Subtracts this matrix from the given matrix.
     *
     * @param m The mstrix to subtract from.
     * @return The result of the subtraction, i.e. {@code m - this}.
     */
    public Mat2 isub(Mat2 m) {
        return new Mat2(
                m.m00() - m00(), m.m01() - m01(),
                m.m10() - m10(), m.m11() - m11()
        );
    }

    /**
     * @return The transpose of the given matrix, i.e. {@code this<sup>T</sup>}.
     */
    public Mat2 transpose() {
        return new Mat2(m00(), m10(), m01(), m11());
    }

    /**
     * Multiplies this matrix with the given matrix.
     * 
     * @param m The matrix to multiply with.
     * @return The result of the matrix multiplication, i.e. {@code this * m}.
     */
    public Mat2 mul(Mat2 m) {
        return new Mat2(
                m00()*m.m00() + m01()*m.m10(), m00()*m.m01() + m01()*m.m11(),
                m10()*m.m00() + m11()*m.m10(), m10()*m.m01() + m11()*m.m11()
        );
    }

    /**
     * Multiplies this matrix with the given vector.
     * 
     * @param v The vector to multiply with.
     * @return The result of the multiplication, i.e. {@code this * v}.
     */
    public Vector mul(Vector v) {
        return new Vector(m00()*v.x() + m01()*v.y(), m10()*v.x()+ m11()*v.y());
    }

    /**
     * Multiplies the transpose of the given vector with this matrix.
     * 
     * @param v The vector to multiply with.
     * @return The result of the multiplication, i.e. {@code y<sup>T</sup> * this}.
     */
    public Vector imul(Vector v) {
        return new Vector(v.x()*m00() + v.y()*m10(), v.x()*m01() + v.y()*m11());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Mat2)) return false;
        Mat2 m = (Mat2) obj;
        return m00() == m.m00() && m01() == m.m01() &&
                m10() == m.m10() && m11() == m.m11();
    }
    
    @Override
    public String toString() {
        String[] strs = new String[4];
        int max = 0;
        for (int i = 0; i < strs.length; i++) {
            max = Math.max(max, (strs[i] = Double.toString(get(i % 2, i / 2))).length());
        }
        for (int i = 0; i < strs.length; i++) {
            if (strs[i].length() < max) {
                strs[i] = MultiTool.fillLeft(strs[i], max, ' ');
            }
        }
        
        return "[" + strs[0] + "  " + strs[1] + "]" + Var.LS +
                "[" + strs[2] + "  " + strs[3] + "]";
    }
    
    @Override
    public int hashCode() {
        return MultiTool.calcHashCode(mat);
    }
    
    @Override
    public Mat2 clone() {
        return new Mat2(m00(), m01(), m10(), m00());
    }
    
    
}
