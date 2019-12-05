package convex_layers.hull;

import convex_layers.InputVertex;
import convex_layers.math.Vector;
import tools.data.collection.rb_tree.LinkedRBKey;

/**
 * 
 */
public class VectorYNode
        extends LinkedRBKey<VectorYNode> {
    
    private InputVertex iv;
    
    
    public VectorYNode(InputVertex iv) {
        this.iv = iv;
    }
    
    public Vector getVec() {
        return iv.getV();
    }
    
    public InputVertex getIV() {
        return iv;
    }
    
    @Override
    public int compareTo(VectorYNode vyn) {
        double res = (iv.getY() - vyn.iv.getY());
        if (res == 0) return 0;
        else if (res < 0) return Math.min(-1, (int) res);
        else return Math.max(1, (int) res);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorYNode)) return false;
        return iv.equals(((VectorYNode) obj).iv);
    }
    
    @Override
    public int hashCode() {
        return iv.getV().hashCode();
    }
    
    
}
