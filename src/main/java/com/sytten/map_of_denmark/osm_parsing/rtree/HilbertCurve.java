package com.sytten.map_of_denmark.osm_parsing.rtree;

import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;

import java.io.Serializable;

public class HilbertCurve implements Serializable {

    final static int BITS = 31;

    float m_MinimumX;
    float m_MinimumY;
    float m_MaximumX;
    float m_MaximumY;
    int m_HilbertOrder;


    public HilbertCurve(int HilbertOrder, float MinX, float MinY, float MaxX, float MaxY) {
        m_MinimumX = MinX;
        m_MinimumY = MinY;
        m_MaximumX = MaxX;
        m_MaximumY = MaxY;
        m_HilbertOrder = HilbertOrder;
    }

    public long GetHilbertValue(Rectangle Rect) {

        float MidX = (Rect.getMaxX() + Rect.getMinX()) / 2;
        float MidY = (Rect.getMaxY() + Rect.getMinY()) / 2;

        return GetHilbertValue(MidX, MidY);
    }

    public long GetHilbertValue(float CenterX, float CenterY) {

        float NormalizedX = (CenterX - m_MinimumX) / (m_MaximumX - m_MinimumX);
        float NormalizedY = (CenterY - m_MinimumY) / (m_MaximumY - m_MinimumY);

        int x = (int)(NormalizedX * ((1 << m_HilbertOrder) - 1));
        int y = (int)(NormalizedY * ((1 << m_HilbertOrder) - 1));

        return HilbertCoordsToIndex(x, y, m_HilbertOrder);
    }

    private long HilbertCoordsToIndex(int x, int y, int Order) {
        long d = 0;
        for (int s = Order - 1; s >= 0; s--) {
            int rx = ((x >> s) & 1);
            int ry = ((y >> s) & 1);
            d = d << 2 | ((rx * 3) ^ ry);

            // Rotate/flip quadrant appropriately
            if (ry == 0) {
                if (rx == 1) {
                    x = (1 << Order) - 1 - x;
                    y = (1 << Order) - 1 - y;
                }

                // Swap x and y
                int tmp = x;
                x = y;
                y = tmp;
            }
        }

        return d;
    }
}