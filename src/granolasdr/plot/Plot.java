/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Plot.java
 *
 * Created on Dec 3, 2009, 2:32:07 PM
 */
package granolasdr.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author matt
 */
public class Plot extends javax.swing.JPanel {

    private int height = 0;
    private int width = 0;
    private double xAxisMin = Double.MAX_VALUE;
    private double yAxisMin = Double.MAX_VALUE;
    private double xAxisMax = -Double.MAX_VALUE;
    private double yAxisMax = -Double.MAX_VALUE;
    private int numVGrids = 4;
    private int numHGrids = 4;
    private Image offscreen = null;
    private Graphics2D bufferGraphics;
    private boolean drawVGridB = true;
    private boolean drawHGridB = true;
    private boolean drawHAxisB = true;
    private boolean drawVAxisB = true;
    // private boolean updateLock = false;
    private Color backgroundColor = Color.white;
    private Color selectionColor = Color.YELLOW;
    private Color axisColor = Color.black;
    private Color gridColor = Color.gray;
    private List<Series> dataList = new ArrayList<Series>();
    private Point selectionStart = null;
    private Point selectionEnd = null;
    private boolean highLight = true;

    /** Creates new form Plot */
    public Plot() {
        initComponents();
    }

    
    public void disableHighlight(){
        highLight = false;
    }
    
    /**
     * Returns the color of the background
     *
     * @return background color
     *
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param color Color to draw on the background
     */
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
        update();
    }

    /**
     * Returns the color of the selection
     *
     * @return background color
     *
     */
    public Color getSelectionColor() {
        return selectionColor;
    }

    /**
     * Sets the selection color.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param color Color to draw on the background
     */
    public void setSelectionColor(Color color) {
        selectionColor = color;
        update();
    }

    public double[] getYAxis(){
        double[] returnValue = {0.0, 0.0};
        returnValue[0] = yAxisMin;
        returnValue[1] = yAxisMax;
        return(returnValue);
    }
    
    public double[] getXAxis(){
        double[] returnValue = new double[2];
        returnValue[0] = xAxisMin;
        returnValue[1] = xAxisMax;
        return(returnValue);
    }
    
    public void setYAxis(double max, double min) {
        yAxisMin = min;
        yAxisMax = max;
    }

    public void setXAxis(double max, double min) {
        xAxisMin = min;
        xAxisMax = max;
    }

    public void setZoom(ZoomCoordinates zoom) {
        yAxisMin = zoom.getMinY();
        yAxisMax = zoom.getMaxY();
        xAxisMin = zoom.getMinX();
        xAxisMax = zoom.getMaxX();
    }

    public ZoomCoordinates getZoom() {
        ZoomCoordinates ret = new ZoomCoordinates(xAxisMin, xAxisMax, yAxisMin, yAxisMax);
        return ret;
    }

    /**
     * Returns the color of the axis
     *
     * @return axis color
     *
     */
    public Color getAxisColor() {
        return axisColor;
    }

    /**
     * Sets the axis color.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param color Color to draw on the axis
     */
    public void setAxisColor(Color color) {
        axisColor = color;
        update();
    }

    /**
     * Returns the color of the grid
     *
     * @return grid color
     *
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Sets the grid color.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param color Color to draw on the grid
     */
    public void setGridColor(Color color) {
        gridColor = color;
        update();
    }

    /**
     * Gets number of Horizontal Grids.
     *
     * @return Number of Horizontal Grids
     */
    public int getNumHGrids() {
        return numHGrids;
    }

    /**
     * Will it draw the Horizontal Grid
     *
     * @return If it will draw horizontal grid
     */
    public boolean isDrawHGrid() {
        return drawHGridB;
    }

    /**
     * Boolean to draw horizontal grid or not.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param b Whether or not to draw horizontal grid.
     */
    public void setDrawHGrid(boolean b) {
        drawHGridB = b;
        update();
    }

    /**
     *
     * @return Whether or not it will draw the horizontal axis.
     */
    public boolean isDrawHAxis() {
        return drawHAxisB;
    }

    /**
     * Set whether or not to draw horizontal axis.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param b Boolean setting to draw horizontal axis or not
     */
    public void setDrawHAxis(boolean b) {
        drawHAxisB = b;
        update();
    }

    /**
     *
     * @return Whether or not it will draw the vertical axis.
     */
    public boolean isDrawVAxis() {
        return drawVAxisB;
    }

    /**
     * Set whether or not to draw vertical axis.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param b Boolean setting to draw vertical axis or not
     */
    public void setDrawVAxis(boolean b) {
        drawVAxisB = b;
        update();
    }

    /**
     * Will it draw the vertical grid
     *
     * @return If it will draw vertical grid
     */
    public boolean isDrawVGrid() {
        return drawVGridB;
    }

    /**
     * Boolean to draw vertical grid or not.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param b Whether or not to draw vertical grid.
     */
    public void setDrawVGrid(boolean b) {
        drawVGridB = b;
        update();
    }

    /**
     * Sets the number of horizontal grids to draw.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param num Number of grids to draw
     */
    public void setNumHGrids(int num) {
        numHGrids = num;
        update();
    }

    /**
     * Returns number of vertical grids that will be drawn
     *
     * @return Number of vetical grids to be drawn
     */
    public int getNumVGrids() {
        return numVGrids;
    }

    /**
     * Sets the number of vertical grids to draw.  This method invokes the changes in the awt
     * thread and is thread safe.
     *
     * @param num Number of grids to draw
     */
    public void setNumVGrids(int num) {
        numVGrids = num;
        update();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(offscreen, 0, 0, this);
        //    super.paint(g);
    }

    private void drawVGrid() {
        double interval = width / ((double) numVGrids + 1.0);
        float[] pattern = {0.0f, 0.0f};
        pattern[0] = 1;
        pattern[1] = 4;
        Stroke oldStroke = bufferGraphics.getStroke();
        Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, pattern, 0.0f);
        bufferGraphics.setStroke(stroke);
        for (int cnt = 0; cnt < numVGrids; cnt++) {
            int mid = (int) ((cnt + 1) * interval);
            bufferGraphics.drawLine(mid, 0, mid, height);
        }
        bufferGraphics.setStroke(oldStroke);
    }

    private void drawVAxisTicks() {
        double interval = height / ((double) numHGrids + 1.0);
        int w = width / 2;
        for (int cnt = 0; cnt < numHGrids; cnt++) {
            int mid = (int) ((cnt + 1) * interval);
            bufferGraphics.drawLine(w - 2, mid, w + 2, mid);
        }
    }

    private void drawHAxisTicks() {
        double interval = width / ((double) numVGrids + 1.0);
        int h = height / 2;
        for (int cnt = 0; cnt < numVGrids; cnt++) {
            int mid = (int) ((cnt + 1) * interval);
            bufferGraphics.drawLine(mid, h - 2, mid, h + 2);
        }
    }

    private void drawHGrid() {
        double interval = height / ((double) numHGrids + 1.0);
        float[] pattern = {0.0f, 0.0f};
        pattern[0] = 1;
        pattern[1] = 4;
        Stroke oldStroke = bufferGraphics.getStroke();
        Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, pattern, 0.0f);
        bufferGraphics.setStroke(stroke);
        for (int cnt = 0; cnt < numHGrids; cnt++) {
            int mid = (int) ((cnt + 1) * interval);
            bufferGraphics.drawLine(0, mid, width, mid);
        }
        bufferGraphics.setStroke(oldStroke);
    }

    private void drawHAxis() {
        int h = height / 2;
        bufferGraphics.drawLine(0, h, width, h);
    }

    private void drawVAxis() {
        int w = width / 2;
        bufferGraphics.drawLine(w, 0, w, height);
    }

     private void drawSelection() {
        if (selectionStart != null && selectionEnd != null) {
            Color currpaint = bufferGraphics.getColor();
            bufferGraphics.setColor(new Color(selectionColor.getRed(),
                    selectionColor.getGreen(),
                    selectionColor.getBlue(),
                    100));
            int[] xPoints = {0, 0, 0, 0};
            int[] yPoints = {0, 0, 0, 0};
            xPoints[0] = selectionStart.x;
            yPoints[0] = selectionStart.y;
            xPoints[1] = selectionStart.x;
            yPoints[1] = selectionEnd.y;
            xPoints[2] = selectionEnd.x;
            yPoints[2] = selectionEnd.y;
            xPoints[3] = selectionEnd.x;
            yPoints[3] = selectionStart.y;
            bufferGraphics.fillPolygon(xPoints, yPoints, 4);
            bufferGraphics.setColor(Color.BLACK);
            bufferGraphics.drawPolygon(xPoints, yPoints, 4);
            bufferGraphics.setColor(currpaint);
        }
    }

    /**
     * Add a data series to the plot.  This is for standard series data and will
     * draw the X axis with series value not actual time value. In other words,
     * this function adds data with not x values for the x axis.
     *
     * @param ydata - DoubleBuffer with the y axis data as the values.  The y
     * data can change, just call an update();
     * @param color - Color thay you want the axis to be.
     * @param autoscale - Autoscale the graph or go with current max and min.
     * @param connectLines - Connect the lines for each point or just draw points.
     */
    public void addData(DoubleBuffer ydata, int color, boolean autoscale, boolean connectLines) {
        addData(ydata, Color.getColor("", color), autoscale, connectLines);
    }
    
    public void addData(DoubleBuffer ydata, Color color, boolean autoscale, boolean connectLines) {
        Series s = new Series(ydata);
        if (autoscale) {
            s.findXMaxMin();
            s.findYMaxMin();
        }
        s.dataColor = color;
        s.connect = connectLines;
        dataList.add(s);
        update();
    }

    public void clearData() {
        dataList.clear();
        update();
    }

    /**
     * Add data series to the graph.  The data seris are stored in double buffers.
     * The data in the double buffers can change, just call update() when you
     * change the data to cause the series to recalculate and the graph to update.
     * 
     * @param xdata - DoubleBuffer with the x axis data as the values.  The x
     * data can change, just call an update();
     * @param ydata - DoubleBuffer with the y axis data as the values.  The y
     * data can change, just call an update();
     * @param color - Color thay you want the axis to be.
     * @param autoscale - Autoscale the graph or go with current max and min.
     * @param connectLines - Connect the lines for each point or just draw points.
     */
    
    public void addData(DoubleBuffer xdata, DoubleBuffer ydata, int color, boolean autoscale, boolean connectLines) {
         addData(xdata, ydata, Color.getColor("", color), autoscale, connectLines);
    }
    
    public void addData(DoubleBuffer xdata, DoubleBuffer ydata, Color color, boolean autoscale, boolean connectLines) {
        Series s = new Series(xdata, ydata);
        if (autoscale) {
            s.findXMaxMin();
            s.findYMaxMin();
        }
        s.dataColor = color;
        s.connect = connectLines;
        dataList.add(s);
        update();
    }

    private void drawBackground() {
        
//        if(offscreen == null){
            offscreen = createImage(width, height);
//        }
        if (offscreen != null) {
            bufferGraphics = (Graphics2D) offscreen.getGraphics();
            bufferGraphics.clearRect(0, 0, width, height);
            bufferGraphics.setPaintMode();
            bufferGraphics.setColor(backgroundColor);
            bufferGraphics.fillRect(0, 0, width, height);

            bufferGraphics.setColor(axisColor);
            if (drawHAxisB) {
                drawHAxis();
                drawHAxisTicks();
            }
            if (drawVAxisB) {
                drawVAxis();
                drawVAxisTicks();
            }
            bufferGraphics.setColor(gridColor);
            if (drawHGridB) {
                drawHGrid();
            }
            if (drawVGridB) {
                drawVGrid();
            }
            for (Series s : dataList) {
                s.drawPoints();
            }
            drawSelection();
        }
        
    }

    /**
     * Causes all autoscales to be recalculated and then forces an update on
     * the plot.  This function is automatically called by most functions such
     * as setDrawHGrid(), but has to be called if the underlying data to a
     * double buffer changes.
     * 
     */
    public synchronized void update() {
        // updateLock = true;
        doLayout();
        repaint();
        // updateLock = false;
    }

    /**
     * Causes all autoscales to be recalculated and min/max to be found and then
     * forces an update on the plot.  Same as update except finds new scales
     *
     */
    public void updateWithRescale() {
        xAxisMin = Double.MAX_VALUE;
        yAxisMin = Double.MAX_VALUE;
        xAxisMax = -Double.MAX_VALUE;
        yAxisMax = -Double.MAX_VALUE;
        for (Series s : dataList) {
            s.findXMaxMin();
            s.findYMaxMin();
        }
        update();
    }

    private Point getPoint(double x, double y) {
        double w = (x - xAxisMin) / (xAxisMax - xAxisMin) * width;
        double h = (1.0 - (y - yAxisMin) / (yAxisMax - yAxisMin)) * height;
//        System.out.println("yMin " + yMin + " yMax " + yMax + " Y " + y + " heigth " + h);
        return new Point((int) w, (int) h);
    }

    private double[] getPValue(int x, int y) {
        double w = ((double) x / width) * (xAxisMax - xAxisMin) + xAxisMin;
        double h = (1 - (double) y / height) * (yAxisMax - yAxisMin) + yAxisMin;
        return new double[]{w, h};
    }

    @Override
    public void doLayout() {
        super.doLayout();
        height = getHeight();
        width = getWidth();
//        System.out.println("Height " + height + " Width " + width);
        drawBackground();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setToolTipText(" ");
        setOpaque(false);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

   // JPL: methods disabled for the MMP_pistl, feel free to uncoment for other usage
    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        if(highLight){
            double[] pval = getPValue(evt.getX(), evt.getY());
            String x = String.format("%.4f", pval[0]);
            String y = String.format("%.4f", pval[1]);
            setToolTipText("(" + x + "," + y + ")");
            repaint();
        }
//        System.out.println("Point is (" + pval[0] + "," + pval[1] + ") at (" + evt.getX() + "," + evt.getY() + ")");
    }//GEN-LAST:event_formMouseMoved

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
//
        if(highLight){
//        double[] pval = getPValue(evt.getX(), evt.getY());
//        String x = String.format("%.4f", pval[0]);
//        String y = String.format("%.4f", pval[1]);
//        System.out.println("Mouse was dragged (" + x + " , " + y + ")");
            selectionEnd = evt.getPoint();
            update();
        }
    }//GEN-LAST:event_formMouseDragged

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if(highLight){
//        double[] pval = getPValue(evt.getX(), evt.getY());
//        String x = String.format("%.4f", pval[0]);
//        String y = String.format("%.4f", pval[1]);
//        System.out.println("Mouse was pressed at " + "(" + x + " , " + y + ")");
            selectionStart = evt.getPoint();
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        if(highLight){
            ZoomCoordinates oldZoom = new ZoomCoordinates(xAxisMin, xAxisMax, yAxisMin, yAxisMax);
            if (evt.getButton() == 1) {
                if (selectionStart != null && selectionEnd != null) {
                    double[] sval = getPValue(selectionStart.x, selectionStart.y);
                    double[] eval = getPValue(selectionEnd.x, selectionEnd.y);
                    double sx = sval[0];
                    double sy = sval[1];
                    double ex = eval[0];
                    double ey = eval[1];
                    if (sx < ex) {
                        xAxisMin = sx;
                        xAxisMax = ex;
                    } else {
                        xAxisMin = ex;
                        xAxisMax = sx;
                    }
                    if (sy < ey) {
                        yAxisMin = sy;
                        yAxisMax = ey;
                    } else {
                        yAxisMin = ey;
                        yAxisMax = sy;
                    }
                    selectionStart = null;
                    selectionEnd = null;
                    update();
                }
            } else {
                selectionStart = null;
                selectionEnd = null;
                updateWithRescale();
            }
            ZoomCoordinates newZoom = new ZoomCoordinates(xAxisMin, xAxisMax, yAxisMin, yAxisMax);
            firePropertyChange("Zoom Event", oldZoom, newZoom);
            
        }
    }//GEN-LAST:event_formMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private class Series{

        private DoubleBuffer xData;
        private DoubleBuffer yData;
        private int[] xPoints = null;
        private int[] yPoints = null;
        private double maxX = -Double.MAX_VALUE;
        private double maxY = -Double.MAX_VALUE;
        private double minX = Double.MAX_VALUE;
        private double minY = Double.MAX_VALUE;
        private boolean connect = true;
        private Color dataColor = Color.red;

        private Series(DoubleBuffer ydata) {
            yData = ydata;
            xData = null;
            xPoints = new int[yData.capacity()];
            yPoints = new int[yData.capacity()];
        }

        private Series(DoubleBuffer xdata, DoubleBuffer ydata) {
            xData = xdata;
            yData = ydata;
            xPoints = new int[yData.capacity()];
            yPoints = new int[yData.capacity()];
        }

        private void findYMaxMin() {
            yData.rewind();
            maxY = -Double.MAX_VALUE;
            minY = Double.MAX_VALUE;
            while (yData.hasRemaining()) {
                double point = yData.get();
                if (point > maxY) {
                    maxY = point;
                }
                if (point < minY) {
                    minY = point;
                }
            }

            if (maxY > yAxisMax) {
                if (maxY > 0) {
                    yAxisMax = maxY + maxY * 0.1;
                } else {
                    yAxisMax = maxY - maxY * 0.1;
                }
            }
            if (minY < yAxisMin) {
                if (minY > 0) {
                    yAxisMin = minY - minY * 0.1;
                } else {
                    yAxisMin = minY + minY * 0.1;
                }
            }
//            System.out.println("yAxisMax is " + maxY + " yAxisMin " + minY);
        }

        private void findXMaxMin() {
            if (xData != null) {
                xData.rewind();
                maxX = -Double.MAX_VALUE;
                minX = Double.MAX_VALUE;
                while (xData.hasRemaining()) {
                    double point = xData.get();
                    if (point > maxX) {
                        maxX = point;
                    }
                    if (point < minX) {
                        minX = point;
                    }
                }
            } else {
                minX = 0;
                maxX = yData.capacity() - 1;
            }
            if (maxX > xAxisMax) {
                xAxisMax = maxX;
            }
            if (minX < xAxisMin) {
                xAxisMin = minX;
            }
        }

        private void drawPoints() {
            // JPL: no point in computing values we aren't going to display 
            int startx = 0;
            int endx = yPoints.length;
            
//            if(xData != null && (xAxisMin < (Double.MAX_VALUE / 2.0)) && (xAxisMax > (-Double.MAX_VALUE / 2.0))){
//                double start = xData.get(0);
//                double stop = xData.get(yPoints.length - 1);
//                if(stop > start){
//                    startx = (int)(yPoints.length * ((xAxisMin - start) / (stop - start)));
//                    endx = (int)(yPoints.length * ((xAxisMax - start) / (stop - start)));
//                }
//            }
            
            
            
            for (int cnt = startx; cnt < endx; cnt++) {
                if (xData != null) {
                    Point p = getPoint(xData.get(cnt), yData.get(cnt));
//                    System.out.println("X " + xData.get(cnt) + " Y " + yData.get(cnt) + " Point " + p);
                    xPoints[cnt] = p.x;
                    yPoints[cnt] = p.y;
                } else {
                    Point p = getPoint(cnt, yData.get(cnt));
//                    System.out.println("X " + cnt + " Y " + yData.get(cnt) + " Point " + p);
                    xPoints[cnt] = p.x;
                    yPoints[cnt] = p.y;
                }

            }
            bufferGraphics.setColor(dataColor);
            Stroke oldStroke = bufferGraphics.getStroke();
            Stroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
            bufferGraphics.setStroke(stroke);
            if (connect) {
                bufferGraphics.drawPolyline(xPoints, yPoints, xPoints.length);
            } else {
                for (int cnt = 0; cnt < xPoints.length; cnt++) {
                    bufferGraphics.drawOval(xPoints[cnt], yPoints[cnt], 2, 2);
                }

            }
            bufferGraphics.setStroke(oldStroke);
        }
    }
}
