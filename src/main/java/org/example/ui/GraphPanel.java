package org.example.ui;

import org.example.model.Edge;
import org.example.model.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Інтерактивна панель для створення та редагування графів.
 * Підтримує:
 * - Додавання вершин (ЛКМ)
 * - Видалення вершин/ребер (ПКМ)
 * - З'єднання вершин (перетягування)
 * - Встановлення джерела (подвійний клік)
 * - Генерацію випадкових графів
 */
public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    // Константи для візуалізації
    private static final int VERTEX_RADIUS = 20;
    private static final Color VERTEX_COLOR = new Color(100, 149, 237);
    private static final Color SOURCE_COLOR = new Color(255, 69, 0);
    private static final Color EDGE_COLOR = Color.DARK_GRAY;
    private static final Color SELECTED_COLOR = new Color(255, 215, 0);
    private static final Font VERTEX_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    private static final Font WEIGHT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    
    // Внутрішні класи для зберігання візуальної інформації
    private static class VisualVertex {
        int id;
        int x, y;
        boolean isSource;
        
        VisualVertex(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.isSource = false;
        }
        
        boolean contains(int px, int py) {
            int dx = px - x;
            int dy = py - y;
            return dx * dx + dy * dy <= VERTEX_RADIUS * VERTEX_RADIUS;
        }
    }
    
    private static class VisualEdge {
        VisualVertex from, to;
        int weight;
        
        VisualEdge(VisualVertex from, VisualVertex to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }
    
    // Стан панелі
    private List<VisualVertex> vertices;
    private List<VisualEdge> edges;
    private VisualVertex sourceVertex;
    private int nextVertexId;
    
    // Стан взаємодії
    private VisualVertex selectedVertex;
    private VisualVertex dragStartVertex;
    private Point dragCurrentPoint;
    private boolean isDragging;
    
    // Режими роботи
    private boolean showWeights = true;
    private boolean allowNegativeWeights = true;
    private Random random = new Random();
    
    // Колбеки для зовнішнього коду
    private Runnable onGraphChanged;
    
    public GraphPanel() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        nextVertexId = 0;
        
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));
        setBorder(BorderFactory.createLoweredBevelBorder());
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        // Створюємо приклад графа
        createExampleGraph();
    }
    
    public void setOnGraphChanged(Runnable callback) {
        this.onGraphChanged = callback;
    }
    
    /**
     * Створює приклад графа для демонстрації
     */
    private void createExampleGraph() {
        clear();
        
        // Додаємо вершини
        addVertex(100, 100);  // 0
        addVertex(250, 80);   // 1  
        addVertex(400, 120);  // 2
        addVertex(150, 220);  // 3
        addVertex(350, 250);  // 4
        
        // Додаємо ребра
        addEdge(0, 1, 10);
        addEdge(0, 3, 5);
        addEdge(1, 2, 3);
        addEdge(1, 3, 2);
        addEdge(2, 4, 7);
        addEdge(3, 4, 9);
        addEdge(3, 1, -1);  // негативне ребро
        
        // Встановлюємо джерело
        setSource(0);
        
        repaint();
        notifyGraphChanged();
    }
    
    /**
     * Генерує випадковий граф
     */
    public void generateRandomGraph(int vertexCount, int edgeCount) {
        clear();
        
        // Генеруємо випадкові позиції вершин
        int width = getWidth() - 2 * VERTEX_RADIUS;
        int height = getHeight() - 2 * VERTEX_RADIUS;
        
        for (int i = 0; i < vertexCount; i++) {
            int x = VERTEX_RADIUS + random.nextInt(width);
            int y = VERTEX_RADIUS + random.nextInt(height);
            
            // Перевіряємо, щоб вершини не перекривалися
            boolean overlaps = false;
            for (VisualVertex v : vertices) {
                int dx = x - v.x;
                int dy = y - v.y;
                if (dx * dx + dy * dy < (3 * VERTEX_RADIUS) * (3 * VERTEX_RADIUS)) {
                    overlaps = true;
                    break;
                }
            }
            
            if (!overlaps) {
                addVertex(x, y);
            } else {
                i--; // Спробуємо ще раз
            }
        }
        
        // Генеруємо випадкові ребра
        int addedEdges = 0;
        int attempts = 0;
        while (addedEdges < edgeCount && attempts < edgeCount * 3) {
            int from = random.nextInt(vertices.size());
            int to = random.nextInt(vertices.size());
            
            if (from != to && !hasEdge(from, to)) {
                int weight = allowNegativeWeights ? 
                    random.nextInt(21) - 10 :  // -10 до 10
                    random.nextInt(20) + 1;    // 1 до 20
                addEdge(from, to, weight);
                addedEdges++;
            }
            attempts++;
        }
        
        // Встановлюємо випадкове джерело
        if (!vertices.isEmpty()) {
            setSource(random.nextInt(vertices.size()));
        }
        
        repaint();
        notifyGraphChanged();
    }
    
    private boolean hasEdge(int fromId, int toId) {
        for (VisualEdge edge : edges) {
            if (edge.from.id == fromId && edge.to.id == toId) {
                return true;
            }
        }
        return false;
    }
    
    private void addVertex(int x, int y) {
        vertices.add(new VisualVertex(nextVertexId++, x, y));
    }
    
    private void addEdge(int fromId, int toId, int weight) {
        VisualVertex from = findVertexById(fromId);
        VisualVertex to = findVertexById(toId);
        if (from != null && to != null) {
            edges.add(new VisualEdge(from, to, weight));
        }
    }
    
    private VisualVertex findVertexById(int id) {
        return vertices.stream()
                .filter(v -> v.id == id)
                .findFirst()
                .orElse(null);
    }
    
    private void setSource(int vertexId) {
        // Скидаємо попереднє джерело
        if (sourceVertex != null) {
            sourceVertex.isSource = false;
        }
        
        // Встановлюємо нове
        sourceVertex = findVertexById(vertexId);
        if (sourceVertex != null) {
            sourceVertex.isSource = true;
        }
    }
    
    public void clear() {
        vertices.clear();
        edges.clear();
        sourceVertex = null;
        nextVertexId = 0;
        selectedVertex = null;
        repaint();
        notifyGraphChanged();
    }
    
    /**
     * Експортує поточний граф у об'єкт Graph
     */
    public Graph exportGraph() {
        if (vertices.isEmpty()) {
            return new Graph(1); // Порожній граф з однією вершиною
        }
        
        Graph graph = new Graph(vertices.size());
        
        for (VisualEdge vedge : edges) {
            graph.addEdge(vedge.from.id, vedge.to.id, vedge.weight);
        }
        
        return graph;
    }
    
    /**
     * Повертає ID джерельної вершини
     */
    public int getSourceVertex() {
        return sourceVertex != null ? sourceVertex.id : 0;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Антиаліасинг для кращої якості
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Малюємо ребра
        g2d.setStroke(new BasicStroke(2.0f));
        for (VisualEdge edge : edges) {
            drawEdge(g2d, edge);
        }
        
        // Малюємо лінію перетягування
        if (isDragging && dragStartVertex != null && dragCurrentPoint != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
            g2d.drawLine(dragStartVertex.x, dragStartVertex.y, dragCurrentPoint.x, dragCurrentPoint.y);
        }
        
        // Малюємо вершини
        for (VisualVertex vertex : vertices) {
            drawVertex(g2d, vertex);
        }
        
        // Малюємо інструкції, якщо граф порожній
        if (vertices.isEmpty()) {
            drawInstructions(g2d);
        }
        
        g2d.dispose();
    }
    
    private void drawEdge(Graphics2D g2d, VisualEdge edge) {
        g2d.setColor(EDGE_COLOR);
        
        // Обчислюємо точки на межі кіл (а не центри)
        double dx = edge.to.x - edge.from.x;
        double dy = edge.to.y - edge.from.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 2 * VERTEX_RADIUS) return; // Занадто близько
        
        double unitX = dx / dist;
        double unitY = dy / dist;
        
        int startX = (int) (edge.from.x + unitX * VERTEX_RADIUS);
        int startY = (int) (edge.from.y + unitY * VERTEX_RADIUS);
        int endX = (int) (edge.to.x - unitX * VERTEX_RADIUS);
        int endY = (int) (edge.to.y - unitY * VERTEX_RADIUS);
        
        // Малюємо лінію
        g2d.drawLine(startX, startY, endX, endY);
        
        // Малюємо стрілку
        drawArrowHead(g2d, startX, startY, endX, endY);
        
        // Малюємо вагу
        if (showWeights) {
            int midX = (startX + endX) / 2;
            int midY = (startY + endY) / 2;
            
            g2d.setFont(WEIGHT_FONT);
            FontMetrics fm = g2d.getFontMetrics();
            String weightStr = String.valueOf(edge.weight);
            int textWidth = fm.stringWidth(weightStr);
            
            // Фон для тексту
            g2d.setColor(Color.WHITE);
            g2d.fillRect(midX - textWidth/2 - 2, midY - fm.getHeight()/2, textWidth + 4, fm.getHeight());
            
            // Сам текст
            g2d.setColor(edge.weight < 0 ? Color.RED : Color.BLACK);
            g2d.drawString(weightStr, midX - textWidth/2, midY + fm.getAscent()/2);
        }
    }
    
    private void drawArrowHead(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        
        int arrowLength = 10;
        double arrowAngle = Math.PI / 6;
        
        int x3 = (int) (x2 - arrowLength * Math.cos(angle - arrowAngle));
        int y3 = (int) (y2 - arrowLength * Math.sin(angle - arrowAngle));
        int x4 = (int) (x2 - arrowLength * Math.cos(angle + arrowAngle));
        int y4 = (int) (y2 - arrowLength * Math.sin(angle + arrowAngle));
        
        g2d.drawLine(x2, y2, x3, y3);
        g2d.drawLine(x2, y2, x4, y4);
    }
    
    private void drawVertex(Graphics2D g2d, VisualVertex vertex) {
        // Вибираємо колір
        Color color = vertex.isSource ? SOURCE_COLOR : 
                     vertex == selectedVertex ? SELECTED_COLOR : VERTEX_COLOR;
        
        // Малюємо коло
        g2d.setColor(color);
        g2d.fillOval(vertex.x - VERTEX_RADIUS, vertex.y - VERTEX_RADIUS, 
                     2 * VERTEX_RADIUS, 2 * VERTEX_RADIUS);
        
        // Малюємо обводку
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(vertex.x - VERTEX_RADIUS, vertex.y - VERTEX_RADIUS, 
                     2 * VERTEX_RADIUS, 2 * VERTEX_RADIUS);
        
        // Малюємо номер вершини
        g2d.setFont(VERTEX_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        String id = String.valueOf(vertex.id);
        int textWidth = fm.stringWidth(id);
        int textHeight = fm.getHeight();
        
        g2d.setColor(Color.WHITE);
        g2d.drawString(id, vertex.x - textWidth/2, vertex.y + fm.getAscent()/2);
    }
    
    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 14));
        
        String[] instructions = {
            "Клікніть, щоб додати вершину",
            "Перетягніть між вершинами, щоб створити ребро",
            "ПКМ для видалення",
            "Подвійний клік для встановлення джерела"
        };
        
        FontMetrics fm = g2d.getFontMetrics();
        int y = getHeight() / 2 - (instructions.length * fm.getHeight()) / 2;
        
        for (String instruction : instructions) {
            int x = (getWidth() - fm.stringWidth(instruction)) / 2;
            g2d.drawString(instruction, x, y);
            y += fm.getHeight() + 5;
        }
    }
    
    private VisualVertex findVertexAt(int x, int y) {
        for (VisualVertex vertex : vertices) {
            if (vertex.contains(x, y)) {
                return vertex;
            }
        }
        return null;
    }
    
    private void notifyGraphChanged() {
        if (onGraphChanged != null) {
            onGraphChanged.run();
        }
    }
    
    // Обробники миші
    @Override
    public void mouseClicked(MouseEvent e) {
        VisualVertex clickedVertex = findVertexAt(e.getX(), e.getY());
        
        if (e.getClickCount() == 2) {
            // Подвійний клік - встановити джерело
            if (clickedVertex != null) {
                setSource(clickedVertex.id);
                repaint();
                notifyGraphChanged();
            }
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            // Лівий клік
            if (clickedVertex == null) {
                // Додати нову вершину
                addVertex(e.getX(), e.getY());
                repaint();
                notifyGraphChanged();
            }
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            // Правий клік - видалити
            if (clickedVertex != null) {
                removeVertex(clickedVertex);
                repaint();
                notifyGraphChanged();
            }
        }
    }
    
    private void removeVertex(VisualVertex vertex) {
        // Видаляємо всі ребра, пов'язані з цією вершиною
        edges.removeIf(edge -> edge.from == vertex || edge.to == vertex);
        
        // Видаляємо саму вершину
        vertices.remove(vertex);
        
        // Якщо це було джерело, скидаємо його
        if (vertex == sourceVertex) {
            sourceVertex = null;
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        VisualVertex vertex = findVertexAt(e.getX(), e.getY());
        if (vertex != null && e.getButton() == MouseEvent.BUTTON1) {
            selectedVertex = vertex;
            dragStartVertex = vertex;
            isDragging = false;
            repaint();
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (isDragging && dragStartVertex != null) {
            VisualVertex endVertex = findVertexAt(e.getX(), e.getY());
            if (endVertex != null && endVertex != dragStartVertex) {
                // Створюємо ребро
                String weightStr = JOptionPane.showInputDialog(
                    this, 
                    "Введіть вагу ребра:", 
                    "Нове ребро", 
                    JOptionPane.PLAIN_MESSAGE
                );
                
                if (weightStr != null && !weightStr.trim().isEmpty()) {
                    try {
                        int weight = Integer.parseInt(weightStr.trim());
                        if (!hasEdge(dragStartVertex.id, endVertex.id)) {
                            addEdge(dragStartVertex.id, endVertex.id, weight);
                            notifyGraphChanged();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                            this, 
                            "Некоректна вага ребра", 
                            "Помилка", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        }
        
        selectedVertex = null;
        dragStartVertex = null;
        isDragging = false;
        dragCurrentPoint = null;
        repaint();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragStartVertex != null) {
            isDragging = true;
            dragCurrentPoint = e.getPoint();
            repaint();
        }
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {}
    
    // Публічні методи для управління панеллю
    public void setShowWeights(boolean show) {
        this.showWeights = show;
        repaint();
    }
    
    public void setAllowNegativeWeights(boolean allow) {
        this.allowNegativeWeights = allow;
    }
    
    public boolean isEmpty() {
        return vertices.isEmpty();
    }
    
    public int getVertexCount() {
        return vertices.size();
    }
    
    public int getEdgeCount() {
        return edges.size();
    }
}