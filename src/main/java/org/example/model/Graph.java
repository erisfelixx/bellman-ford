package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Клас Graph представляє орієнтований зважений граф.
 * Вершини нумеруємо від 0 до n-1.
 */
public class Graph {
    private final int n;               // кількість вершин
    private final List<Edge> edges;    // список усіх ребер

    public Graph(int n) {
        this.n = n;
        this.edges = new ArrayList<>();
    }

    /** Повертає кількість вершин */
    public int getVertexCount() {
        return n;
    }

    /** Додає ребро u -> v з вагою weight */
    public void addEdge(int u, int v, int weight) {
        edges.add(new Edge(u, v, weight));
    }

    /** Повертає список усіх ребер */
    public List<Edge> getEdges() {
        return edges;
    }
}
