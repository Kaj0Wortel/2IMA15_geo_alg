package convex_layers.evaluate;

import convex_layers.*;
import convex_layers.data.IgnoreRangeSearch;
import convex_layers.data.Range2DSearch;
import convex_layers.data.kd_tree.KDTree;
import convex_layers.data.prior_tree.PriorTreeSearch;
import convex_layers.data.quad_tree.QuadTree;
import convex_layers.visual.NullVisualizer;
import convex_layers.visual.Visual;
import lombok.NonNull;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.WarmupMode;
import tools.Pair;
import tools.Var;
import tools.log.FileLogger;
import tools.log.Logger;
import tools.log.MultiLogger;
import tools.log.StreamLogger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
//@Warmup(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
//@Measurement(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Threads(value = 2)
@Fork(value = 1)
public class Benchmarking {
    
    /* ----------------------------------------------------------------------
     * Constants.
     * ----------------------------------------------------------------------
     */
    /** The data directory used for benchmarking. */
    private static final String DATA_DIR = System.getProperty("user.dir") + Var.FS + "data" + Var.FS;
    /** The directory the input files are located. */
    private static final String SOURCE_DIR = DATA_DIR + "challenge_1" + Var.FS + "uniform" + Var.FS;
    /** The directory the output files are located. */
    private static final String DEST_DIR = DATA_DIR + "sol" + Var.FS;
    /** The directory the benchmark reports go. */
    private static final String BENCH_DIR = DATA_DIR + "benchmark" + Var.FS;
    
    /** The input files from the input folder. */
    @NonNull
    private static final File[] IN_FILES = new File(SOURCE_DIR).listFiles();
    static {
        if (IN_FILES == null) throw new IllegalStateException();
        Arrays.sort(IN_FILES, (f1, f2) -> String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName()));
    }
    
    /** The current run. */
    private static int RUN = 0;
    
    /** The logger used to log errors. */
    private static final Logger ERROR_LOGGER;
    static {
        Logger log = null;
        try {
            log = new MultiLogger(
                    new StreamLogger(System.err),
                    new FileLogger(Var.LOG_DIR + "err_log.log")
            );
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
            
        } finally {
            ERROR_LOGGER = log;
        }
    }
    
    
    /* ----------------------------------------------------------------------
     * Variables.
     * ----------------------------------------------------------------------
     */
    /** The problem to solve. */
    private Pair<Problem2, File> problem;


    /* ----------------------------------------------------------------------
     * Functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Sets-up the problem for the current run.
     */
    @Setup
    public void setup() {
        problem = loadProblem(RUN);
    }
    
    /**
     * Executes the algorithm using the {@link KDTree} class.
     */
    @Benchmark
    public void kdTree() {
        execute(KDTree.class);
    }
    
    /**
     * Executes the algorithm using the {@link PriorTreeSearch} class.
     */
    @Benchmark
    public void priorTree() {
        execute(PriorTreeSearch.class);
    }
    
    /**
     * Executes the algorithm using the {@link QuadTree} class.
     */
    @Benchmark
    public void quadTree() {
        execute(QuadTree.class);
    }
    
    /**
     * Executes the algorithm using the {@link IgnoreRangeSearch} class.
     */
    @Benchmark
    public void noRangeSearch() {
        if (RUN <= 54) execute(IgnoreRangeSearch.class);
    }
    
    /**
     * Executes the algorithm using the given 2D range search class.
     * 
     * @param searchClass The 2D range search class used in the algorithm.
     */
    @SuppressWarnings("rawtypes")
    private void execute(Class<? extends Range2DSearch> searchClass) {
        Logger.setDefaultLogger(null);
        try {
            new ConvexLayersOptimized(searchClass).solve(problem.getFirst(), new NullVisualizer());
            
        } catch (Exception e) {
            Logger.setDefaultLogger(ERROR_LOGGER);
            Logger.write(e);
            Logger.setDefaultLogger(null);
        }
    }
    
    
    /* ----------------------------------------------------------------------
     * Static functions.
     * ----------------------------------------------------------------------
     */
    /**
     * Loads a problem. Additionally couples it with the destination file.
     *  
     * @param run The file to process.
     * 
     * @return A pair containing the problem with the destination file.
     */
    @SuppressWarnings("ConstantConditions")
    public static Pair<Problem2, File> loadProblem(int run) {
        File inFile = IN_FILES[run];
        File outFile = new File(DEST_DIR + inFile.getName() + ".solution.json");
        Problem2 p = ProblemIO.readProblem(inFile);
        return new Pair<>(p, outFile);
    }
    
    /**
     * Runs the {@code i}'th file.
     * 
     * @param opt The benchmark options.
     * @param i   The file to test.
     *
     * @throws RunnerException If the runner threw an exception.
     */
    @SuppressWarnings("ConstantConditions")
    private static void runFile(ChainedOptionsBuilder opt, int i)
            throws RunnerException {
        if (i <= 28) { // 100-2
            opt.warmupIterations(20).measurementIterations(50);
        } else if (i <= 46) { // 1000-2
            opt.warmupIterations(10).measurementIterations(25);
        } else if (i <= 54) { // 5000-2
            opt.warmupIterations(5).measurementIterations(20);
        } else if (i == 64) { // 10000-2
            opt.warmupIterations(2).measurementIterations(10);
        } else {
            opt.warmupIterations(1).measurementIterations(5);
        }
        
        String fileName = IN_FILES[RUN = i].getName();
        fileName = fileName.substring(0, fileName.length() - 4) + "tex";
        opt.result(BENCH_DIR + fileName);
        new Runner(opt.build()).run();
    }
    
    /**
     * Benchmarks all files in the input directory.
     * 
     * @throws RunnerException If the runner threw an exception.
     */
    public static void benchmark()
            throws RunnerException {
        ChainedOptionsBuilder opt = new OptionsBuilder()
                .include(Benchmarking.class.getSimpleName())
                .shouldDoGC(true)
                .resultFormat(ResultFormatType.LATEX)
                // Warmup
                .warmupTime(TimeValue.milliseconds(10_000))
                .warmupMode(WarmupMode.BULK_INDI)
                .warmupBatchSize(1)
                .warmupIterations(1)
                // Measurement
                .measurementTime(TimeValue.milliseconds(10_000))
                .measurementBatchSize(1)
                .measurementIterations(1)
                ;
        
        int amt = 1;// files.length;
        for (int i = 0; i < amt; i++) {
            runFile(opt, i);
        }
    }
    
    /**
     * Runs all files in the input directory.
     */
    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    public static void runAll() {
        Class<? extends Range2DSearch> searchClass = KDTree.class;
        Visual vis = new NullVisualizer();
        for (int i = 0; i < IN_FILES.length; i++) {
            Logger.setDefaultLogger(null);
            Pair<Problem2, File> p = loadProblem(i);
            try {
                System.out.println("Solving " + IN_FILES[i].getName());
                Collection<OutputEdge> sol = new ConvexLayersOptimized(searchClass)
                        .solve(p.getFirst(), vis);
                ProblemIO.saveSolution(p.getSecond(), sol, p.getFirst());

            } catch (Exception e) {
                Logger.setDefaultLogger(ERROR_LOGGER);
                Logger.write(e);
                Logger.setDefaultLogger(null);
            }
        }
    }

    /**
     * The main function of this class.
     * 
     * @param args Not used.
     *
     * @throws RunnerException If the runner threw an exception.
     */
    public static void main(String... args)
            throws RunnerException {
        benchmark();
        //runAll();
    }
    
    
}
