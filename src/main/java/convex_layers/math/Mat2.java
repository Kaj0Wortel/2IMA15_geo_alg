package convex_layers.math;

import tools.MultiTool;
import tools.Var;

public class Mat2 {
    
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
    
    public double det() {
        return m00()*m11() - m10()*m01();
    }
    
    public Mat2 add(Mat2 m) {
        return new Mat2(
                m00() + m.m00(), m01() + m.m01(),
                m10() + m.m10(), m11() + m.m11()
        );
    }
    
    public Mat2 sub(Mat2 m) {
        return new Mat2(
                m00() - m.m00(), m01() - m.m01(),
                m10() - m.m10(), m11() - m.m11()
        );
    }
    
    public Mat2 isub(Mat2 m) {
        return new Mat2(
                m.m00() - m00(), m.m01() - m01(),
                m.m10() - m10(), m.m11() - m11()
        );
    }
    
    public Mat2 transpose() {
        return new Mat2(m00(), m10(), m01(), m11());
    }
    
    public Mat2 mul(Mat2 m) {
        return new Mat2(
                m00()*m.m00() + m01()*m.m10(), m00()*m.m01() + m01()*m.m11(),
                m10()*m.m00() + m11()*m.m10(), m10()*m.m01() + m11()*m.m11()
        );
    }
    
    public Vector mul(Vector v) {
        return new Vector(m00()*v.x() + m01()*v.y(), m10()*v.x()+ m11()*v.y());
    }
    
    public Vector imul(Vector v) {
        return new Vector(v.x()*m00() + v.y()*m10(), v.x()*m01() + v.y()*m11());
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
    
    public static void main(String[] args) {
        Edge e = new Edge(new Vector(1, 1), new Vector(3, 1));
        Vector v1 = new Vector(2, 2);
        Vector v2 = new Vector(2, 0);
        Vector v3 = new Vector(0, 0);
        Vector v4 = new Vector(0, 2);
        System.out.println(Math.toDegrees(e.angle(v1)));
        System.out.println(Math.toDegrees(e.angle(v2)));
        System.out.println(Math.toDegrees(e.angle(v3)));
        System.out.println(Math.toDegrees(e.angle(v4)));
        //System.out.println(Math.toDegrees(e.iangle(v)));
    }
    
    
}
