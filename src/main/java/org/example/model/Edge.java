package org.example.model;

/**
 * Клас Edge представляє одне зважене ребро з початком u, кінцем v і вагою weight.
 */
public class Edge {
    private final int u;
    private final int v;
    private final int weight;

    public Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    public int getU() {
        return u;
    }

    public int getV() {
        return v;
    }

    public int getWeight() {
        return weight;
    }
}
