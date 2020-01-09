package convex_layers.data;

import convex_layers.BaseInputVertex;
import convex_layers.data.quad_tree.QuadTree;

public class QuadTreeTest
        extends Range2DSearchTest {
    
    @Override
    public Range2DSearch<BaseInputVertex> getSearch() {
        return new QuadTree<>();
    }
    
    
}
