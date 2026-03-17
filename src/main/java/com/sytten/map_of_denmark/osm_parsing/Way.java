package com.sytten.map_of_denmark.osm_parsing;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
public class Way implements Serializable{
    private ArrayList<Node> nodes;
    double[] coords;
    int importance;
    String name;
    String type;
    private int speedLimit;


    public Way(ArrayList<Node> way) {
        this.importance = importance;
        this.nodes = way;
        coords = new double[way.size() * 2];
        for (int i = 0 ; i < way.size() ; ++i) {
            var node = way.get(i);
            coords[2 * i] = 0.56 * node.lon;
            coords[2 * i + 1] = -node.lat;
        }
    }

    public Way(ArrayList<Node> wayNodes, int ordinal) {
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        if(name != null){
            return name;
        } else {
            return ("");
        }
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void draw(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(coords[0], coords[1]);
        for (int i = 2 ; i < coords.length ; i += 2) {
            gc.lineTo(coords[i], coords[i+1]);
        }
        gc.stroke();
    }

    public int getImportance() { // NEW: returns importance.
        return importance;
    }

}
