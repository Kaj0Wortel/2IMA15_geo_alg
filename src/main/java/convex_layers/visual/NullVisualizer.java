package convex_layers.visual;

import convex_layers.BaseInputVertex;
import convex_layers.math.Edge;
import convex_layers.math.Vector;

import java.awt.*;
import java.util.List;

/**
 * Visualiser implementation which simply ignores all draw commands.
 */
public class NullVisualizer
        implements Visual {
    
    @Override
    public void redraw() {
    }
    
    @Override
    public void setPoints(List<Iterable<Vector>> points) {
    }
    
    @Override
    public void addPoint(Iterable<Vector> pointList) {
    }
    
    @Override
    public void setPointColors(List<Paint> pointColors) {
    }
    
    @Override
    public void setEdges(List<Iterable<Edge>> edges) {
    }
    
    @Override
    public void addEdge(Iterable<Edge> edgeList) {
    }
    
    @Override
    public void setEdgeColors(List<Paint> edgeColors) {
    }
    
    @Override
    public void setLabels(List<Iterable<String>> labelList) {
    }
    
    @Override
    public void addLabel(Iterable<String> labelList) {
    }
    
    @Override
    public void setData(List<Iterable<? extends BaseInputVertex>> data) {
    }
    
    @Override
    public void addData(List<Iterable<? extends BaseInputVertex>> data) {
    }
    
    @Override
    public void clear() {
    }
    
    @Override
    public void clearAll() {
    }
    
    
}
