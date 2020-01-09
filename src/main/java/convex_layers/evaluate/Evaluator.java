package convex_layers.evaluate;

import convex_layers.*;
import convex_layers.checker.*;
import convex_layers.data.Range2DSearch;
import convex_layers.data.quad_tree.QuadTree;
import convex_layers.visual.NullVisualizer;
import convex_layers.visual.Visual;
import convex_layers.visual.VisualRender;
import convex_layers.visual.Visualizer;
import tools.Var;
import tools.log.Logger;
import tools.log.NullLogger;
import tools.log.StreamLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public class Evaluator {

    Visual errorVis = new VisualRender();
//    Logger logger = new StreamLogger(System.out);
    Logger logger = NullLogger.getInstance();
    boolean checkValidity = false;
    boolean calculateProperties = true;
    boolean visualizeRun = false;
    boolean visualizeOutput = false;
    boolean saveSolution = false;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss.SSS");

    public void evaluate() {
        String folder = "challenge_1";
        String type = "uniform";
//        String type = "images";
//        String name = "uniform-0000015-1";
//        String name = "uniform-0000040-1";
//        String name = "uniform-0000060-1";
//        String name = "uniform-0001000-1";
//        String name = "uniform-0010000-1";
//        String name = "euro-night-0010000";

        String[] names = {
            "uniform-0010000-1"
        };
        Class<Range2DSearch>[] searches = new Class[] {
                QuadTree.class
        };

        for (String name : names) {
            for (Class<Range2DSearch> search : searches) {

                String path = "data" + Var.FS + folder + Var.FS + type + Var.FS + name;

                File inFile = new File(path + ".instance.json");
                File outFile = new File(path + ".solution.json");
                Problem2 problem = ProblemIO.readProblem(inFile);
                RunProperties properties = evaluate(problem, search, outFile);
                System.out.println("Errors: " + properties.hasErrors());
            }
        }
    }

    public RunProperties evaluate(Problem2 problem, Class<Range2DSearch> search, File outFile) {
        RunProperties properties = new RunProperties();
        properties.problem = problem;
        properties.searchClass = search;

        Logger.setDefaultLogger(logger);
        Visual vis = visualizeRun ? new Visualizer() : new NullVisualizer();

        Solver solver = new ConvexLayersOptimized(search);
        Checker checker = new MultiChecker(new EdgeIntersectionChecker(), new ConvexChecker());

        Logger.write("========== Solving problem " + problem.getName() + " ==========");
        properties.startTime = System.currentTimeMillis();
        Logger.write("Started at time " + DATE_FORMAT.format(new Date(properties.startTime)));
        properties.solution = solver.solve(problem, vis);
        properties.endTime = System.currentTimeMillis();
        Logger.write("End time: " + DATE_FORMAT.format(new Date(properties.endTime)));

        properties.error = new CheckerError();
        if (checkValidity) {
            Logger.write("==========  Checking validity  ==========");
            properties.error = checker.check(problem, properties.solution);
            Logger.write("Errors: " + properties.hasErrors());
            if (properties.hasErrors()) {
                Logger.write("NB: Warning! Solution has errors!");
            }
        }

        if (calculateProperties) {
            Logger.write("========== Score / properties ==========");
            properties.scoreLowerBound = ScoreCalculator.calculateLowerBoundScore(problem);
            Logger.write("Score lower bound: " + properties.scoreLowerBound);
            properties.score = ScoreCalculator.calculateScore(problem, properties.solution);
            Logger.write("Score: " + properties.score);
            Logger.write("That's " + properties.getScoreRation() + " as much as the lower bound.");

            double runSeconds = properties.getRunSeconds() / 1000.0;
            Logger.write("Running time (s): " + runSeconds);
        }

        if (visualizeOutput) {
            Logger.write("========== Drawing picture ==========");
            drawPicture(problem, properties.solution, properties.error);
        }

        if (saveSolution) {
            Logger.write("========== Saving solution ==========");
            if (properties.hasErrors()) {
                Logger.write("Not writing solution to file since there are errors in the solution!");
            } else {
                ProblemIO.saveSolution(outFile, properties.solution, problem);
            }
        }
        Logger.write("========== ========== ==========");

        return properties;
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
