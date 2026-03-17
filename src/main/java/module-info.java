module com.sytten.map_of_denmark {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.management;
    requires java.xml;

    // allow FXML loader to reflect into your controllers and application
    opens com.sytten.map_of_denmark.osm_parsing to javafx.fxml;

    // (optional) export if you need other modules to see it
    exports com.sytten.map_of_denmark.osm_parsing;
}
