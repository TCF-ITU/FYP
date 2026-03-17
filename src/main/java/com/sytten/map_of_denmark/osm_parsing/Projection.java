package com.sytten.map_of_denmark.osm_parsing;

import javafx.geometry.Point2D;

public class Projection {

    private float m_X;
    private float m_Y;
    private float m_Width;
    private float m_Height;

    private float m_ScaleX;
    private float m_ScaleY;



    public static Point2D Project(float Lon, float Lat) {
        return new Point2D(ProjectLon(Lon), ProjectLat(Lat));
    }

    public static float ProjectLon(float Lon) {
        return 0.56f * Lon;
    }

    public static float ProjectLat(float Lat) {
        return -Lat;
    }

    public static float InverseProjectLon(double Lon) {
        return ((float)(Lon)) / 0.56f;
    }

    public static float InverseProjectLon(float Lon) {
        return Lon / 0.56f;
    }

    public static float InverseProjectLat(double Lat) {
        return (float)Math.abs((float)Lat);
    }

    public static float InverseProjectLat(float Lat) {
        return Math.abs(Lat);
    }
}
