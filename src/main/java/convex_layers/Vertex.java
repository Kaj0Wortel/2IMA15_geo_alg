package convex_layers;

import lombok.Data;
import lombok.Getter;

/**
 * class representing a point in the plain
 */
@Data
public class Vertex {
    public Vertex(long id, double x, double y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

    private final long id;
    private double x;
    private double y;
    
    
}
