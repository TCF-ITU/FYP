package com.sytten.map_of_denmark.address;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        TextField input = new TextField();
        TextArea output =  new TextArea();
        BorderPane pane = new BorderPane();

        pane.setTop(input);
        pane.setCenter(output);

        input.setOnAction(e->{
            Address a = Address.parse(input.getText());
            output.setText(a.toString());
        });

        Scene scene = new Scene(pane);
        stage.setTitle("Address Parsing");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}