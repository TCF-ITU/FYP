package com.sytten.map_of_denmark.osm_parsing;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import javafx.scene.input.MouseButton;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    private Node startNode;
    private Node endNode;
    double lastX;
    double lastY;
    public Controller(Model model, View view) {
        view.canvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
        });

        /* Debug shit */
        view.canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F1) {
                view.m_UpdateViewport = !view.m_UpdateViewport;
            }
            if (e.getCode() == KeyCode.F2) {
                view.m_DisplayViewport = !view.m_DisplayViewport;
            }
            if (e.getCode() == KeyCode.F3) {
                view.m_DisplayBoundingBoxes = !view.m_DisplayBoundingBoxes;
            }
        });

        view.canvas.setOnMouseDragged(e -> {

            double dx = e.getX() - lastX;
            double dy = e.getY() - lastY;
            view.pan(dx, dy);


            lastX = e.getX();
            lastY = e.getY();
        });

        view.canvas.widthProperty().addListener((Observable, OldValue, NewValue) -> {
            float DeltaX = NewValue.floatValue() - OldValue.floatValue();
            view.pan(DeltaX / 2, 0);
        });

        view.canvas.heightProperty().addListener((Observable, OldValue, NewValue) -> {
            float DeltaY = NewValue.floatValue() - OldValue.floatValue();
            view.pan(0, DeltaY / 2);
        });

        view.canvas.setOnScroll(e -> {
            double factor = e.getDeltaY();
            view.zoom(e.getX(), e.getY(), Math.pow(1.01, factor));
            view.redraw();
        });

        view.canvas.setFocusTraversable(true);
        view.canvas.requestFocus();



        view.canvas.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2) {
                Point2D modelCoords = view.mousetoModel(e.getX(), e.getY());
                Node closest = findClosestNode(model, modelCoords);

                if(startNode == null) {
                    startNode = closest;
                    System.out.println("Starting node selected" + startNode.id);
                } else if(endNode == null) {
                    endNode = closest;
                    System.out.println("Ending node selected" + endNode.id);
                    findAndDisplayPath(model, view);
                } else {
                    startNode = closest;
                    endNode = null;
                    System.out.println("New starting node selected" + startNode.id);
                }
            }
        });
    }

    private Node findClosestNode(Model model, Point2D point){
        Node closest = null;
        double minDist = Double.MAX_VALUE;
        System.out.println("Looking for node near: " + point.getX() + "," + point.getY());
        for (Way way : model.graphWays) {
            for (Node node : way.getNodes()) {
                double nodeX = 0.56 * node.lon;
                double nodeY = -node.lat;
                double distance = point.distance(nodeX, nodeY);

                if (distance < minDist) {
                    minDist = distance;
                    closest = node;
                }
            }
        }
        System.out.println("Clicked at: " + point.getX() + ", " + point.getY());
        System.out.println("Closest node: " + closest.id + " at " + (0.56 * closest.lon) + ", " + (-closest.lat));
        return closest;
    }

    private void findAndDisplayPath(Model model, View view) {
        String selected = view.getSelectedRouteType();
        RouteType routeType;
        if(selected.equals("Car")) {
            routeType = RouteType.CAR;
        } else {
            routeType = RouteType.BIKE;
        }
        System.out.println("Finding path from " + startNode.id + " to " + endNode.id);
        if(model.carRoad == null){
            model.buildRoad(RouteType.CAR);
        }else if(model.bikeRoad == null){
            model.buildRoad(RouteType.BIKE);
        }

        int startIndex = model.idToGraphIndex.get(startNode.id);
        int endIndex = model.idToGraphIndex.get(endNode.id);

        List<Node> path = model.findShortestPath(startIndex, endIndex, routeType);
        System.out.println("Path contains " + path.size() + " nodes");
        view.setShortestPath(path);
        view.redraw();
        if (path.size() == 1) {
            System.out.println("⚠ No path found. Nodes likely in separate components.");
        }
    }
}