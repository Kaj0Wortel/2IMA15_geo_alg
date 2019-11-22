@Data
package convex_layers;

public class Edge {
    public Edge(Vertex v1, Vertex v2){
        this.v1 = v1;
        this.v2 = v2;
    }
    Vertex v1;
    Vertex v2;
}
