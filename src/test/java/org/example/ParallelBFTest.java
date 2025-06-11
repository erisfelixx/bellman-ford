package org.example;

import org.example.algorithm.ParallelBellmanFord;
import org.example.model.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParallelBFTest {

    @Test
    void testSimpleGraph() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 2, 8);
        graph.addEdge(1, 3, 2);
        graph.addEdge(2, 3, -4);
        graph.addEdge(3, 4, 1);

        ParallelBellmanFord bf = new ParallelBellmanFord();
        int[] dist = bf.findShortestPaths(graph, 0);

        assertNotNull(dist);
        assertEquals(0, dist[0], "Wrong distance to vertex 0");
        assertEquals(3, dist[1], "Wrong distance to vertex 1");
        assertEquals(8, dist[2], "Wrong distance to vertex 2");
        // Правильна найкоротша відстань до 3 = 4
        assertEquals(4, dist[3], "Wrong distance to vertex 3");
        // Правильна найкоротша відстань до 4 = 5
        assertEquals(5, dist[4], "Wrong distance to vertex 4");
    }

    @Test
    void testDisconnectedGraph() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 5);
        // Вершина 2 від’єднана

        int[] dist = new ParallelBellmanFord().findShortestPaths(graph, 0);

        assertNotNull(dist);
        assertEquals(0, dist[0], "Wrong distance to vertex 0");
        assertEquals(5, dist[1], "Wrong distance to vertex 1");
        assertEquals(Integer.MAX_VALUE, dist[2], "Vertex 2 має бути недосяжним");
    }

    @Test
    void testNegativeCycle() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, -5); // від’ємний цикл 0→1→2→0

        int[] dist = new ParallelBellmanFord().findShortestPaths(graph, 0);
        // Оскільки є від’ємний цикл, метод має повернути null
        assertNull(dist);
    }
}
