package convex_layers;

/**
 * class representing a point in the plain
 */

public class Vertex {
    public Vertex(long id, double x, double y){
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Getter long id;
    @Data double x;
    @Data double y;
}
