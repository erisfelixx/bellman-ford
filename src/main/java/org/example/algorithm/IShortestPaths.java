package org.example.algorithm;

import org.example.model.Graph;

/**
 * Інтерфейс для алгоритму пошуку найкоротших шляхів.
 * Метод findShortestPaths повертає масив відстаней від джерела source
 * до всіх інших вершин графа; якщо шляхів немає – Integer.MAX_VALUE.
 * Якщо знайдений від’ємний цикл, повертає null.
 */
public interface IShortestPaths {
    /**
     * Обчислити найкоротші відстані у графі graph від вершини source до всіх інших.
     *
     * @param graph  екземпляр Graph
     * @param source номер початкової вершини (0..n-1)
     * @return масив dist[], де dist[v] – мінімальна відстань від source до v,
     *         або Integer.MAX_VALUE, якщо v недосяжна. Якщо в графі є від’ємний цикл,
     *         повертає null.
     */
    int[] findShortestPaths(Graph graph, int source);
}
