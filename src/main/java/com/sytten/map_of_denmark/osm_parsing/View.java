package com.sytten.map_of_denmark.osm_parsing;

import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.parser.OsmRelation;
import com.sytten.map_of_denmark.osm_parsing.parser.OsmWay;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class View {
    private final Model model;
    public final Canvas canvas;
    private final GraphicsContext gc;
    private final Label scaleLabel;
    private final ComboBox<String> routeMode;
    private final TextField input;
    private final TextField input2;
    private final Button toggleDirectionButton;
    private final ListView<String> suggestionList = new ListView<>();
    private final Popup popup = new Popup();
    private final ListView<String> suggestionList2 = new ListView<>();
    private final Popup popup2 = new Popup();

    private List<Node> shortestPath = new ArrayList<>();
    private final Tooltip currentTooltip = new Tooltip();
    private final Affine trans = new Affine();
    private Rectangle m_ViewPort;
    private LOD currentZoomIndex = LOD.Level6;
    private double currentZoomLevel = 10.0;
    public boolean m_UpdateViewport = true;
    public boolean m_DisplayViewport = false;
    public boolean m_DisplayBoundingBoxes = false;

    double x1 = 100;
    double y1 = 100;
    double x2 = 200;
    double y2 = 800;
    public double zoomScale;

    public View(Model model, Stage stage) {
        this.model = model;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sytten/map_of_denmark/mapofdenmark_view.fxml"));
            BorderPane root = loader.load();

            this.canvas = (Canvas) root.lookup("#canvas");
            this.scaleLabel = (Label) root.lookup("#scaleLabel");
            this.routeMode = (ComboBox<String>) root.lookup("#routeMode");
            this.input = (TextField) root.lookup("#input");
            this.input2 = (TextField) root.lookup("#input2");
            this.toggleDirectionButton = (Button) root.lookup("#toggleDirectionButton");

            if (canvas == null) throw new RuntimeException("Canvas not found in FXML");

            this.gc = canvas.getGraphicsContext2D();

            canvas.setFocusTraversable(true);
            canvas.requestFocus();
            canvas.widthProperty().bind(stage.widthProperty());
            canvas.heightProperty().bind(stage.heightProperty());

            canvas.setOnMouseMoved(e -> {
                Point2D modelPoint = mousetoModel(e.getX(), e.getY());
                String name = getWayNameNear(modelPoint);
                if (name != null && !name.equals(currentTooltip.getText())) {
                    currentTooltip.setText(name);
                    Tooltip.install(canvas, currentTooltip);
                } else if (name == null) {
                    Tooltip.uninstall(canvas, currentTooltip);
                }
            });

            // Set up route mode options
            if (routeMode != null) {
                routeMode.getItems().addAll("Car", "Bike");
                routeMode.setValue("Car");
            }

            // Toggle visibility of input2 and routeMode
            toggleDirectionButton.setOnAction(e -> {
                boolean isVisible = input2.isVisible();
                input2.setVisible(!isVisible);
                input2.setManaged(!isVisible);
                routeMode.setVisible(!isVisible);
                routeMode.setManaged(!isVisible);
            });

            // Setup autocomplete for both inputs
            configureAutocomplete(input, suggestionList, popup);
            configureAutocomplete(input2, suggestionList2, popup2);

            // Scene and show
            Scene scene = new Scene(root);
            stage.setTitle("Draw Lines");
            stage.setScene(scene);
            stage.show();

            m_ViewPort = GetCurrentMapBounds();

            redraw();
            pan(-Projection.ProjectLon(model.m_MinimumLongitude), model.m_MaximumLatitude);
            zoom(0, 0, canvas.getHeight() / (model.m_MaximumLatitude - model.m_MinimumLatitude));

        } catch (IOException e) {
            throw new RuntimeException("Couldn't load FXML", e);
        }
    }

    private void configureAutocomplete(TextField inputField, ListView<String> listView, Popup popup) {
        listView.setMaxHeight(145);
        listView.setPrefWidth(200);

        inputField.setOnKeyReleased(e -> {
            String typed = inputField.getText().trim().toLowerCase();

            if (typed.isEmpty()) {
                popup.hide();
                return;
            }

            List<String> suggestions = model.autocomplete(typed);
            if (suggestions.isEmpty()) {
                popup.hide();
                return;
            }

            List<String> topSuggestions = suggestions.stream().limit(5).toList();
            listView.getItems().setAll(topSuggestions);

            if (!popup.isShowing()) {
                popup.getContent().setAll(listView);
                popup.show(inputField,
                        inputField.localToScreen(0, inputField.getHeight()).getX(),
                        inputField.localToScreen(0, inputField.getHeight()).getY());
            }
        });

        listView.setOnMouseClicked(event -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                inputField.setText(selected);
                popup.hide();
            }
        });

        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) popup.hide();
        });

        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    inputField.setText(selected);
                    popup.hide();
                }
            }
        });
    }

    Point2D ModelToWorld(float x, float y) {
        return new Point2D(x / 0.56, -y);
    }

    Point2D WorldToModel(float x, float y) {
        return new Point2D(0.56 * x, -y);
    }
    Rectangle GetCurrentViewBounds() {

        Point2D topLeft = mousetoModel(-150,canvas.getHeight() + 150);
        Point2D BottomRight = mousetoModel(canvas.getWidth() + 150, -150);

        return new Rectangle(
                Projection.InverseProjectLon(topLeft.getX()), Projection.InverseProjectLat(topLeft.getY()),
                Projection.InverseProjectLon(BottomRight.getX()),Projection.InverseProjectLat(BottomRight.getY())
        );
    }

    public void setShortestPath(List<Node> Path) {
        shortestPath = Path;
        redraw();
    }

    Rectangle GetCurrentMapBounds() {
        return new Rectangle(
                model.m_MinimumLongitude, model.m_MinimumLatitude,
                model.m_MaximumLongitude, model.m_MaximumLatitude
        );
    }

    void redraw() {
        gc.setTransform(new Affine());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(0.5/Math.sqrt(trans.determinant()));// NEW: 0.5 instead of 1 - testing

        //Rectangle ViewPort = GetCurrentViewBounds();

        List<OsmWay> Ways = new LinkedList<>();
        List<OsmRelation> Relations = new LinkedList<>();


        if (currentZoomIndex == LOD.Level1) {
            LOD[] Lods = {LOD.Level6, LOD.Level5, LOD.Level4, LOD.Level3, LOD.Level2, LOD.Level1};

            for (LOD Level : Lods) {
                var WaysTree = model.m_WaysLodMap.get(Level);
                var RelationsTree = model.m_RelationsLodMap.get(Level);

                if (WaysTree != null)
                    WaysTree.QueryRegionAppend(Ways, m_ViewPort);

                if (RelationsTree != null)
                    RelationsTree.QueryRegionAppend(Relations, m_ViewPort);
            }
        }
        else if (currentZoomIndex == LOD.Level2) {
            LOD Lods[] = {LOD.Level6, LOD.Level5, LOD.Level4, LOD.Level3, LOD.Level2};

            for (LOD Level : Lods) {
                var WaysTree = model.m_WaysLodMap.get(Level);
                var RelationsTree = model.m_RelationsLodMap.get(Level);

                if (WaysTree != null)
                    WaysTree.QueryRegionAppend(Ways, m_ViewPort);

                if (RelationsTree != null)
                    RelationsTree.QueryRegionAppend(Relations, m_ViewPort);
            }
        }
        else if (currentZoomIndex == LOD.Level3) {
            LOD Lods[] = {LOD.Level6, LOD.Level5, LOD.Level4, LOD.Level3};

            for (LOD Level : Lods) {
                var WaysTree = model.m_WaysLodMap.get(Level);
                var RelationsTree = model.m_RelationsLodMap.get(Level);

                if (WaysTree != null)
                    WaysTree.QueryRegionAppend(Ways, m_ViewPort);

                if (RelationsTree != null)
                    RelationsTree.QueryRegionAppend(Relations, m_ViewPort);
            }
        }
        else if (currentZoomIndex == LOD.Level4) {
            LOD Lods[] = {LOD.Level6, LOD.Level5, LOD.Level4};

            for (LOD Level : Lods) {
                var WaysTree = model.m_WaysLodMap.get(Level);
                var RelationsTree = model.m_RelationsLodMap.get(Level);

                if (WaysTree != null)
                    WaysTree.QueryRegionAppend(Ways, m_ViewPort);

                if (RelationsTree != null)
                    RelationsTree.QueryRegionAppend(Relations, m_ViewPort);
            }
        }
        else if (currentZoomIndex == LOD.Level5) {
            LOD Lods[] = {LOD.Level6, LOD.Level5};

            for (LOD Level : Lods) {
                var WaysTree = model.m_WaysLodMap.get(Level);
                var RelationsTree = model.m_RelationsLodMap.get(Level);

                if (WaysTree != null)
                    WaysTree.QueryRegionAppend(Ways, m_ViewPort);

                if (RelationsTree != null)
                    RelationsTree.QueryRegionAppend(Relations, m_ViewPort);
            }
        }
        else if (currentZoomIndex == LOD.Level6) {
            LOD Lods[] = {LOD.Level6};

            for (LOD Level : Lods) {
                var WaysTree = model.m_WaysLodMap.get(Level);
                var RelationsTree = model.m_RelationsLodMap.get(Level);

                if (WaysTree != null)
                    WaysTree.QueryRegionAppend(Ways, m_ViewPort);

                if (RelationsTree != null)
                    RelationsTree.QueryRegionAppend(Relations, m_ViewPort);
            }
        }

        System.out.println("Ways: " + Ways.size());

        for (OsmRelation Relation : Relations) {
            Relation.Draw(gc);
        }

        for (OsmWay Way : Ways) {
            Way.draw(gc, zoomScale);

            if (m_DisplayBoundingBoxes) {
                Way.DrawBoundingBox(gc);
            }
        }

        if (m_DisplayViewport) {
            DrawViewPort();
        }
        if (!shortestPath.isEmpty()) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(5/Math.sqrt(trans.determinant()));

            gc.beginPath();
            Node first = shortestPath.get(0);
            gc.moveTo(0.56*first.lon, -first.lat);

            for(Node node : shortestPath) {
                gc.lineTo(0.56*node.lon, -node.lat);
            }
            gc.stroke();
            gc.setStroke(Color.BLACK);
        }
        for (Line line : model.list) {
            line.draw(gc);
        }
    }

    private void updateCurrentZoomIndex() {
        double s = Math.sqrt(trans.determinant());
        if (s > 5000) currentZoomIndex = LOD.Level6;
        else if (s > 2000) currentZoomIndex = LOD.Level5;
        else if (s > 1000) currentZoomIndex = LOD.Level4;
        else if (s > 500) currentZoomIndex = LOD.Level3;
        else if (s > 100) currentZoomIndex = LOD.Level2;
        else currentZoomIndex = LOD.Level1;
    }

    void DrawViewPort() {

        gc.beginPath();

        Point2D TopLeft = Projection.Project(m_ViewPort.getMinX(), m_ViewPort.getMinY());
        Point2D BottomRight = Projection.Project(m_ViewPort.getMaxX(), m_ViewPort.getMaxY());

        var Width = BottomRight.getX() - TopLeft.getX();
        var Height = BottomRight.getY() - TopLeft.getY();

        gc.rect(TopLeft.getX(), TopLeft.getY(), Width, Height);

        gc.stroke();

    }

    void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);

        if (m_UpdateViewport) {
            m_ViewPort = GetCurrentViewBounds();
        }

        redraw();
    }

    void zoom(double dx, double dy, double factor) {
        pan(-dx, -dy);
        trans.prependScale(factor, factor);
        pan(dx, dy);

        Rectangle ViewPort = GetCurrentViewBounds();
        Rectangle MapBounds = GetCurrentMapBounds();

        float ScaleY = ViewPort.GetHeight() / MapBounds.GetHeight();

        currentZoomLevel = currentZoomLevel * factor;

        //System.out.println("[ZOOM] Scale% is: " + ScaleY);
        updateScaleLabel();

        if (currentZoomLevel > 300000) {
            currentZoomIndex = LOD.Level1;
            zoomScale = 6;
        } else if (currentZoomLevel > 200000 ) {
            currentZoomIndex = LOD.Level2;
            zoomScale = 5;
        } else if (currentZoomLevel > 50000) {
            currentZoomIndex = LOD.Level3;
            zoomScale = 4;
        } else if (currentZoomLevel > 20000) {
            currentZoomIndex = LOD.Level4;
            zoomScale = 3;
        } else if (currentZoomLevel > 4000) {
            currentZoomIndex = LOD.Level5;
            zoomScale = 2;
        } else {
            currentZoomIndex = LOD.Level6;
            zoomScale = 1;
        }

        System.out.println("[ZOOM] ZoomLevel is: " + zoomScale);
    }

    public double getZoomLevel() {
        return currentZoomLevel;
    }

    private int calculateZoomLevelFromScale(double scale) {
        if (scale < 100) return 1;
        if (scale < 500) return 2;
        if (scale < 2000) return 3;
        return 6;
    }

    public Point2D mousetoModel(double lastX, double lastY) {
        try {
            return trans.inverseTransform(lastX, lastY);
        } catch (   NonInvertibleTransformException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }

    }
    private void updateScaleLabel() {
        if (scaleLabel == null) return;

        double scale = Math.sqrt(trans.determinant());
        double metersPer100Pixels = 100 / scale;

        String text;

        if (metersPer100Pixels >= 1000) {
            text = String.format("Scale: %.1f km", metersPer100Pixels / 1000);
        } else if (metersPer100Pixels >= 1) {
            text = String.format("Scale: %.0f m", metersPer100Pixels);
        } else{
            text = String.format("Scale: %.2f mm", metersPer100Pixels);
        }

        scaleLabel.setText(text);
    }
    public String getSelectedRouteType() {
        if (routeMode != null) {
            return routeMode.getValue();
        }
        return "Car";
    }
    private String getWayNameNear(Point2D point) {
        double minDistance = Double.MAX_VALUE;
        String closestName = null;

        for (Way way : model.graphWays) {
            List<Node> nodes = way.getNodes();
            for (int i = 0; i < nodes.size() - 1; i++) {
                Point2D p1 = new Point2D(0.56 * nodes.get(i).lon, -nodes.get(i).lat);
                Point2D p2 = new Point2D(0.56 * nodes.get(i + 1).lon, -nodes.get(i + 1).lat);

                double distance = pointToDist(point, p1, p2);
                String name = way.getName();
                if (distance < minDistance && distance < 2.0 && name != null && !name.startsWith("(")) {
                    minDistance = distance;
                    closestName = name;
                }
            }
        }

        return closestName;
    }

    private double pointToDist(Point2D point1, Point2D point2, Point2D point3){
        double x = point3.getX() - point2.getX();
        double y = point3.getY() - point2.getY();

        if (x == 0 && y == 0) {
            return point1.distance(point2);
        }

        double t = ((point1.getX() - point2.getX()) * x + (point1.getY() - point2.getY()) * y) /
                (x * x + y * y);
        t = Math.max(0, Math.min(1, t));

        Point2D projection = new Point2D(point2.getX() + t * x, point2.getY() + t * y);
        return point1.distance(projection);
    }
}
