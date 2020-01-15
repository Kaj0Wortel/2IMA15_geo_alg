package convex_layers.evaluate;

import convex_layers.BaseInputVertex;
import convex_layers.OutputEdge;
import convex_layers.Problem2;
import convex_layers.checker.CheckerError;
import convex_layers.data.Range2DSearch;

import java.util.Collection;

public class RunProperties {

    public double score;
    public double scoreLowerBound;
    public Problem2 problem;
    public Collection<OutputEdge> solution;
    public long startTime;
    public long endTime;
    public CheckerError error = new CheckerError();
    public Class<Range2DSearch<BaseInputVertex>> searchClass;
    public long seed;
    public Exception exception = null;

    public long getRunTime() {
        return endTime - startTime;
    }

    public double getRunSeconds() {
        return getRunTime() / 1000.0;
    }

    public boolean hasErrors() {
        return exception != null || error.hasErrors();
    }

    public double getScoreRation() {
        return score / scoreLowerBound;
    }

}
