package convex_layers.hull;

import convex_layers.InputVertex;
import tools.Var;

public class NearIntersection {
    
    public VectorYNode v1;
    public VectorYNode v2;
    public VectorYNode v3;
    public VectorYNode v4;
    
    public NearIntersection(VectorYNode v1, VectorYNode v2, VectorYNode v3, VectorYNode v4) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
    }
    
    
    @Override
    public String toString() {
        return getClass().getCanonicalName() + "[" + Var.LS +
                "    v1: " + (v1 == null ? "null" : v1.toString()) + Var.LS +
                "    v2: " + (v2 == null ? "null" : v2.toString()) + Var.LS +
                "    v3: " + (v3 == null ? "null" : v3.toString()) + Var.LS +
                "    v4: " + (v4 == null ? "null" : v4.toString()) + Var.LS +
                "]";
    }
        
}
