package org.example.ui;

import org.example.algorithm.ParallelBellmanFord;
import org.example.algorithm.SequentialBellmanFord;
import org.example.model.Edge;
import org.example.model.Graph;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Панель для покрокової візуалізації алгоритму Беллмана-Форда
 * з анімацією процесу релаксації ребер та оновлення відстаней.
 */
public class AlgorithmVisualizationPanel extends JPanel {
    // Компоненти інтерфейсу
    private final VisualizationCanvas canvas;
    private final JTable distanceTable;
    private final JTextArea statusArea;
    private final JButton startButton, pauseButton, stepButton, resetButton;
    private final JSlider speedSlider;
    private final JCheckBox parallelModeCheckBox;
    private final JProgressBar progressBar;
    private final Timer animationTimer;

    // Стан візуалізації
    private int state;
    private static class VisualizationState {
        static final int IDLE = 0;
        static final int RUNNING = 1;
        static final int PAUSED = 2;
        static final int FINISHED = 3;
        static final int NEGATIVE_CYCLE = 4;
    }
    private boolean isPaused;

    // Алгоритмічні дані
    private Graph currentGraph;
    private int sourceVertex;
    private int[] distances;
    private int[] previousVertices;
    private boolean[] vertexUpdated;
    private List<Edge> edges;
    private int currentIteration;
    private int currentEdgeIndex;
    private Edge currentEdge;

    public AlgorithmVisualizationPanel() {
        setLayout(new BorderLayout());

        // 1) Ініціалізація компонентів
        canvas              = new VisualizationCanvas();
        String[] cols       = {"Вершина", "Відстань", "Попередник", "Оновлена"};
        distanceTable       = new JTable(new Object[0][4], cols);
        statusArea          = new JTextArea(6, 25);
        startButton         = new JButton("▶ Старт");
        pauseButton         = new JButton("⏸ Пауза");
        stepButton          = new JButton("⏭ Крок");
        resetButton         = new JButton("🔄 Скинути");
        speedSlider         = new JSlider(1, 10, 5);
        parallelModeCheckBox= new JCheckBox("Паралельний режим");
        progressBar         = new JProgressBar();

        // Timer (анімація)
        animationTimer = new Timer(1000, this::onAnimationTick);
        updateAnimationSpeed();

        // 2) Налаштування вигляду
        canvas.setPreferredSize(new Dimension(600, 450));
        canvas.setBackground(Color.WHITE);
        canvas.setBorder(BorderFactory.createLoweredBevelBorder());

        distanceTable.setDefaultEditor(Object.class, null);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        statusArea.setBackground(getBackground());

        pauseButton.setEnabled(false);

        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        progressBar.setStringPainted(true);
        progressBar.setString("Готово до запуску");

        // 3) Макет
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stepButton);
        controlPanel.add(resetButton);
        controlPanel.add(new JLabel("Швидкість:"));
        controlPanel.add(speedSlider);
        controlPanel.add(parallelModeCheckBox);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(300, 0));
        infoPanel.add(new JScrollPane(distanceTable), BorderLayout.CENTER);
        infoPanel.add(new JScrollPane(statusArea), BorderLayout.SOUTH);

        add(progressBar,      BorderLayout.NORTH);
        add(canvas,           BorderLayout.CENTER);
        add(controlPanel,     BorderLayout.SOUTH);
        add(infoPanel,        BorderLayout.EAST);

        // 4) Обробники
        startButton.addActionListener(e -> startVisualization());
        pauseButton.addActionListener(e -> pauseVisualization());
        stepButton.addActionListener(e -> stepVisualization());
        resetButton.addActionListener(e -> resetVisualization());
        speedSlider.addChangeListener(e -> updateAnimationSpeed());

        // 5) Початковий стан
        resetVisualization();
        generateExampleGraph();
    }

    private void generateExampleGraph() {
        currentGraph = new Graph(6);
        currentGraph.addEdge(0,1,7);   currentGraph.addEdge(0,2,9);
        currentGraph.addEdge(0,5,14);  currentGraph.addEdge(1,2,10);
        currentGraph.addEdge(1,3,15);  currentGraph.addEdge(2,3,11);
        currentGraph.addEdge(2,5,2);   currentGraph.addEdge(3,4,6);
        currentGraph.addEdge(4,5,9);
        // currentGraph.addEdge(5,4,-3); // можна додати для демонстрації негативного циклу

        sourceVertex = 0;
        canvas.setGraphData(currentGraph, sourceVertex);
        edges = currentGraph.getEdges();
        updateDistanceTable();
    }

    private void startVisualization() {
        if (state==VisualizationState.IDLE || state==VisualizationState.FINISHED) {
            initializeAlgorithm();
            state = VisualizationState.RUNNING;
            animationTimer.start();
        } else if (state==VisualizationState.PAUSED) {
            state = VisualizationState.RUNNING;
            animationTimer.start();
        }
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        updateStatus("Старт алгоритму...");
    }

    private void pauseVisualization() {
        if (state==VisualizationState.RUNNING) {
            state = VisualizationState.PAUSED;
            animationTimer.stop();
            startButton.setEnabled(true);
            pauseButton.setEnabled(false);
            updateStatus("Пауза");
        }
    }

    private void stepVisualization() {
        if (state==VisualizationState.IDLE || state==VisualizationState.FINISHED) {
            initializeAlgorithm();
            state = VisualizationState.PAUSED;
        }
        if (state==VisualizationState.PAUSED) {
            performSingleStep();
        }
    }

    private void resetVisualization() {
        animationTimer.stop();
        state = VisualizationState.IDLE;
        currentIteration = 0;
        currentEdgeIndex = 0;
        currentEdge = null;

        int n = (currentGraph!=null? currentGraph.getVertexCount() : 0);
        distances = new int[n];
        Arrays.fill(distances, Integer.MAX_VALUE);
        if (n>0) distances[sourceVertex] = 0;

        previousVertices = new int[n];
        Arrays.fill(previousVertices, -1);

        vertexUpdated = new boolean[n];

        startButton.setEnabled(true);
        pauseButton.setEnabled(false);

        progressBar.setValue(0);
        progressBar.setString("Готово до запуску");
        updateStatus("Скинуто");

        updateDistanceTable();
        canvas.setVisualizationData(distances, null, vertexUpdated);
        canvas.repaint();
    }

    private void initializeAlgorithm() {
        int n = currentGraph.getVertexCount();
        distances = new int[n];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[sourceVertex]=0;
        previousVertices = new int[n];
        Arrays.fill(previousVertices, -1);
        vertexUpdated = new boolean[n];
        currentIteration=0;
        currentEdgeIndex=0;
        edges = currentGraph.getEdges();
        updateDistanceTable();
        canvas.setVisualizationData(distances, null, vertexUpdated);
    }

    private void onAnimationTick(ActionEvent e) {
        if (state==VisualizationState.RUNNING) {
            performSingleStep();
        }
    }

    private void performSingleStep() {
        int n = currentGraph.getVertexCount();
        if (currentIteration >= n-1) {
            checkNegativeCycle();
            return;
        }
        if (currentEdgeIndex==0) {
            Arrays.fill(vertexUpdated, false);
            updateStatus("Ітерація "+(currentIteration+1)+"/"+(n-1));
        }
        if (currentEdgeIndex < edges.size()) {
            currentEdge = edges.get(currentEdgeIndex);
            relaxCurrentEdge();
            currentEdgeIndex++;
            int total = (n-1)*edges.size();
            int done  = currentIteration*edges.size()+currentEdgeIndex;
            int prog  = (int)(100.0*done/total);
            progressBar.setValue(prog);
            progressBar.setString("Прогрес: "+prog+"%");
        } else {
            currentIteration++;
            currentEdgeIndex=0;
            currentEdge=null;
        }
        updateDistanceTable();
        canvas.setVisualizationData(distances, currentEdge, vertexUpdated);
        canvas.repaint();
    }

    private void relaxCurrentEdge() {
        Edge e = currentEdge;
        int u=e.getU(), v=e.getV(), w=e.getWeight();
        if (distances[u]!=Integer.MAX_VALUE && distances[u]+w < distances[v]) {
            int old = distances[v];
            distances[v] = distances[u]+w;
            previousVertices[v]=u;
            vertexUpdated[v]=true;
            updateStatus(String.format("Релаксація %d→%d: %d+%d < %s",
                u,v, distances[u],w, old==Integer.MAX_VALUE?"∞":old));
        }
    }

    private void checkNegativeCycle() {
        boolean neg=false;
        for (Edge e: edges) {
            int u=e.getU(), v=e.getV(), w=e.getWeight();
            if (distances[u]!=Integer.MAX_VALUE && distances[u]+w < distances[v]) {
                neg=true; break;
            }
        }
        animationTimer.stop();
        if (neg) {
            state = VisualizationState.NEGATIVE_CYCLE;
            updateStatus("❌ Виявлено негативний цикл!");
            progressBar.setString("Негативний цикл");
        } else {
            state = VisualizationState.FINISHED;
            updateStatus("✅ Завершено успішно");
            progressBar.setValue(100);
            progressBar.setString("Завершено");
        }
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        currentEdge=null;
        canvas.setVisualizationData(distances, currentEdge, vertexUpdated);
        canvas.repaint();
    }

    private void updateDistanceTable() {
        int n = distances!=null? distances.length : 0;
        Object[][] data = new Object[n][4];
        for (int i=0;i<n;i++) {
            data[i][0]=i;
            data[i][1]= distances[i]==Integer.MAX_VALUE?"∞":distances[i];
            data[i][2]= previousVertices[i]<0?"-":previousVertices[i];
            data[i][3]= vertexUpdated[i]?"✓":"";
        }
        distanceTable.setModel(new DefaultTableModel(data,
            new String[]{"Вершина","Відстань","Попередник","Оновлена"}) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        });
        // Відмітити джерело і оновлені
        distanceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,
                    boolean sel,boolean foc,int r,int c) {
                Component cpn = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (r==sourceVertex) {
                    cpn.setBackground(new Color(255,255,200));
                } else if (c==3 && vertexUpdated[r]) {
                    cpn.setBackground(new Color(200,255,200));
                } else if (!sel) {
                    cpn.setBackground(Color.WHITE);
                }
                return cpn;
            }
        });
    }

    private void updateStatus(String msg) {
        statusArea.append(msg+"\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void updateAnimationSpeed() {
        int delay = 1100 - speedSlider.getValue()*100;
        animationTimer.setDelay(delay);
    }

    public void setGraph(Graph g, int src) {
        this.currentGraph=g;
        this.sourceVertex=src;
        this.edges=g.getEdges();
        canvas.setGraphData(g,src);
        resetVisualization();
    }

    // ------------------- VisualizationCanvas -------------------
    private static class VisualizationCanvas extends JPanel {
        private Graph graph;
        private int sourceVertex;
        private int[] distances;
        private boolean[] vertexUpdated;
        private Edge currentEdge;
        private Point[] pos;
        final int R=25;

        void setGraphData(Graph g,int src) {
            this.graph=g; this.sourceVertex=src;
            calculatePositions();
        }
        void setVisualizationData(int[] dist,Edge e,boolean[] upd) {
            this.distances=dist; this.currentEdge=e; this.vertexUpdated=upd;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (graph==null||pos==null) return;
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            drawEdges(g2);
            drawVertices(g2);
            g2.dispose();
        }
        private void calculatePositions(){
            if (graph==null) return;
            int n=graph.getVertexCount();
            pos=new Point[n];
            int cx=getWidth()/2, cy=getHeight()/2, rad=Math.min(cx,cy)-50;
            if (rad<50) rad=100;
            for(int i=0;i<n;i++){
                double a=2*Math.PI*i/n;
                pos[i]=new Point(cx+(int)(rad*Math.cos(a)),
                                 cy+(int)(rad*Math.sin(a)));
            }
        }
        private void drawEdges(Graphics2D g2){
            if (graph==null) return;
            g2.setStroke(new BasicStroke(2));
            for(Edge e: graph.getEdges()){
                int u=e.getU(),v=e.getV();
                if (u>=pos.length||v>=pos.length) continue;
                Point A=pos[u], B=pos[v];
                boolean isCurr = e.equals(currentEdge);
                g2.setColor(isCurr?Color.RED:Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(isCurr?3:2));
                double dx=B.x-A.x, dy=B.y-A.y, d=Math.hypot(dx,dy);
                double ux=dx/d, uy=dy/d;
                int x1=(int)(A.x+ux*R), y1=(int)(A.y+uy*R);
                int x2=(int)(B.x-ux*R), y2=(int)(B.y-uy*R);
                g2.drawLine(x1,y1,x2,y2);
                drawArrow(g2,x1,y1,x2,y2);
                drawWeight(g2,e,x1,y1,x2,y2);
            }
        }
        private void drawArrow(Graphics2D g2,int x1,int y1,int x2,int y2){
            double ang=Math.atan2(y2-y1,x2-x1);
            int L=12; double a=Math.PI/6;
            int x3=(int)(x2-L*Math.cos(ang-a)),
                y3=(int)(y2-L*Math.sin(ang-a));
            int x4=(int)(x2-L*Math.cos(ang+a)),
                y4=(int)(y2-L*Math.sin(ang+a));
            g2.drawLine(x2,y2,x3,y3);
            g2.drawLine(x2,y2,x4,y4);
        }
        private void drawWeight(Graphics2D g2,Edge e,int x1,int y1,int x2,int y2){
            int mx=(x1+x2)/2, my=(y1+y2)/2;
            String s=String.valueOf(e.getWeight());
            FontMetrics fm=g2.getFontMetrics();
            int w=fm.stringWidth(s), h=fm.getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRect(mx-w/2-2,my-h/2-1,w+4,h+2);
            g2.setColor(e.getWeight()<0?Color.RED:Color.BLACK);
            g2.drawString(s,mx-w/2,my+fm.getAscent()/2);
        }
        private void drawVertices(Graphics2D g2){
            if (distances==null) return;
            for(int i=0;i<pos.length;i++){
                Point p=pos[i];
                boolean isSource=(i==sourceVertex);
                boolean updated=(vertexUpdated!=null && vertexUpdated[i]);
                Color col=isSource?new Color(255,69,0)
                          :updated?new Color(50,205,50)
                          :new Color(100,149,237);
                g2.setColor(col);
                g2.fillOval(p.x-R,p.y-R,2*R,2*R);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(p.x-R,p.y-R,2*R,2*R);
                g2.setColor(Color.WHITE);
                String label=Integer.toString(i);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(label,p.x-fm.stringWidth(label)/2,p.y+fm.getAscent()/2);
            }
        }
    }
}
