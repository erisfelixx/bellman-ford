package org.example;

import org.example.algorithm.ParallelBellmanFord;
import org.example.algorithm.SequentialBellmanFord;
import org.example.model.Graph;

import java.util.Random;

/**
 * Клас BFBenchmark порівнює час виконання послідовної та паралельної версій Bellman-Ford.
 * Тепер ребра мають лише невід’ємні ваги, тому від’ємних циклів не виникає.
 */
public class BFBenchmark {
    public static void main(String[] args) {
        int n = 1000;   // кількість вершин
        int m = 5000;   // кількість ребер
        Graph graph = new Graph(n);
        Random rnd = new Random();

        // Генеруємо випадковий зважений граф, але з НЕВІД’ЄМНИМИ вагами
        for (int i = 0; i < m; i++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            int w = rnd.nextInt(100); // ваги від 0 до 99 (від’ємних немає)
            graph.addEdge(u, v, w);
        }

        int source = 0;

        // Послідовна версія
        SequentialBellmanFord seq = new SequentialBellmanFord();
        long startSeq = System.currentTimeMillis();
        int[] distSeq = seq.findShortestPaths(graph, source);
        long endSeq = System.currentTimeMillis();
        System.out.println("Sequential time: " + (endSeq - startSeq) + " ms");

        // Паралельна версія
        ParallelBellmanFord par = new ParallelBellmanFord();
        long startPar = System.currentTimeMillis();
        int[] distPar = par.findShortestPaths(graph, source);
        long endPar = System.currentTimeMillis();
        System.out.println("Parallel time: " + (endPar - startPar) + " ms");

        // Тепер обидва масиви гарантовано НЕ дорівнюють null, оскільки немає від’ємного циклу
        // (усі ребра — невід’ємні)
        if (distSeq == null || distPar == null) {
            // Цей блок ніколи не виконається
            System.err.println("Negative cycle detected in the graph!");
            return;
        }

        // Порівнюємо результати обох версій
        boolean equal = true;
        for (int i = 0; i < n; i++) {
            int dSeq = distSeq[i];
            int dPar = distPar[i];
            if (!((dSeq == Integer.MAX_VALUE && dPar == Integer.MAX_VALUE) || (dSeq == dPar))) {
                equal = false;
                System.out.printf(
                        "Mismatch at vertex %d: sequential=%s, parallel=%s%n",
                        i,
                        (dSeq == Integer.MAX_VALUE ? "INF" : dSeq),
                        (dPar == Integer.MAX_VALUE ? "INF" : dPar)
                );
                break;
            }
        }
        System.out.println("Results equal: " + equal);
    }
}
