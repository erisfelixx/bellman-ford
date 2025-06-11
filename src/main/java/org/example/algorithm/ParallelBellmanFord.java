package org.example.algorithm;

import org.example.model.Edge;
import org.example.model.Graph;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Паралельна реалізація Bellman-Ford із використанням Java-потоків та перевіркою на від’ємні цикли.
 */
public class ParallelBellmanFord implements IShortestPaths {

    @Override
    public int[] findShortestPaths(Graph graph, int source) {
        int n = graph.getVertexCount();
        List<Edge> edges = graph.getEdges();
        int m = edges.size();

        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[source] = 0;

        int[] distNext = new int[n];
        int threads = Runtime.getRuntime().availableProcessors();
        Thread[] pool = new Thread[threads];

        for (int iter = 0; iter < n - 1; iter++) {
            // Копіюємо поточні відстані в distNext
            System.arraycopy(dist, 0, distNext, 0, n);

            AtomicBoolean updated = new AtomicBoolean(false);
            int chunkSize = (m + threads - 1) / threads;

            for (int t = 0; t < threads; t++) {
                final int start = t * chunkSize;
                final int end = Math.min(start + chunkSize, m);

                pool[t] = new Thread(() -> {
                    for (int i = start; i < end; i++) {
                        Edge e = edges.get(i);
                        int u = e.getU();
                        int v = e.getV();
                        int w = e.getWeight();

                        int du = dist[u];
                        if (du != Integer.MAX_VALUE) {
                            int newDist = du + w;
                            synchronized (distNext) {
                                if (newDist < distNext[v]) {
                                    distNext[v] = newDist;
                                    updated.set(true);
                                }
                            }
                        }
                    }
                });
                pool[t].start();
            }

            // Чекаємо, поки всі потоки завершаться
            for (int t = 0; t < threads; t++) {
                try {
                    pool[t].join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Якщо за цю ітерацію не було жодного оновлення – виходимо
            if (!updated.get()) {
                break;
            }

            // Копіюємо distNext у dist для наступної ітерації
            System.arraycopy(distNext, 0, dist, 0, n);
        }

        // Перевірка на від’ємні цикли
        for (Edge e : edges) {
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
