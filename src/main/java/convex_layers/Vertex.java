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

    long id;
    double x;
    double y;
    boolean hulled = false;
    Vertex prev;
    Vertex next;

    public Vertex clone(Vertex v) {
        return new Vertex(id, x, y);
    }

    public Vertex add(Vertex v) {
        return new Vertex(-1, x + v.x, y + v.y);
    }

    public Vertex sub(Vertex v) {
        return new Vertex(-1, x - v.x, y - v.y);
    }
}
