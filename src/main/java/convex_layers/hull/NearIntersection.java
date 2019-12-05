package convex_layers.hull;

import convex_layers.InputVertex;

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
        
}
