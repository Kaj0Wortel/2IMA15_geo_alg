package convex_layers.checker;

import convex_layers.OutputEdge;
import convex_layers.Problem2;

import java.util.Collection;

public class MultiChecker
        implements Checker {
    
    /** The checkers used to check the results. */
    private Checker[] checkers;
    
    public MultiChecker(Checker... checkers) {
        if (checkers == null) this.checkers = new Checker[0];
        else this.checkers = checkers;
    }
    
    @Override
    public CheckerError check(Problem2 problem, Collection<OutputEdge> sol) {
        CheckerError err = new CheckerError();
        for (Checker c : checkers) {
            CheckerError res = c.check(problem, sol);
            err.addEdges(res.getEdges());
            err.addPoints(res.getPoints());
        }
        return err;
    }
    
    
}
