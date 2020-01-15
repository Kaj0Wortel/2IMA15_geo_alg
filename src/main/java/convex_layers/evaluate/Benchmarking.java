package convex_layers.evaluate;

import convex_layers.*;
import convex_layers.data.IgnoreRangeSearch;
import convex_layers.data.Range2DSearch;
import convex_layers.data.kd_tree.KDTree;
import convex_layers.data.prior_tree.PriorTreeSearch;
import convex_layers.data.quad_tree.QuadTree;
import convex_layers.hull.ConvexHull;
import convex_layers.visual.NullVisualizer;
import convex_layers.visual.Visual;
import lombok.NonNull;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.WarmupMode;
import tools.Pair;
import tools.Var;
import tools.log.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1_000, timeUnit = TimeUnit.MILLISECONDS)
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
    
    private static final Visual VIS = new NullVisualizer();
    
    /** The current run. */
    private static int RUN = 26; // A value between 0 and 81.
    
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
    @Setup(value = Level.Trial)
    public void benchmarkSetup() {
//        System.out.println("HERE---------------------------------------");
    }
    
    @Setup(value = Level.Iteration)
    public void iterationSetup() {
//        System.out.println("HERE+++++++++++++++++++++++++++++++++++++++++");
        problem = loadProblem(RUN);
    }
    
    /**
     * Sets a new seed for the convex hull.
     */
    @Setup(value = Level.Invocation)
    public void runSetup() {
//        System.out.println("HERE==========================================");
        ConvexHull.setSeed(Var.RAN.nextLong());
    }
    
    /**
     * Executes the algorithm using the {@link KDTree} class.
     */
    @Benchmark
    public void kdTree(Blackhole bh) {
        execute(KDTree.class, bh);
    }
    
    /**
     * Executes the algorithm using the {@link PriorTreeSearch} class.
     */
    @Benchmark
    public void priorTree(Blackhole bh) {
        execute(PriorTreeSearch.class, bh);
    }
    
    /**
     * Executes the algorithm using the {@link QuadTree} class.
     */
    @Benchmark
    public void quadTree(Blackhole bh) {
        execute(QuadTree.class, bh);
    }
    
    /**
     * Executes the algorithm using the {@link IgnoreRangeSearch} class.
     */
    @Benchmark
    public void noRangeSearch(Blackhole bh) {
        if (RUN <= 54) execute(IgnoreRangeSearch.class, bh);
    }
    
    /**
     * Executes the algorithm using the given 2D range search class.
     * 
     * @param searchClass The 2D range search class used in the algorithm.
     */
    @SuppressWarnings("rawtypes")
    private void execute(Class<? extends Range2DSearch> searchClass, Blackhole bh) {
        Logger.setDefaultLogger(NullLogger.getInstance());
        try {
            Collection<OutputEdge> sol = new ConvexLayersOptimized(searchClass).solve(problem.getFirst(), VIS);
            bh.consume(sol);
            
        } catch (Exception e) {
            Logger.setDefaultLogger(ERROR_LOGGER);
            Logger.write(e);
            Logger.setDefaultLogger(NullLogger.getInstance());
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
//        if (i <= 28) { // 100-2
//            opt.warmupBatchSize(20).measurementBatchSize(50);
//        } else if (i <= 46) { // 1000-2
//            opt.warmupBatchSize(10).measurementBatchSize(25);
//        } else if (i <= 54) { // 5000-2
//            opt.warmupBatchSize(5).measurementBatchSize(20);
//        } else if (i == 64) { // 10000-2
//            opt.warmupBatchSize(2).measurementBatchSize(10);
//        } else {
//            opt.warmupBatchSize(1).measurementBatchSize(5);
//        }
        
        String fileName = IN_FILES[i].getName();
        fileName = fileName.substring(0, fileName.length() - 4) + "tex";
        opt.result(BENCH_DIR + fileName);
        new Runner(opt.build()).run();
    }
    
    /**
     * Benchmarks all files in the input directory.
     * 
     * @throws RunnerException If the runner threw an exception.
     */
    @SuppressWarnings({"ConstantConditions", "unused"})
    public static void benchmark()
            throws RunnerException {
        ChainedOptionsBuilder opt = new OptionsBuilder()
//                .include(Benchmarking.class.getSimpleName())
//                .shouldDoGC(true)
                .resultFormat(ResultFormatType.LATEX)
                // Warmup
//                .warmupTime(TimeValue.milliseconds(5_000))
//                .warmupMode(WarmupMode.INDI)
//                .warmupBatchSize(1)
//                .warmupIterations(1)
//                // Measurement
//                .measurementTime(TimeValue.milliseconds(1_000))
//                .measurementBatchSize(1)
//                .measurementIterations(1)
                ;
        
//        int amt = IN_FILES.length;
//        for (int i = 0; i < amt; i++) {
//            runFile(opt, i);
//        }
        runFile(opt, RUN);
    }
    
    /**
     * Runs all files in the input directory.
     */
    @SuppressWarnings({"rawtypes", "ConstantConditions", "unused"})
    public static void runAll() {
        Class<? extends Range2DSearch> searchClass = KDTree.class;
        for (int i = 0; i < IN_FILES.length; i++) {
            Logger.setDefaultLogger(NullLogger.getInstance());
            Pair<Problem2, File> p = loadProblem(i);
            try {
                Collection<OutputEdge> sol = new ConvexLayersOptimized(searchClass)
                        .solve(p.getFirst(), VIS);
                ProblemIO.saveSolution(p.getSecond(), sol, p.getFirst());

            } catch (Exception e) {
                Logger.setDefaultLogger(ERROR_LOGGER);
                Logger.write(e);
                Logger.setDefaultLogger(NullLogger.getInstance());
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
