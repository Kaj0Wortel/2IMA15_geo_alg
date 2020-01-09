package convex_layers.data;

import convex_layers.BaseInputVertex;
import convex_layers.data.kd_tree.KDTree;

public class KDTreeTest
        extends Range2DSearchTest {
    
    @Override
    public Range2DSearch<BaseInputVertex> getSearch() {
        return new KDTree<>();
    }
    
    
}
