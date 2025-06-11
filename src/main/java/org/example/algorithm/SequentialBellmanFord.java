package org.example.algorithm;

import org.example.model.Edge;
import org.example.model.Graph;

import java.util.Arrays;

/**
 * Послідовна (одно­потокова) реалізація алгоритму Беллмана–Форда з перевіркою на від’ємні цикли.
 */
public class SequentialBellmanFord implements IShortestPaths {

    @Override
    public int[] findShortestPaths(Graph graph, int source) {
        int n = graph.getVertexCount();
        int[] dist = new int[n];

        // Ініціалізуємо всі відстані = нескінченність (Integer.MAX_VALUE),
        // крім початкової вершини source
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        // Основний цикл релаксацій (n-1 ітерацій)
        for (int i = 0; i < n - 1; i++) {
            boolean updated = false;
            for (Edge e : graph.getEdges()) {
                int u = e.getU();
                int v = e.getV();
                int w = e.getWeight();
                if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    updated = true;
                }
            }
            // Якщо за ітерацію не було жодного оновлення – виходимо раніше
            if (!updated) {
                break;
            }
        }

        // Перевірка на від’ємні цикли: якщо ще можна зменшити dist[], значить цикл є
        for (Edge e : graph.getEdges()) {
            int u = e.getU();
            int v = e.getV();
            int w = e.getWeight();
            if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
                // Виявлено від’ємний цикл
                return null;
            }
        }

        return dist;
    }
}
