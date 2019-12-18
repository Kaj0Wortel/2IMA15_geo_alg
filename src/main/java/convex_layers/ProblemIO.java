package convex_layers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import tools.log.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Static tool class for reading a problem from a file and writing a
 * solution back to a file.
 */
public final class ProblemIO {
    
    /**
     * Private constructor to prevent creation of an instance of a static tool class.
     */
    private ProblemIO() {
    }
    
    
    /**
     * Reads the problem from a file.
     * 
     * @param file The file to read the problem from.
     * 
     * @return The problem set.
     */
    public static Problem2 readProblem(File file) {
        Logger.write("Started reading " + file.getAbsolutePath());
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            return readProblem(bis);
            
        } catch (IOException e) {
            Logger.write(e);
            System.exit(-1);
            return null;
            
        } finally {
            Logger.write("Finished reading " + file.getAbsolutePath());
        }
    }

    /**
     * Reads a problem from the given input stream.
     * 
     * @param in The input stream used to read the problem.
     * 
     * @return The problem set.
     */
    public static Problem2 readProblem(InputStream in) {
        JSONObject json = new JSONObject(new JSONTokener(in));
        Collection<BaseInputVertex> vertices = new ArrayList<>();
        String name = json.getString("name");
        JSONArray points = json.getJSONArray("points");
        for (int i = 0; i < points.length(); i++) {
            JSONObject point = points.getJSONObject(i);
            int id = point.getInt("i");
            double x = point.getDouble("x");
            double y = point.getDouble("y");
            Logger.write("id: " + id + ", x: " + x + ", y : " + y);
            vertices.add(new BaseInputVertex(id, x, y));
        }

        return new Problem2(name, vertices);
    }
    
    /**
     * Saves the solution of a problem to a file.
     * 
     * @param file    The file to save the problem at.
     * @param sol     The solution of the problem.
     * @param problem The initial problem.
     */
    public static void saveSolution(File file, Collection<OutputEdge> sol, Problem2 problem) {
        Logger.write("Saving solution of " + problem.getName() + " having " + sol.size()
                + " edges to  the file " + file.getAbsolutePath());
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false))) {
            saveSolution(bos, sol, problem);
            
        } catch (IOException e) {
            Logger.write(e);
            System.exit(-1);
            
        } finally {
            Logger.write("Finished saving solution of " + problem.getName());
        }
    }
    
    /**
     * Outputs a JSON for the given solution to the given output stream.
     *
     * @param out     The output stream used to output the solution to.
     * @param sol     The solution of the problem.
     * @param problem The initial problem.
     */
    public static void saveSolution(OutputStream out, Collection<OutputEdge> sol, Problem2 problem)
                throws IOException {
        JSONObject json = new JSONObject();
        json.put("type", "Solution");
        json.put("instance_name", problem.getName());
        JSONObject meta = new JSONObject();
        meta.put("comment", "This is generated by the messy algorithm");
        json.put("meta", meta);

        JSONArray edges = new JSONArray();
        for (OutputEdge edge : sol) {
            JSONObject e = new JSONObject();
            e.put("i", edge.getV1().getId());
            e.put("j", edge.getV2().getId());
            edges.put(e);
        }
        json.put("edgs", edges);
        out.write(json.toString().getBytes());
    }
    
    
}