package convex_layers.evaluate;

import convex_layers.*;
import convex_layers.checker.*;
import convex_layers.data.IgnoreRangeSearch;
import convex_layers.data.Range2DSearch;
import convex_layers.visual.NullVisualizer;
import convex_layers.visual.Visual;
import convex_layers.visual.VisualRender;
import convex_layers.visual.Visualizer;
import tools.Var;
import tools.log.Logger;
import tools.log.StreamLogger;

import java.io.File;
import java.util.Collection;

public class Evaluator {

    Visual errorVis = new VisualRender();
    Logger logger = new StreamLogger(System.out);
    boolean checkValidity = true;
    boolean calculateProperties = true;
    boolean visualizeRun = false;
    boolean visualizeOutput = true;
    boolean saveSolution = true;

    public void evaluate() {
        String folder = "challenge_1";
        String type = "uniform";
//        String type = "images";
//        String name = "uniform-0000015-1";
//        String name = "uniform-0000040-1";
//        String name = "uniform-0000060-1";
        String name = "uniform-0001000-1";
//        String name = "uniform-0010000-1";
//        String name = "euro-night-0010000";
        String path = "data" + Var.FS + folder + Var.FS + type + Var.FS + name;

        File inFile = new File(path + ".instance.json");
        File outFile = new File(path + ".solution.json");
        Problem2 problem = ProblemIO.readProblem(inFile);
        evaluate(problem, IgnoreRangeSearch.class, outFile);
    }

    public void evaluate(Problem2 problem, Class<? extends Range2DSearch> search, File outFile) {
        Logger.setDefaultLogger(logger);
        Visual vis = visualizeRun ? new Visualizer() : new NullVisualizer();

        Solver solver = new ConvexLayersOptimized(search);
        Checker checker = new MultiChecker(new EdgeIntersectionChecker(), new ConvexChecker());

        Logger.write("========== Solving problem " + problem.getName() + " ==========");
        long startTime = System.currentTimeMillis();
        Logger.write("Started at time " + startTime);
        Collection<OutputEdge> sol = solver.solve(problem, vis);
        long endTime = System.currentTimeMillis();
        Logger.write("End time: " + endTime);

        CheckerError error = new CheckerError();
        if (checkValidity) {
            Logger.write("==========  Checking validity  ==========");
            error = checker.check(problem, sol);
            Logger.write("Errors: " + error.hasErrors());
            if (error.hasErrors()) {
                Logger.write("NB: Warning! Solution has errors!");
            }
        }

        if (calculateProperties) {
            Logger.write("========== Score / properties ==========");
            double scoreLowerBound = ScoreCalculator.calculateLowerBoundScore(problem);
            Logger.write("Score lower bound: " + scoreLowerBound);
            double score = ScoreCalculator.calculateScore(problem, sol);
            Logger.write("Score: " + score);
            double scoreRation = score / scoreLowerBound;
            Logger.write("That's " + scoreRation + " as much as the lower bound.");

            long runTime = endTime - startTime;
            double runSeconds = runTime / 1000.0;
            Logger.write("Running time (s): " + runSeconds);
        }

        if (visualizeOutput) {
            Logger.write("========== Drawing picture ==========");
            drawPicture(problem, sol, error);
        }

        if (saveSolution) {
            Logger.write("========== Saving solution ==========");
            if (error.hasErrors()) {
                Logger.write("Not writing solution to file since there are errors in the solution!");
            } else {
                ProblemIO.saveSolution(outFile, sol, problem);
            }
        }
        Logger.write("========== ========== ==========");
    }

    private void drawPicture(Problem2 problem, Collection<OutputEdge> solution, CheckerError error) {
        errorVis.clearAll();
        errorVis.addPoint(Visual.toVec(problem.getVertices()));
        errorVis.addLabel(Visual.toLabel(problem.getVertices()));
        errorVis.addEdge(Visual.toEdge(solution));
        error.draw(errorVis);
    }

    public static void main(String[] args) {
        System.out.println("Evaluator started");
        new Evaluator().evaluate();
    }

}
