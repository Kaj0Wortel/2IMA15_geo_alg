package convex_layers.data;

import convex_layers.BaseInputVertex;
import convex_layers.data.prior_tree.PriorTreeSearch;

public class PriorTreeSearchTest
        extends Range2DSearchTest {
    
    @Override
    public Range2DSearch<BaseInputVertex> getSearch() {
        return new PriorTreeSearch<>();
    }
    
    
}
