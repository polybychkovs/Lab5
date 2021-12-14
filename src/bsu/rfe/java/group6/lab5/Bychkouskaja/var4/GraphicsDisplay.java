package bsu.rfe.java.group6.lab5.Bychkouskaja.var4;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {
    private ArrayList<Double[]> graphicsData;

    private ArrayList<Double[]> originalData;

    private int selectedMarker = -1;

    private double minX;

    private double maxX;

    private double minY;

    private double maxY;

    private double[][] viewport = new double[2][2];

    private ArrayList<double[][]> undoHistory = (ArrayList)new ArrayList<>(5);

    private double scaleX;

    private double scaleY;

    private BasicStroke axisStroke;

    private BasicStroke gridStroke;

    private BasicStroke markerStroke;

    private BasicStroke selectionStroke;

    private Font axisFont;

    private Font labelsFont;

    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();

    private boolean scaleMode = false;

    private boolean changeMode = false;

    private double[] originalPoint = new double[2];

    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        this.axisStroke = new BasicStroke(2.0F, 0, 0, 10.0F, null, 0.0F);
        this.gridStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[] { 4.0F, 4.0F }, 0.0F);
        this.markerStroke = new BasicStroke(1.0F, 0, 0, 10.0F, null, 0.0F);
        this.selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[] { 10.0F, 10.0F }, 0.0F);
        this.axisFont = new Font("Serif", 1, 36);
        this.labelsFont = new Font("Serif", 0, 10);
        formatter.setMaximumFractionDigits(5);
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    public void displayGraphics(ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;
        this.originalData = (ArrayList)new ArrayList<Double>(graphicsData.size());
        for (Double[] point : graphicsData) {
            Double[] newPoint = new Double[2];
            newPoint[0] = new Double(point[0].doubleValue());
            newPoint[1] = new Double(point[1].doubleValue());
            this.originalData.add(newPoint);
        }
        this.minX = ((Double[])graphicsData.get(0))[0].doubleValue();
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0].doubleValue();
        this.minY = ((Double[])graphicsData.get(0))[1].doubleValue();
        this.maxY = this.minY;
        for (int i = 1; i < graphicsData.size(); i++) {
            if (((Double[])graphicsData.get(i))[1].doubleValue() < this.minY)
                this.minY = ((Double[])graphicsData.get(i))[1].doubleValue();
            if (((Double[])graphicsData.get(i))[1].doubleValue() > this.maxY)
                this.maxY = ((Double[])graphicsData.get(i))[1].doubleValue();
        }
        zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.scaleX = getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        if (this.graphicsData == null || this.graphicsData.size() == 0)
            return;
        Graphics2D canvas = (Graphics2D)g;
        paintGrid(canvas);
        paintAxis(canvas);
        paintGraphics(canvas);
        paintMarkers(canvas);
        paintLabels(canvas);
        paintSelection(canvas);
    }

    private void paintSelection(Graphics2D canvas) {
        if (!this.scaleMode)
            return;
        canvas.setStroke(this.selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(this.selectionRect);
    }

    private void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        for (Double[] point : this.graphicsData) {
            if (point[0].doubleValue() < this.viewport[0][0] || point[1].doubleValue() > this.viewport[0][1] ||
                    point[0].doubleValue() > this.viewport[1][0] || point[1].doubleValue() < this.viewport[1][1])
                continue;
            if (currentX != null && currentY != null)
                canvas.draw(new Line2D.Double(translateXYtoPoint(currentX.doubleValue(), currentY.doubleValue()),
                        translateXYtoPoint(point[0].doubleValue(), point[1].doubleValue())));
            currentX = point[0];
            currentY = point[1];
        }
    }

    private void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);
        Ellipse2D.Double lastMarker = null;
        int i = -1;
        for (Double[] point : this.graphicsData) {
            int radius;
            i++;
            if (point[0].doubleValue() < this.viewport[0][0] || point[1].doubleValue() > this.viewport[0][1] ||
                    point[0].doubleValue() > this.viewport[1][0] || point[1].doubleValue() < this.viewport[1][1])
                continue;
            if (i == this.selectedMarker) {
                radius = 6;
            } else {
                radius = 3;
            }
            Ellipse2D.Double marker = new Ellipse2D.Double();
            Point2D center = translateXYtoPoint(point[0].doubleValue(), point[1].doubleValue());
            Point2D corner = new Point2D.Double(center.getX() + radius, center.getY() + radius);
            marker.setFrameFromCenter(center, corner);
            if (i == this.selectedMarker) {
                lastMarker = marker;
                continue;
            }
            canvas.draw(marker);
            canvas.fill(marker);
        }
        if (lastMarker != null) {
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            canvas.draw(lastMarker);
            canvas.fill(lastMarker);
        }
    }

    private void paintLabels(Graphics2D canvas) {
        double labelXPos, labelYPos;
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (this.viewport[1][1] < 0.0D && this.viewport[0][1] > 0.0D) {
            labelYPos = 0.0D;
        } else {
            labelYPos = this.viewport[1][1];
        }
        if (this.viewport[0][0] < 0.0D && this.viewport[1][0] > 0.0D) {
            labelXPos = 0.0D;
        } else {
            labelXPos = this.viewport[0][0];
        }
        double pos = this.viewport[0][0];
        double step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D;
        while (pos < this.viewport[1][0]) {
            Point2D.Double point = translateXYtoPoint(pos, labelYPos);
            String label = formatter.format(pos);
            Rectangle2D bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            pos += step;
        }
        pos = this.viewport[1][1];
        step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D;
        while (pos < this.viewport[0][1]) {
            Point2D.Double point = translateXYtoPoint(labelXPos, pos);
            String label = formatter.format(pos);
            Rectangle2D bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            pos += step;
        }
        if (this.selectedMarker >= 0) {
            Point2D.Double point = translateXYtoPoint(((Double[])this.graphicsData.get(this.selectedMarker))[0].doubleValue(), ((Double[])this.graphicsData.get(this.selectedMarker))[1].doubleValue());
            String label = "X=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            Rectangle2D bounds = this.labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }
    }
    //сетка
    private void paintGrid(Graphics2D canvas) {
        canvas.setStroke(this.gridStroke);
        canvas.setColor(Color.GRAY);
        double pos = this.viewport[0][0];
        double step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0D;
        while (pos < this.viewport[1][0]) {
            canvas.draw(new Line2D.Double(translateXYtoPoint(pos, this.viewport[0][1]),
                    translateXYtoPoint(pos, this.viewport[1][1])));
            pos += step;
        }
        canvas.draw(new Line2D.Double(translateXYtoPoint(this.viewport[1][0], this.viewport[0][1]),
                translateXYtoPoint(this.viewport[1][0], this.viewport[1][1])));
        pos = this.viewport[1][1];
        step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0D;
        while (pos < this.viewport[0][1]) {
            canvas.draw(new Line2D.Double(translateXYtoPoint(this.viewport[0][0], pos),
                    translateXYtoPoint(this.viewport[1][0], pos)));
            pos += step;
        }
        canvas.draw(new Line2D.Double(translateXYtoPoint(this.viewport[0][0], this.viewport[0][1]),
                translateXYtoPoint(this.viewport[1][0], this.viewport[0][1])));
    }

    //оси
    private void paintAxis(Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context = canvas.getFontRenderContext();
        if (this.viewport[0][0] <= 0.0D && this.viewport[1][0] >= 0.0D) {
            canvas.draw(new Line2D.Double(translateXYtoPoint(0.0D, this.viewport[0][1]),
                    translateXYtoPoint(0.0D, this.viewport[1][1])));
            canvas.draw(new Line2D.Double(translateXYtoPoint(-(this.viewport[1][0] - this.viewport[0][0]) * 0.0025D, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015D),
                    translateXYtoPoint(0.0D, this.viewport[0][1])));
            canvas.draw(new Line2D.Double(translateXYtoPoint((this.viewport[1][0] - this.viewport[0][0]) * 0.0025D, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015D),
                    translateXYtoPoint(0.0D, this.viewport[0][1])));
            Rectangle2D bounds = this.axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = translateXYtoPoint(0.0D, this.viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10.0F, (float)(labelPos.y + bounds.getHeight() / 2.0D));
        }
        if (this.viewport[1][1] <= 0.0D && this.viewport[0][1] >= 0.0D) {
            canvas.draw(new Line2D.Double(translateXYtoPoint(this.viewport[0][0], 0.0D),
                    translateXYtoPoint(this.viewport[1][0], 0.0D)));
            canvas.draw(new Line2D.Double(translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01D, (this.viewport[0][1] - this.viewport[1][1]) * 0.005D),
                    translateXYtoPoint(this.viewport[1][0], 0.0D)));
            canvas.draw(new Line2D.Double(translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01D, -(this.viewport[0][1] - this.viewport[1][1]) * 0.005D),
                    translateXYtoPoint(this.viewport[1][0], 0.0D)));
            Rectangle2D bounds = this.axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = translateXYtoPoint(this.viewport[1][0], 0.0D);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0D), (float)(labelPos.y - bounds.getHeight() / 2.0D));
        }
    }

    protected Point2D.Double translateXYtoPoint(double x, double y) {
        double deltaX = x - this.viewport[0][0];
        double deltaY = this.viewport[0][1] - y;
        return new Point2D.Double(deltaX * this.scaleX, deltaY * this.scaleY);
    }

    protected double[] translatePointToXY(int x, int y) {
        return new double[] { this.viewport[0][0] + x / this.scaleX, this.viewport[0][1] - y / this.scaleY };
    }

    protected int findSelectedPoint(int x, int y) {
        if (this.graphicsData == null)
            return -1;
        int pos = 0;
        for (Double[] point : this.graphicsData) {
            Point2D.Double screenPoint = translateXYtoPoint(point[0].doubleValue(), point[1].doubleValue());
            double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y) * (screenPoint.getY() - y);
            if (distance < 100.0D)
                return pos;
            pos++;
        }
        return -1;
    }

    public void reset() {
        displayGraphics(this.originalData);
    }

    public class MouseHandler extends MouseAdapter {
        // обработать событие от щелчка кнопкой мыши
        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                if (GraphicsDisplay.this.undoHistory.size() > 0) {
                    GraphicsDisplay.this.viewport = GraphicsDisplay.this.undoHistory.get(GraphicsDisplay.this.undoHistory.size() - 1);
                    GraphicsDisplay.this.undoHistory.remove(GraphicsDisplay.this.undoHistory.size() - 1);
                } else {
                    GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX, GraphicsDisplay.this.maxY, GraphicsDisplay.this.maxX, GraphicsDisplay.this.minY);
                }
                GraphicsDisplay.this.repaint();
            }
        }
        // обработать событие нажатия кнопки мыши
        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() != 1)
                return;
            GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.this.changeMode = true;
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            } else {
                GraphicsDisplay.this.scaleMode = true;
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
                GraphicsDisplay.this.selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
            }
        }
        // обработать событие отпускания кнопки мыши
        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() != 1)
                return;
            GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            if (GraphicsDisplay.this.changeMode) {
                GraphicsDisplay.this.changeMode = false;
            } else {
                GraphicsDisplay.this.scaleMode = false;
                double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                GraphicsDisplay.this.undoHistory.add(GraphicsDisplay.this.viewport);
                GraphicsDisplay.this.viewport = new double[2][2];
                GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0], GraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);
                GraphicsDisplay.this.repaint();
            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        // обработать событие перемещения мыши
        public void mouseMoved(MouseEvent ev) {
            GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            if (GraphicsDisplay.this.selectedMarker >= 0) {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            } else {
                GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            }
            GraphicsDisplay.this.repaint();
        }
        // обработать событие перетаскивания курсора мыши
        public void mouseDragged(MouseEvent ev) {
            if (GraphicsDisplay.this.changeMode) {
                double[] currentPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1].doubleValue() + currentPoint[1] - ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1].doubleValue();
                if (newY > GraphicsDisplay.this.viewport[0][1])
                    newY = GraphicsDisplay.this.viewport[0][1];
                if (newY < GraphicsDisplay.this.viewport[1][1])
                    newY = GraphicsDisplay.this.viewport[1][1];
                ((Double[])GraphicsDisplay.this.graphicsData.get(GraphicsDisplay.this.selectedMarker))[1] = Double.valueOf(newY);
                GraphicsDisplay.this.repaint();
            } else {
                double width = ev.getX() - GraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0D)
                    width = 5.0D;
                double height = ev.getY() - GraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0D)
                    height = 5.0D;
                GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(), GraphicsDisplay.this.selectionRect.getY(), width, height);
                GraphicsDisplay.this.repaint();
            }
        }
    }
}
