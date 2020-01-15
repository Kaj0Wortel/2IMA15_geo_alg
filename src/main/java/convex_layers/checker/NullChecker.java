package convex_layers.checker;

import convex_layers.OutputEdge;
import convex_layers.Problem2;

import java.util.Collection;

public class NullChecker
        implements Checker {
    
    
    @Override
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        return new CheckerError();
    }
    
    
}
