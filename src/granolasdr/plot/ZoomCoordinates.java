/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package granolasdr.plot;

import java.io.Serializable;

/**
 *
 * @author root
 */
public class ZoomCoordinates  implements Serializable {

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public ZoomCoordinates(double minx, double maxx, double miny, double maxy) {
        minX = minx;
        minY = miny;
        maxX = maxx;
        maxY = maxy;
    }

    /**
     * @return the minX
     */
    public double getMinX() {
        return minX;
    }

    /**
     * @param minX the minX to set
     */
    public void setMinX(double minX) {
        this.minX = minX;
    }

    /**
     * @return the maxX
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * @param maxX the maxX to set
     */
    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    /**
     * @return the minY
     */
    public double getMinY() {
        return minY;
    }

    /**
     * @param minY the minY to set
     */
    public void setMinY(double minY) {
        this.minY = minY;
    }

    /**
     * @return the maxY
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * @param maxY the maxY to set
     */
    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.minX) ^ (Double.doubleToLongBits(this.minX) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.maxX) ^ (Double.doubleToLongBits(this.maxX) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.minY) ^ (Double.doubleToLongBits(this.minY) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.maxY) ^ (Double.doubleToLongBits(this.maxY) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZoomCoordinates other = (ZoomCoordinates) obj;
        if (Double.doubleToLongBits(this.minX) != Double.doubleToLongBits(other.minX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxX) != Double.doubleToLongBits(other.maxX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.minY) != Double.doubleToLongBits(other.minY)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxY) != Double.doubleToLongBits(other.maxY)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String maxx = String.format("%.2f", maxX);
        String minx = String.format("%.2f", minX);
        String maxy = String.format("%.2f", maxY);
        String miny = String.format("%.2f", minY);
        return "X( " + minx + " , " + maxx + " ) Y( " + miny + " , " + maxy + " )";
    }

}
