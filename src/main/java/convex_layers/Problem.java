package convex_layers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class Problem {
    String name;
    Set<InputVertex> vertices;
}
