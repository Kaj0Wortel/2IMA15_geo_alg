# Convex partition generator

This package can generate a 3-approximation for the minimum convex partitioning problem in expected `O(n log(n))`, and worst case `O(n^2)`.



## Usage

Run the file `convex_layers.ConvexLayersOptimized` to execute the program.  One can externally replicate the setup and modify what to run. An example has been given in the `main(String[] args)` function in the same main class.

There are a some markers in this function indicating what each part does. We'll explain this here in more detail.

### Input file

This part determines the input file. Note that currently the output file is also dependent on the input file.

### Setup visualizer

This part initializes the visualizer of the problem. By default, it uses a visualizer which stores the images as a `BufferedImage` to allow viewing previous images. Note that is method is extremely slow and requires a lot of memory, especially in large cases. Take a look at the `convex_layers.visual` package for other visualizers.

### Setup solver

This part initializes the solver used to solve the problem. The class used for the 2D range search is given here as an argument to allow code injection. Take a look at the `convex_layers.data` package for other 2D range search classes.

### Setup checker

This part initializes the checker used to check the solution. Take a look at the `convex_layers.checker` for more checkers. Note that the `ConvexChecker` class is very fast (expected `O(|V|log(|V|)+|E|)`, worst `O(|V|log(|V|)+|E|log(|E|))`),  however, the `FastEdgeInterSectionChecker` is very slow (best `O(|E| log(|E|))`, `worst/expected `O(|E|^2)`). This is due to the fact that it uses an already available linked hash set instead of a specialized heap as storage for the line sweep algorithm for intersection checking.

### Generate solution

This part generates the solution using the current settings.

### Check quality

This part checks the quality of the solution. Note that the quality checking is done before the correctness. This is because the correctness takes a lot longer to finish. Take a look at the `convex_layers.evaluate` package for more information.

### Check correctness

This part checks the correctness of the solution. By default, we check small instances on both convexness of the points and edge intersections, but large instances only for convexness. The output the checker is a `CheckerError`, which can be passed to a visualizer to show which edges are wrong, or printed to the console. The default visualizer for this is the `VisualRenderer`, because this visualizer allows scaling and translations. Take a look at the `convex_layers.checker` package for more information.

### Save solution

This statement saves the solution to the output file.



## Application versions

- **Editor**: IntelliJ IDEA version 2019.3.1 Ultimate Edition
- **Java**: 11
- **Maven**: 4.0.0