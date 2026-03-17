package com.sytten.map_of_denmark.osm_parsing.geometry;

import java.io.Serializable;

public class Rectangle implements Serializable {

    float x1;
    float y1;
    float x2;
    float y2;

    public Rectangle(float x1, float y1, float x2, float y2) {

        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
    }

    public Rectangle(Rectangle That) {
        this.x1 = That.x1;
        this.y1 = That.y1;
        this.x2 = That.x2;
        this.y2 = That.y2;
    }

    public boolean Intersects(Rectangle that) {
        return !(x1 > that.x2 || that.x1 > x2 || y1 > that.y2 || that.y1 > y2);
    }

    public boolean Contains(float x, float y) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public void Expand(float x, float y) {
        x1 = Math.min(x1, x);
        x2 = Math.max(x2, x);
        y1 = Math.min(y1, y);
        y2 = Math.max(y2, y);
    }

    public void Expand(Rectangle that) { // Expands this rectangle to fully contain that rectangle.
        x1 = Math.min(x1, that.x1);
        x2 = Math.max(x2, that.x2);
        y1 = Math.min(y1, that.y1);
        y2 = Math.max(y2, that.y2);
    }

    public float GetSize() {
        return GetWidth() * GetHeight();
    }

    public float CalculateExpansion(float x, float y) {
        float newx1 = Math.min(x1, x);
        float newx2 = Math.max(x2, x);
        float newy1 = Math.min(y1, y);
        float newy2 = Math.max(y2, y);

        return (newx2 - newx1) * (newy2 - newy1);
    }

    public float GetWidth() {
        return x2 - x1;
    }

    public float GetHeight() {
        return y2 - y1;
    }

    public float getMinX() { // NEW
        return x1;
    }

    public float getMinY() { // NEW
        return y1;
    }

    public float getMaxX() { // NEW
        return x2;
    }

    public float getMaxY() { // NEW
        return y2;
    }
}
