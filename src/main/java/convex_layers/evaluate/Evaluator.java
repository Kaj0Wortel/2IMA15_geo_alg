package convex_layers.evaluate;

import convex_layers.*;
import convex_layers.checker.*;
import convex_layers.data.IgnoreRangeSearch;
import convex_layers.data.Range2DSearch;
import convex_layers.data.kd_tree.KDTree;
import convex_layers.data.prior_tree.PriorTreeSearch;
import convex_layers.data.quad_tree.QuadTree;
import convex_layers.hull.ConvexHull;
import convex_layers.visual.NullVisualizer;
import convex_layers.visual.Visual;
import convex_layers.visual.VisualRender;
import convex_layers.visual.Visualizer;
import tools.Pair;
import tools.Var;
import tools.font.FontLoader;
import tools.log.Logger;
import tools.log.NullLogger;
import tools.log.StreamLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class Evaluator {
    
    private static final String FOLDER = "challenge_1";
    
    private Visual errorVis = new VisualRender();
    private Logger logger = new StreamLogger(System.out);
//    Logger logger = NullLogger.getInstance();
    boolean checkValidity = false;
    boolean calculateProperties = true;
    boolean visualizeRun = false;
    boolean visualizeOutput = false;
    boolean saveSolution = false;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss.SSS");
    
    // Put Seeds that give an error in here so we have a list of them for fixing
    private static final long[] ERROR_SEEDS = {
            7930711523825690689L
    };
    
    public List<Pair<Problem2, File>> getProblemsInFolder(String type) {
        String[] names = {
                "uniform-0010000-1"
        };
        List<Pair<Problem2, File>> pairs = new ArrayList<>();
        for (String name : names) {
            pairs.add(getProblem(type, name));
        }
        return pairs;
    }
    
    public static Pair<Problem2, File> getProblem(String type, String name) {
        String path = "data" + Var.FS + FOLDER + Var.FS + type + Var.FS + name;

        File inFile = new File(path + ".instance.json");
        File outFile = new File(path + ".solution.json");
        Problem2 problem = ProblemIO.readProblem(inFile);
        return new Pair<>(problem, outFile);
    }
    
    @SuppressWarnings("unchecked")
    public void evaluate() {
        Logger.write("Evaluator started", Logger.Type.INFO);
//        String type = "images";
//        String name = "uniform-0000015-1";
//        String name = "uniform-0000040-1";
//        String name = "uniform-0000060-1";
//        String name = "uniform-0001000-1";
//        String name = "uniform-0010000-1";
//        String name = "euro-night-0010000";

        String[] names = {
//            "uniform-0000010-1",
            "uniform-0000100-1",
//            "uniform-0001000-1",
//            "uniform-0010000-1",
//            "uniform-0100000-1",0004000",
//                "parix-0004000",
//            "mona-lisa-1000000",
        };
        Class<Range2DSearch<BaseInputVertex>>[] searches = new Class[] {
//                IgnoreRangeSearch.class,
//                PriorTreeSearch.class,
//                QuadTree.class,
                KDTree.class,
        };
        
        for (String name : names) {
            for (Class<Range2DSearch<BaseInputVertex>> search : searches) {
                Logger.write("Running for search structure: " + search.getCanonicalName(),
                        Logger.Type.INFO);
                Pair<Problem2, File> prob = getProblem("uniform", name);
                Problem2 problem = prob.getFirst();
                File outFile = prob.getSecond();

                for (int i = 0; i < 1; i ++) {
                    Logger.write("Starting iteration: " + i, Logger.Type.INFO);
                    
                    RunProperties properties = evaluate(problem, search, outFile);
                    Logger.write("Errors: " + properties.hasErrors(), Logger.Type.INFO);
                    if (properties.hasErrors()) {
                        Logger.setDefaultLogger(new StreamLogger(System.out));
                        Logger.write(properties.exception);
                        Logger.write("Seed of error: " + properties.seed, Logger.Type.ERROR);
                        return;
                    }
                }
            }
        }
        Logger.setDefaultLogger(new StreamLogger(System.out));
        Logger.write("********** Evaluation finished **********", Logger.Type.INFO);
    }
    
    public RunProperties evaluate(Problem2 problem, Class<Range2DSearch<BaseInputVertex>> search, File outFile) {
        long seed = new Random().nextLong();
        return evaluate(problem, search, outFile, seed);
    }
    
    public RunProperties evaluate(Problem2 problem, Class<Range2DSearch<BaseInputVertex>> search,
                                  File outFile, long seed) {
        ConvexHull.setSeed(seed);
        
        RunProperties properties = new RunProperties();
        properties.problem = problem;
        properties.searchClass = search;
        properties.seed = ConvexHull.getSeed();
        Visual vis = visualizeRun ? new Visualizer() : new NullVisualizer();
        
        Solver solver = new ConvexLayersOptimized(search);
        Checker checker = new MultiChecker(new EdgeIntersectionChecker(), new ConvexChecker());
        
        Logger.write("========== Solving problem " + problem.getName() + " using "
                + search.getName() +  " ==========", Logger.Type.INFO);
        try {
            properties.startTime = System.currentTimeMillis();
            Logger.write("Started at time " + DATE_FORMAT.format(new Date(properties.startTime)),
                    Logger.Type.INFO);
            
            Logger logger = Logger.getLog();
            Logger.setDefaultLogger(NullLogger.getInstance());
            properties.solution = solver.solve(problem, vis);
            Logger.setDefaultLogger(logger);

            properties.endTime = System.currentTimeMillis();
            Logger.write("End time: " + DATE_FORMAT.format(new Date(properties.endTime)),
                    Logger.Type.INFO);

            double runSeconds = properties.getRunSeconds();
            Logger.write("Running time (s): " + runSeconds);
        } catch (Exception e) {
            properties.exception = e;
            Logger.write(e);
            return properties;
        }
        
        properties.error = new CheckerError();
        if (checkValidity) {
            Logger.write("==========  Checking validity  ==========", Logger.Type.INFO);
            properties.error = checker.check(problem, properties.solution);
            Logger.write("Errors: " + properties.hasErrors(), Logger.Type.INFO);
            if (properties.hasErrors()) {
                Logger.write("NB: Warning! Solution has errors!", Logger.Type.WARNING);
            }
        }
        
        if (calculateProperties) {
            Logger.write("========== Score / properties ==========", Logger.Type.INFO);
            properties.scoreLowerBound = ScoreCalculator.calculateLowerBoundScore(problem);
            Logger.write("Score lower bound: " + properties.scoreLowerBound, Logger.Type.INFO);
            properties.score = ScoreCalculator.calculateScore(problem, properties.solution);
            Logger.write("Score: " + properties.score, Logger.Type.INFO);
            Logger.write("That's " + properties.getScoreRation() + " times as much as the lower bound.",
                    Logger.Type.INFO);
            
            double runSeconds = properties.getRunSeconds();
            Logger.write("Running time (s): " + runSeconds, Logger.Type.INFO);
        }
        
        if (visualizeOutput) {
            Logger.write("========== Drawing picture ==========", Logger.Type.INFO);
            drawPicture(properties.problem, properties.solution, properties.error);
        }
        
        if (saveSolution) {
            Logger.write("========== Saving solution ==========", Logger.Type.INFO);
            if (properties.hasErrors()) {
                Logger.write("Not writing solution to file since there are errors in the solution!",
                        Logger.Type.INFO);
            } else {
                ProblemIO.saveSolution(outFile, properties.solution, problem);
            }
        }

        Logger.write("========== ========== ==========", Logger.Type.INFO);
        
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
        FontLoader.syncLoad();
        Logger.setDefaultLogger(new StreamLogger(System.out));
        new Evaluator().evaluate();
    }
    
    
}
