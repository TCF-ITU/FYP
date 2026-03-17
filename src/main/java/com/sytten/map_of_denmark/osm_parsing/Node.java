package com.sytten.map_of_denmark.osm_parsing;

import java.io.Serializable;

public class Node implements Serializable {
    public long id;
    public double lat, lon;
    public String name;
    public Node(double lat, double lon, long id) {
        this.lat = lat;
        this.lon = lon;
        this.id = id;
        this.name = null;
    }

    public Node(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
        this.id = -1;
        this.name = null;
    }
}
