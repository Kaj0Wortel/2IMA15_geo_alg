package convex_layers.evaluate;


import convex_layers.BaseInputVertex;
import convex_layers.Problem2;
import convex_layers.Solver;
import convex_layers.data.IgnoreRangeSearch;
import convex_layers.data.Range2DSearch;
import convex_layers.data.kd_tree.KDTree;
import convex_layers.data.prior_tree.PriorTreeSearch;
import convex_layers.data.quad_tree.QuadTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import tools.MultiTool;
import tools.Pair;
import tools.Var;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
//@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})//, warmups = 2)
//@Warmup(iterations = 3)
//@Measurement(iterations = 8)
@Warmup(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
public class Benchmarking {

    @SuppressWarnings("unchecked")
    private static final Class<Range2DSearch<BaseInputVertex>>[] RANGE_SEARCH_CLASSES = new Class[] {
            KDTree.class,
            PriorTreeSearch.class,
            QuadTree.class,
            //IgnoreRangeSearch.class
    };
    
    @SuppressWarnings("deprecation")
    private static final File SOURCE_FOLDER = new File(Var.WORKING_DIR + "data" + Var.FS + "challenge_1"
            + Var.FS + "uniform");
    
    
    private static List<Solver> solver;
    private static List<Pair<Problem2, File>> problems;
    
    
    @Setup
    public void setup() {/*
        File[] files = SOURCE_FOLDER.listFiles();
        if (files == null) throw new IllegalStateException("The folder " + SOURCE_FOLDER + " does not exists!");
        for (File file : files) {
            
            
            
            for (Class<Range2DSearch<BaseInputVertex>> search : searches) {
                for (int i = 0; i < 1; i ++) {
                    System.out.println("Hi!");

                    Pair<Problem2, File> prob = getProblem("uniform", name);
                    Problem2 problem = prob.getFirst();
                    File outFile = prob.getSecond();

                    System.out.println("Running");

                    //RunProperties properties = evaluate(problem, search, outFile);
                    System.out.println("Errors: " + properties.hasErrors());
                    if (properties.hasErrors()) {
                        System.out.println("Seed of error: " + properties.seed);
                        return;
                    }
                }
            }
        }*/
    }
    
    @Benchmark
    public void benchmark() {
        MultiTool.sleepThread(100);
    }
    
    private void execute(Class<Range2DSearch<BaseInputVertex>> c) {
        
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmarking.class.getSimpleName())
                .shouldDoGC(true)
                .forks(1)
                .build();
        
        new Runner(opt).run();
    }
    
    
}
