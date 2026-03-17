package com.sytten.map_of_denmark.drawing;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        String path ="data/lines8.txt";
        var model = new Model(path);
        var view = new View(model, stage);
        var controller = new Controller(model, view);
    }
    public static void main(String[] args) {
        launch();
    }
}