package org.example.ui;

import org.example.algorithm.ParallelBellmanFord;
import org.example.algorithm.SequentialBellmanFord;
import org.example.model.Graph;
import org.example.ui.GraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

/**
 * Головне вікно програми з табовим інтерфейсом для різних функцій:
 * - Редактор графів (ручне створення)
 * - Візуалізація алгоритму (покрокове виконання) 
 * - Бенчмарки (порівняльні діаграми)
 * - Простий запуск (оригінальна функціональність)
 */
public class MainWindow extends JFrame {
    private JTabbedPane tabbedPane;
    
    // Панелі для різних функцій
    private JPanel graphEditorPanel;
    private JPanel algorithmVisualizationPanel;
    private JPanel benchmarkPanel;
    private JPanel simpleBenchmarkPanel;
    
    // Компоненти для простого бенчмарку (оригінальна функціональність)
    private JTextArea outputArea;
    private JButton runButton;

    public MainWindow() {
        super("Bellman-Ford Algorithm Suite");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        
        // 1. Панель редактора графів
        createGraphEditorPanel();
        
        // 2. Панель візуалізації алгоритму
        createAlgorithmVisualizationPanel();
        
        // 3. Панель бенчмарків
        createBenchmarkPanel();
        
        // 4. Панель простого запуску (оригінальна)
        createSimpleBenchmarkPanel();
    }
    
    private void createGraphEditorPanel() {
        graphEditorPanel = new JPanel(new BorderLayout());
        
        // Заголовок
        JLabel titleLabel = new JLabel("Інтерактивний редактор графів", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Центральна область для малювання графа
        GraphPanel drawingArea = new GraphPanel();
        drawingArea.setBackground(Color.WHITE);
        drawingArea.setBorder(BorderFactory.createTitledBorder("Область графа"));
        drawingArea.setPreferredSize(new Dimension(600, 400));
        
        // Панель інструментів
        JPanel toolPanel = createGraphEditorToolPanel();
        
        // Інструкції
        JTextArea instructions = new JTextArea(
            "Інструкції:\n" +
            "• ЛКМ - додати вершину\n" +
            "• ПКМ - видалити вершину/ребро\n" +
            "• Перетягування - з'єднати вершини\n" +
            "• Подвійний клік - встановити як джерело"
        );
        instructions.setEditable(false);
        instructions.setBackground(getBackground());
        instructions.setBorder(BorderFactory.createTitledBorder("Керування"));
        
        graphEditorPanel.add(titleLabel, BorderLayout.NORTH);
        graphEditorPanel.add(drawingArea, BorderLayout.CENTER);
        graphEditorPanel.add(toolPanel, BorderLayout.SOUTH);
        graphEditorPanel.add(instructions, BorderLayout.EAST);
    }
    
    private JPanel createGraphEditorToolPanel() {
        JPanel toolPanel = new JPanel(new FlowLayout());
        
        JButton clearButton = new JButton("Очистити");
        JButton randomButton = new JButton("Випадковий граф");
        JButton loadExampleButton = new JButton("Приклад");
        JButton exportButton = new JButton("Експорт");
        
        // Налаштування графа
        JLabel verticesLabel = new JLabel("Вершин:");
        JSpinner verticesSpinner = new JSpinner(new SpinnerNumberModel(5, 3, 20, 1));
        
        JLabel edgesLabel = new JLabel("Ребер:");
        JSpinner edgesSpinner = new JSpinner(new SpinnerNumberModel(8, 3, 50, 1));
        
        toolPanel.add(clearButton);
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(verticesLabel);
        toolPanel.add(verticesSpinner);
        toolPanel.add(edgesLabel);
        toolPanel.add(edgesSpinner);
        toolPanel.add(randomButton);
        toolPanel.add(loadExampleButton);
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(exportButton);
        
        return toolPanel;
    }
    
    private void createAlgorithmVisualizationPanel() {
        algorithmVisualizationPanel = new JPanel(new BorderLayout());
        
        // Заголовок
        JLabel titleLabel = new JLabel("Покрокова візуалізація Bellman-Ford", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Область візуалізації
        JPanel visualizationArea = new JPanel();
        visualizationArea.setBackground(Color.WHITE);
        visualizationArea.setBorder(BorderFactory.createTitledBorder("Візуалізація алгоритму"));
        visualizationArea.setPreferredSize(new Dimension(600, 400));
        
        // Панель управління
        JPanel controlPanel = createVisualizationControlPanel();
        
        // Інформаційна панель
        JPanel infoPanel = createVisualizationInfoPanel();
        
        algorithmVisualizationPanel.add(titleLabel, BorderLayout.NORTH);
        algorithmVisualizationPanel.add(visualizationArea, BorderLayout.CENTER);
        algorithmVisualizationPanel.add(controlPanel, BorderLayout.SOUTH);
        algorithmVisualizationPanel.add(infoPanel, BorderLayout.EAST);
    }
    
    private JPanel createVisualizationControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JButton startButton = new JButton("▶ Старт");
        JButton pauseButton = new JButton("⏸ Пауза");
        JButton stepButton = new JButton("⏭ Крок");
        JButton resetButton = new JButton("🔄 Скинути");
        
        JLabel speedLabel = new JLabel("Швидкість:");
        JSlider speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setPaintTicks(true);
        
        JCheckBox parallelCheckBox = new JCheckBox("Паралельний режим");
        
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stepButton);
        controlPanel.add(resetButton);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(speedLabel);
        controlPanel.add(speedSlider);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(parallelCheckBox);
        
        return controlPanel;
    }
    
    private JPanel createVisualizationInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(250, 0));
        
        // Таблиця відстаней
        String[] columnNames = {"Вершина", "Відстань", "Попередник"};
        Object[][] data = new Object[0][3];
        JTable distanceTable = new JTable(data, columnNames);
        JScrollPane tableScrollPane = new JScrollPane(distanceTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Відстані"));
        
        // Інформація про поточну ітерацію
        JTextArea iterationInfo = new JTextArea(8, 20);
        iterationInfo.setEditable(false);
        iterationInfo.setText("Ітерація: 0\nОброблено ребер: 0\nОновлень: 0\n\nОчікування старту...");
        JScrollPane infoScrollPane = new JScrollPane(iterationInfo);
        infoScrollPane.setBorder(BorderFactory.createTitledBorder("Статус"));
        
        infoPanel.add(tableScrollPane, BorderLayout.CENTER);
        infoPanel.add(infoScrollPane, BorderLayout.SOUTH);
        
        return infoPanel;
    }
    
    private void createBenchmarkPanel() {
        benchmarkPanel = new JPanel(new BorderLayout());
        
        // Заголовок
        JLabel titleLabel = new JLabel("Порівняльний аналіз продуктивності", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Область для графіків
        JPanel chartsArea = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Заглушки для графіків (поки що)
        JPanel chart1 = createChartPlaceholder("Час виконання vs Кількість потоків");
        JPanel chart2 = createChartPlaceholder("Час виконання vs Розмір графа");
        JPanel chart3 = createChartPlaceholder("Прискорення (Speedup)");
        JPanel chart4 = createChartPlaceholder("Ефективність");
        
        chartsArea.add(chart1);
        chartsArea.add(chart2);
        chartsArea.add(chart3);
        chartsArea.add(chart4);
        
        // Панель налаштувань бенчмарку
        JPanel benchmarkControlPanel = createBenchmarkControlPanel();
        
        benchmarkPanel.add(titleLabel, BorderLayout.NORTH);
        benchmarkPanel.add(chartsArea, BorderLayout.CENTER);
        benchmarkPanel.add(benchmarkControlPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createChartPlaceholder(String title) {
        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBorder(BorderFactory.createTitledBorder(title));
        placeholder.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("Графік буде тут", JLabel.CENTER);
        label.setForeground(Color.GRAY);
        placeholder.add(label, BorderLayout.CENTER);
        
        return placeholder;
    }
    
    private JPanel createBenchmarkControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JLabel verticesLabel = new JLabel("Діапазон вершин:");
        JSpinner minVerticesSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 50));
        JLabel toLabel1 = new JLabel("до");
        JSpinner maxVerticesSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        
        JLabel threadsLabel = new JLabel("Макс. потоків:");
        JSpinner maxThreadsSpinner = new JSpinner(new SpinnerNumberModel(
            Runtime.getRuntime().availableProcessors(), 1, 16, 1));
        
        JLabel runsLabel = new JLabel("Запусків:");
        JSpinner runsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        
        JButton runBenchmarkButton = new JButton("🚀 Запустити бенчмарк");
        runBenchmarkButton.setBackground(new Color(0, 150, 0));
        runBenchmarkButton.setForeground(Color.WHITE);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        controlPanel.add(verticesLabel);
        controlPanel.add(minVerticesSpinner);
        controlPanel.add(toLabel1);
        controlPanel.add(maxVerticesSpinner);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(threadsLabel);
        controlPanel.add(maxThreadsSpinner);
        controlPanel.add(runsLabel);
        controlPanel.add(runsSpinner);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(runBenchmarkButton);
        controlPanel.add(progressBar);
        
        return controlPanel;
    }
    
    private void createSimpleBenchmarkPanel() {
        simpleBenchmarkPanel = new JPanel(new BorderLayout());
        
        // Заголовок
        JLabel titleLabel = new JLabel("Простий запуск (оригінальна функціональність)", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Оригінальні компоненти
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        
        runButton = new JButton("Запустити бенчмарк");
        runButton.addActionListener(this::onRun);
        
        // Панель з налаштуваннями
        JPanel settingsPanel = new JPanel(new FlowLayout());
        settingsPanel.add(new JLabel("Вершин:"));
        JSpinner verticesSpinner = new JSpinner(new SpinnerNumberModel(500, 10, 2000, 50));
        settingsPanel.add(verticesSpinner);
        settingsPanel.add(new JLabel("Ребер:"));
        JSpinner edgesSpinner = new JSpinner(new SpinnerNumberModel(2000, 10, 10000, 100));
        settingsPanel.add(edgesSpinner);
        settingsPanel.add(runButton);
        
        simpleBenchmarkPanel.add(titleLabel, BorderLayout.NORTH);
        simpleBenchmarkPanel.add(scrollPane, BorderLayout.CENTER);
        simpleBenchmarkPanel.add(settingsPanel, BorderLayout.SOUTH);
    }
    
    private void setupLayout() {
        // Додаємо всі панелі до табів
        tabbedPane.addTab("📝 Редактор", graphEditorPanel);
        tabbedPane.addTab("🎬 Візуалізація", algorithmVisualizationPanel);
        tabbedPane.addTab("📊 Бенчмарки", benchmarkPanel);
        tabbedPane.addTab("⚡ Простий запуск", simpleBenchmarkPanel);
        
        // Встановлюємо іконки для табів (опціонально)
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Статус-бар
        JLabel statusBar = new JLabel("Готово до роботи");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
    
    // Оригінальна функціональність для простого запуску
    private void onRun(ActionEvent e) {
        outputArea.setText("");  // Очистимо текстову область

        int n = 500;   // Для GUI берімо помірний розмір
        int m = 2000;
        Graph graph = new Graph(n);
        Random rnd = new Random();

        // Генеруємо випадковий граф із можливими від'ємними вагами
        for (int i = 0; i < m; i++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            int w = rnd.nextInt(100) - 50; // тепер можуть бути негативні ребра
            graph.addEdge(u, v, w);
        }

        int source = 0;

        // 1) Перевірка на негативний цикл через будь-яку із версій
        outputArea.append("Checking negative cycle (sequential)…\n");
        SequentialBellmanFord seqCheck = new SequentialBellmanFord();
        int[] distCheck = seqCheck.findShortestPaths(graph, source);
        if (distCheck == null) {
            outputArea.append("Negative cycle detected (sequential). Aborting benchmark.\n");
            return;
        }

        outputArea.append("Checking negative cycle (parallel)…\n");
        ParallelBellmanFord parCheck = new ParallelBellmanFord();
        int[] distCheckPar = parCheck.findShortestPaths(graph, source);
        if (distCheckPar == null) {
            outputArea.append("Negative cycle detected (parallel). Aborting benchmark.\n");
            return;
        }

        // Якщо дійшли сюди — у графі немає негативних циклів
        outputArea.append("Running Sequential Bellman-Ford…\n");
        long t1 = System.currentTimeMillis();
        int[] distSeq = seqCheck.findShortestPaths(graph, source);
        long t2 = System.currentTimeMillis();
        outputArea.append("Sequential time: " + (t2 - t1) + " ms\n");

        outputArea.append("Running Parallel Bellman-Ford…\n");
        long t3 = System.currentTimeMillis();
        int[] distPar = parCheck.findShortestPaths(graph, source);
        long t4 = System.currentTimeMillis();
        outputArea.append("Parallel time: " + (t4 - t3) + " ms\n");

        // Перевіримо, чи результати однакові
        boolean equal = true;
        for (int i = 0; i < n; i++) {
            int dSeq = distSeq[i];
            int dPar = distPar[i];
            if (!((dSeq == Integer.MAX_VALUE && dPar == Integer.MAX_VALUE) || (dSeq == dPar))) {
                equal = false;
                outputArea.append(String.format(
                        "Mismatch at vertex %d: sequential=%s, parallel=%s%n",
                        i,
                        (dSeq == Integer.MAX_VALUE ? "INF" : dSeq),
                        (dPar == Integer.MAX_VALUE ? "INF" : dPar)
                ));
                break;
            }
        }
        outputArea.append("Results equal: " + equal + "\n");
    }

    public static void main(String[] args) {
        // Встановлюємо Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Використовуємо стандартний L&F
        }
        
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}