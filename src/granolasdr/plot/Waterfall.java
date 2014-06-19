/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Waterfall.java
 *
 * Created on Dec 7, 2009, 5:48:11 PM
 */
package granolasdr.plot;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.DoubleBuffer;

/**
 *
 * @author matt
 */
public class Waterfall extends javax.swing.JPanel {
    private BufferedImage offscreen = null;
    private WritableRaster onscreen = null;
    private Graphics bufferGraphics;
    private int height = 0;
    private int width = 0;
    private int refreshScale = 4;
    private double leftEdge = 0.0;
    private double rightEdge = 1.0;
    private DoubleBuffer data;
    // private int max = 0;
    private double amp = 15;
    private double floor = 0.0;
    private double[] temp = null;
    private int[] newPix = null;
    private int currentRaster = 0;
    private Marker[] markers = new Marker[2];
    private TimeLine timeLine = new TimeLine();
    public static boolean usingCuda = true;
    
    private class TimeLine{
        private int[] pixels = new int[4096];
        private double timeValue = 0.0;
        private int yCoord = 0;
        
        public double computeAmplitude(int rgb){
            int blue = rgb & 0xFF;
            int green = (rgb >>> 8) & 0xFF;
            int red = (rgb >>> 16) & 0xFF;
            double delta = 1024.0 / (amp - floor);

            double value = 0;
            
            if(red == 0 && blue == 0 && green == 0){
                value = 0;
            } else if (red == 0 && green == 0) {
                value = blue - 128;
            } else if (blue == 255 && red == 0) {
                value = green + 128;
            } else if (green == 255 && (red + blue) == 256) {
                value = red + 384;
            } else if (red == 255 && blue == 0) {
                value = 896 - green;
            } else if (green == 0 && blue == 0) {
                value = 1152 - red;
            } else {
                value = 1024;
            }

            value = (value / delta) + floor;

            return(value);
        }
        
        public void setTimeLine(double time){
            // first clean up the old time line (if any)
            if(yCoord != 0){
                try{
                    offscreen.setRGB(0, yCoord, width, 1, pixels, 0, 0);
                }
                catch (Exception ex){
                }
            }
            if(time > 0.0 && time < 1.0){
                // store the new time line to the restoration array
                timeValue = time;
                yCoord = (int)(timeValue * height);
                offscreen.getRGB(0, yCoord, width, 1, pixels, 0, 0);
                for(int x = 0; x < width; x++){
                    pixels[x] ^= 0x00FFFFFF;
                }
                offscreen.setRGB(0, yCoord, width, 1, pixels, 0, 0);
                for(int x = 0; x < width; x++){
                    pixels[x] ^= 0x00FFFFFF;
                }
            }
        }
        
        public void getTimeLine(double[] array){
            double xScale = (double)(width - 1) / (double)array.length;
            double[] values = new double[width];
            for(int x = 0; x < width; x++){
                values[x] = computeAmplitude(pixels[x]);
            }
            
            for(int x = 0; x < array.length; x++)
            {
                double xPosition = x * xScale;
                int xIndex = (int)Math.floor(xPosition);
                double xWeight = xPosition - xIndex;
                array[x] = ((1.0 - xWeight) * values[xIndex]) + (xWeight * values[xIndex + 1]);
            }
        }
        
    }
    
    private class Marker{
        public double time;
        public double frequency;
        public int color = 0xFFFFFF;
        public int shape = 1;
        public boolean inUse = false;
        
        public int markWidth = 8;
        public int markHeight = 16;
        private int[][] pixelQueue = null;
        private double effectiveFrequency = 0.0;
        private int xCoord = 0;
        private int yCoord = 0;
        private int compliment = 0x00;
        
        public void drawMarker(){
            effectiveFrequency = (frequency - leftEdge) / (rightEdge - leftEdge);
            compliment = color ^ 0x00FFFFFF;
            if(inUse && frequency > leftEdge && frequency < rightEdge && time >= 0 && time <= 1.0){    
                xCoord = (int)(width * effectiveFrequency);
                yCoord = (int)(height * time);
                updateQueue();
                erase();
                
                switch(shape){
                    // case 1, diamond shaped
                    case 1:
                        for(int y = 0; y < markHeight + 1; y++){
                            for(int x = -markWidth; x < markWidth; x++){
                                if((y >>> 1) >= Math.abs(x)){
                                    try{
                                        if((y >>> 1) != Math.abs(x)){
                                            offscreen.setRGB(xCoord + x, yCoord + y, color);
                                            offscreen.setRGB(xCoord + x, yCoord + ((markHeight << 1) - y - 1), color);
                                        }
                                        else{
                                            offscreen.setRGB(xCoord + x, yCoord + y, compliment);
                                            offscreen.setRGB(xCoord + x, yCoord + ((markHeight << 1) - y - 1), compliment);
                                        }
                                    }
                                    catch (Exception ex){
                                        System.err.println("could not draw marker diamond");
                                    }
                                }
                            }
                        }
                        break;
                    // case 3: cross shaped
                    case 2:
                        // vertical part
                        for(int y = 0; y < markHeight + refreshScale; y++){
                            for(int x = -2; x < 3; x++){
                                try{
                                    if(x == -2 || x == 2 || y == 0 || y == markHeight - 1){
                                        offscreen.setRGB(xCoord + x, yCoord + y, compliment);
                                    }
                                    else{
                                        offscreen.setRGB(xCoord + x, yCoord + y, color);
                                    }
                                }
                                catch (Exception ex){
                                    System.err.println("could not draw marker plus");
                                }
                            }
                        }
                        // horizontal part
                        for(int y = (markHeight >>> 1) - 2; y <= (markHeight >>> 1) + 2; y++){
                            for(int x = -markWidth; x < markWidth; x++){
                                try{
                                    if(x < -1 || x > 1){
                                        if(x == -markWidth || x == (markWidth-1) || y == ((markHeight >>> 1) - 2) || y == ((markHeight >>> 1) + 2)){
                                            offscreen.setRGB(xCoord + x, yCoord + y, compliment);
                                        }
                                        else{
                                            offscreen.setRGB(xCoord + x, yCoord + y, color);
                                        }
                                    }
                                }
                                catch (Exception ex){
                                    System.err.println("could not draw marker plus");
                                }
                            }
                        }
                        break;
                    // default: triangle shaped
                    default:
                        for(int y = 0; y < markHeight-1; y++){
                            for(int x = -markWidth; x < markWidth; x++){
                                if(Math.abs(x) <= (y >>> 1)){
                                    try{
                                        if((y >>> 1) == Math.abs(x)){
                                            offscreen.setRGB(xCoord + x, yCoord + y, compliment);
                                        }
                                        else{
                                            offscreen.setRGB(xCoord + x, yCoord + y, color);
                                        }
                                    }
                                    catch (Exception ex){
                                        System.err.println("could not draw marker triangle");
                                    }
                                }
                            }
                        }
                        for(int x = -markWidth; x < markWidth; x++){
                            try{
                                offscreen.setRGB(xCoord + x, yCoord + markHeight - 1, compliment);
                            }
                            catch (Exception ex){
                            }
                        }
                        break;
                }
                
            }
        }

        private void updateQueue(){
            if(pixelQueue == null || pixelQueue.length < (markWidth << 1) || pixelQueue[0].length < ((markHeight + refreshScale) << 1)){
                pixelQueue = new int[markWidth << 1][(markHeight + refreshScale) << 1];
                for(int y = 0; y <= (markHeight << 1); y++){
                    for(int x = -markWidth; x < markWidth; x++){
                        try{
                            pixelQueue[x+markWidth][y] = offscreen.getRGB(xCoord + x, yCoord + y);
                        }
                        catch (Exception ex){
                            System.err.println(ex.toString());
                            pixelQueue[x+markWidth][y] = color;
                        }
                    }
                }
            }
            else{
                for(int y = (markHeight << 1) + refreshScale; y >= refreshScale; y--){
                    for(int x = 0; x < (markWidth << 1); x++){
                        pixelQueue[x][y] = pixelQueue[x][y-refreshScale];
                    }
                }
                for(int y = 0; y < refreshScale; y++){
                    for(int x = -markWidth; x < markWidth; x++){
                        try{
                            pixelQueue[x+markWidth][y] = offscreen.getRGB(xCoord + x, yCoord + y);
                        }
                        catch (Exception ex){
                            pixelQueue[x+markWidth][y] = color;
                        }
                    }
                }
            }
        }
        
        public void erase(){
            if(pixelQueue != null){
                for(int y = 0; y <= (markHeight << 1) + refreshScale; y++){
                    for(int x =-markWidth; x < markWidth; x++){
                        try{
                            offscreen.setRGB(xCoord + x, yCoord + y, pixelQueue[x+markWidth][y]);
                        }
                        catch (Exception ex){
                        }
                    }
                }
                // pixelQueue = null;
            }
        }
        
        public int getPixel(){
            try{
                return(offscreen.getRGB(xCoord, yCoord));
            }
            catch (Exception ex){
                return(0xFFFFFF);
            }
        }
        
        public int getCover(int x, int y){
            if(!inUse){
                return(0);
            }
            x -= xCoord;
            y -= yCoord;
            
            if(y >= 0 && y <= markHeight){
                if(x >= -markWidth && x <= markWidth){
                    return(pixelQueue[x][y]);
                }
            }
            
            return(0);
        }
    }
    
    /** Creates new form Waterfall */
    public Waterfall() {
        initComponents();
    }
    
    public void setMarker(int index, double time, double freq){
        setMarker(index, time, freq, 0xFFFFFF, 1, 16);
    }
    
    public void setMarker(int index, double time, double freq, int color, int shape, int size){
        if(index < 2  && index >= 0){
            if(markers[index] == null){
                markers[index] = new Marker();
            }
            if(time >= 0){
                if(markers[index].inUse){
                    markers[index].erase();
                }
                markers[index].inUse = true;
                markers[index].frequency = freq;
                markers[index].time = time;
                markers[index].color = color;
                markers[index].shape = shape;
                markers[index].markHeight = size;
                markers[index].markWidth = size >>> 1;
                markers[index].drawMarker();
            }
            else{
                markers[index].erase();
                markers[index].inUse = false;
            }
        }
    }
    
    public double getMarkerAmplitude(int index){
        return(timeLine.computeAmplitude(markers[index].getPixel()));
    }
    
    public double[] getXAxis(){
        double[] returnValue = new double[2];
        returnValue[0] = leftEdge;
        returnValue[1] = rightEdge;
        return(returnValue);
    }
    
    public void setWidth(double start, double end){
        leftEdge = start;
        rightEdge = end;
    }
    
    public void setAmp(double a) {
        amp = a;
    }

    public void setFloor(double a) {
        floor = a;
    }
    
    public double getAmp() {
        return amp;
    }

    public double getFloor() {
        return floor;
    }
    
    public void setData(DoubleBuffer d) {
        data = d;
    }   
    
    public void setTimeLine(double time) {
        timeLine.setTimeLine(time);
        repaint();
    }
    
    public void setRefreshScale(int scale){
        refreshScale = scale;
    }
    
    public int getRefreshScale(){
        return(refreshScale);
    }

    public void getTimeLine(double[] array){
        timeLine.getTimeLine(array);
    }
    
    public synchronized void updateData() {
        if(currentRaster <= 0){
            bufferGraphics.copyArea(0, 0, width, height - refreshScale, 0, refreshScale);
            currentRaster = refreshScale;
        }
        currentRaster--;
        
        data.rewind();

        double delta = 1024.0 / (amp - floor);
        int leftScale = (int)(leftEdge * data.capacity());
        int rightScale = (int)(rightEdge * data.capacity());
        
        // JPL: this thing tends to die whenever we change the display,
        // rewrite to handle exceptions gracefully
        try{
            
            if(temp == null || temp.length < data.capacity()){
                temp = new double[data.capacity()];
            }
            
            data.get(temp, 0, data.capacity());
            
            if(newPix == null || newPix.length < width){
                newPix = new int[width];    
            }
            
            else{
                for (int cnt = 0; cnt < width; cnt++) {
                    double xPosition =  leftScale + (cnt * ((double)(rightScale - leftScale - 1) / (double)width));
                    int xIndex = (int)Math.floor(xPosition);
                    double xScale = xPosition - xIndex;
                    double value = ((1.0 - xScale) * temp[xIndex]) + (xScale * temp[xIndex + 1]) ;
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    int v = (int) ((value - floor) * delta);

                    blue = 384 - Math.abs(v - 256);
                    blue = Math.max(blue, 0);
                    blue = Math.min(blue, 255);
                    blue += 256 * (Math.abs(v) / 1024);
                    green = 384 - Math.abs(v - 512);
                    green = Math.max(green, 0);
                    green = Math.min(green, 255);
                    green += 256 * (Math.abs(v) / 1024);
                    red = 384 - Math.abs(v - 768);
                    red = Math.max(red, 0);
                    red = Math.min(red, 255);
                    red += 128 * (Math.abs(v) / 1024);
                    
                    newPix[cnt] = 0xff000000 | (red << 16) | (green << 8) | blue;
                }
            }

            offscreen.setRGB(0, currentRaster, width, 1, newPix, 0, width);
            
            if(currentRaster == 0)
            {
                for(int i = 0; i < markers.length; i++){
                    if(markers[i] != null && markers[i].inUse){
                        markers[i].drawMarker();
                    }
                }
            }
            
            
            repaint();
        }
        catch (Exception ex){
            System.err.println("exception: " + ex.toString() + " thrown whilst redrawing the waterfall");
            height = getHeight();
            width = getWidth();
            
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(offscreen, 0, 0, this);
        super.paint(g);
    }


    private void createBackground() {
        offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreen = offscreen.getRaster();
        bufferGraphics = offscreen.getGraphics();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        height = getHeight();
        width = getWidth();
        
//        System.out.println("Height " + height + " Width " + width);
        createBackground();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setOpaque(false);

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
