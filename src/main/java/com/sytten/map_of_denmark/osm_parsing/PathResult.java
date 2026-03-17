package com.sytten.map_of_denmark.osm_parsing;

//Et objekt som holder på et Array af distancer mellem startpositionen og de respektive vertices
public class PathResult {
    public int[] distances;
    int[] previous;

    public PathResult(int[] distances, int[] previous) {
        this.distances = distances;
        this.previous = previous;
    }
}
