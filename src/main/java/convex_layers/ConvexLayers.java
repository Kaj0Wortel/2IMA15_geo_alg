package convex_layers;

import convex_layers.linked_list.DoublyLinkedList;
import tools.Var;
import tools.log.FileLogger;
import tools.log.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main project class. <br>
 * This class delegates the work for solving the problem.
 */
public class ConvexLayers {
    
    
    /** The input file */
    private final File input;
    /** The output file. */
    private final File output;
    //Set that keeps track of all variables that still need to be checked.
    private Set<Vertex> remaining = new HashSet<Vertex>();
    //Set containing the edges of the resulting convex partitioning
    private Set<Edge> sol = new HashSet<Edge>();
    //Linked list of the inner convex hull
    private DoublyLinkedList<Vertex> in;
    //Linked list of the outer convex hull
    private DoublyLinkedList<Vertex> out;

    /**
     * Creates a new convex layer class, which solves the convex decomposition problem
     * for the given input and outputs the result to the given output file.
     * 
     * @param input The input file.
     * @param output The output file.
     */
    public ConvexLayers(File input, File output) {
        this.input = input;
        this.output = output;
    }
    
    /**
     * This function is used to compute the output file.
     */
    public synchronized void compute() {
        readInput();
        // TODO: all;
        writeOutput();
    }
    
    /**
     * Responsibility Pieter
     */
    private void giftWrapping() {
        
    }
    
    /**
     * Responsibility Kaj
     */
    private void updateHull() {
        
    }
    
    /**
     * Responsibility, Rowin
     */
    private void readInput() {
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                // TODO: process line.
            }
        } catch  (IOException e) {
            Logger.write(e);
        }
    }
    
    /**
     * Responsibility, Stijn
     */
    private void writeOutput() {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            while (true) { // TODO: condition
                pw.println();// TODO: process line.
            }
        } catch  (IOException e) {
            Logger.write(e);
        }
    }
    
    /**
     * Responsibility, Stijn, Pieter
     */
    private void computeIntersection() {
        
    }
    
    private static void initLogger() {
        try {
            Logger.setDefaultLogger(new FileLogger(Var.LOG_FILE));
            
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }
    
    /**
     * The main function used to initialize the program.
     * 
     * @param args The runtime arguments.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!"); // TODO
        initLogger();
        if (args == null || args.length < 2) {
            Logger.write("Expected 2 arguments.", Logger.Type.ERROR);
            System.exit(1);
        }
        ConvexLayers cl = new ConvexLayers(new File(args[0]), new File(args[1]));
        cl.compute();
    }
    
    
}
