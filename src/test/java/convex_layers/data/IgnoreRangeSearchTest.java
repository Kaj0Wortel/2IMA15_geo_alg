package convex_layers.data;

import convex_layers.BaseInputVertex;

public class IgnoreRangeSearchTest
        extends Range2DSearchTest {
    
    @Override
    public Range2DSearch<BaseInputVertex> getSearch() {
        return new IgnoreRangeSearch<>();
    }
    
    
}
