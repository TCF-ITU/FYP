package com.sytten.map_of_denmark.drawing;
import java.util.Iterator;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;

public class View {
    Canvas canvas = new Canvas(640, 480);;
    Affine trans = new Affine();
    Model model;

    GraphicsContext gc = canvas.getGraphicsContext2D();


    double x1;
    double y1;
    double x2;
    double y2;
    public View(Model model, Stage primaryStage) {
        this.model = model;
        primaryStage.setTitle("Draw Lines");
        BorderPane pane = new BorderPane(canvas);
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
        redraw();
    }
    void redraw(){
        Iterator it = model.lines.iterator();
        gc.setTransform(new Affine());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());


        while (it.hasNext()) {
            String fileline = (String) it.next();
            String[] coords = fileline.split(" ");
            double x1 = Double.parseDouble(coords[1]);
            double y1 = Double.parseDouble(coords[2]);
            double x2 = Double.parseDouble(coords[3]);
            double y2 = Double.parseDouble(coords[4]);
            gc.setLineWidth(0.2);
            gc.setTransform(trans);
            gc.beginPath();
            gc.moveTo(x1, y1);
            gc.lineTo(x2, y2);
            gc.stroke();
        }
    }
    void pan(double dx, double dy){
        trans.prependTranslation(dx, dy);
        redraw();
    }
    void zoom(double factor, double dx, double dy){
        trans.prependTranslation(-dx, -dy);
        trans.prependScale(factor, factor);
        trans.prependTranslation(dx, dy);
        redraw();
    }
    public Point2D mousetoModel(double X, double Y) {
        try {
            return trans.inverseTransform(X, Y);
        } catch (NonInvertibleTransformException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

}
