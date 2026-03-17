package com.sytten.map_of_denmark.osm_parsing;

import com.sytten.map_of_denmark.osm_parsing.simpletable.SimpleTable;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, XMLStreamException, ClassNotFoundException {
        String filename = "data/bornholm.osm";
        var model = Model.load(filename);
        var view = new View(model, stage);
        new Controller(model, view);
    }

    public static void main(String[] args) {
        launch();
    }
}