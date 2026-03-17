package com.sytten.map_of_denmark.osm_parsing.parser;

import com.sytten.map_of_denmark.osm_parsing.*;
import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.xml.stream.XMLStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OsmWay implements OsmData, SpatialObject, Serializable {
    private List<Long> nodeIds = new ArrayList<>();
    private String name;
    private String type;
    static List<OsmNode> g_TempNodes = new ArrayList<>();
    static boolean g_IsHouse = false;
    boolean m_IsHighway = false;
    float[] m_Coords;

    public List<OsmNode> m_Nodes = new ArrayList<>();

    LOD m_LevelOfDetail;

    public OsmWay() {

    }
    public boolean isHighway() {
        return m_IsHighway;
    }

    public OsmWay(Model Model, OsmPreWay Data) {

        List<Long> NodeIds = Data.GetNodeIds();
        m_IsHighway = Data.isHighway();
        type = Data.type;

        m_Coords = new float[NodeIds.size() * 2];

        for(int i = 0; i < NodeIds.size(); i++) {

            OsmNode Node = Model.GetMissingNode(NodeIds.get(i));

            if (Node == null) {
                System.out.print("");
            }

            m_Coords[i*2+0] = Node.GetLon();
            m_Coords[i*2+1] = Node.GetLat();
            m_Nodes.add(Node);
        }

        m_LevelOfDetail = Data.GetLOD();
        // Do some stuff here.
    }

    public LOD GetLOD() {
        return m_LevelOfDetail;
    }

    public LOD GetLODLevel() {

        if (m_LevelOfDetail == null)
            return null;

        return m_LevelOfDetail.GetLevel();
    }

    public float[] GetCoords() {
        return m_Coords;
    }

    public void draw(GraphicsContext gc, double zoomScale) {
        boolean isPolygon = false;

        if (m_Coords[0] == m_Coords[m_Coords.length-2] && m_Coords[1] == m_Coords[m_Coords.length-1]) {
            isPolygon = true;
        }

        gc.beginPath();
        float FirstX = Projection.ProjectLon(m_Coords[0]);
        float FirstY = Projection.ProjectLat(m_Coords[1]);
        gc.moveTo(FirstX, FirstY);

        for (int i = 2; i < m_Coords.length; i += 2) {
            float x = Projection.ProjectLon(m_Coords[i]);
            float y = Projection.ProjectLat(m_Coords[i + 1]);

            gc.lineTo(x, y);
        }

        if (m_Coords == null || m_Coords.length < 4) {
            System.out.println("[DEBUG] Skipping way with insufficient coords.");
            return;
        }

        Color fillColor = getColorForLOD(m_LevelOfDetail);
        if (fillColor == null) fillColor = Color.web("FEFEE5");
        Color strokeColor = fillColor.deriveColor(0, 1, 0.8, 1.0);

        if (m_LevelOfDetail == LOD.Ferry) {
            strokeColor = Color.web("#1A237E"); // dark blue
        }

        gc.beginPath();

        gc.setStroke(strokeColor);
        gc.setLineWidth(getStrokeWidthFromTags(zoomScale) * 0.001);
        gc.moveTo(m_Coords[0] * 0.56, -m_Coords[1]);

        for (int i = 2; i < m_Coords.length; i += 2) {
            gc.lineTo(m_Coords[i] * 0.56, -m_Coords[i + 1]);
        }

        if (isPolygon) {
            gc.setFill(fillColor);
            gc.closePath();
            gc.fill();
        }
        gc.stroke();
    }

    public void DrawBoundingBox(GraphicsContext gc) {

        Rectangle Bounds = GetBoundingBox();

        Point2D TopLeft = Projection.Project(Bounds.getMinX(), Bounds.getMinY());
        Point2D BottomRight = Projection.Project(Bounds.getMaxX(), Bounds.getMaxY());

        var Width = BottomRight.getX() - TopLeft.getX();
        var Height = BottomRight.getY() - TopLeft.getY();

        gc.beginPath();
        gc.rect(TopLeft.getX(), TopLeft.getY(), Width, Height);
        gc.stroke();
    }


    public void Start(XMLStreamReader Reader, Model Model) {
        return;
    }

    public void Append(XMLStreamReader Reader, Model Model) {
        System.out.println("[DEBUG] Appending way: " + Reader.getAttributeValue(null, "id"));
        var Name = Reader.getLocalName();

        if (Name.equals("nd")) {

            long ReferenceId = Long.parseLong(Reader.getAttributeValue(null, "ref"));

            OsmNode Node = Model.GetNode(ReferenceId);

            if (Node != null) {
                g_TempNodes.add(Node);
                m_Nodes.add(Node);
            }

        } else if (Name.equals("tag")) {

            var Key = Reader.getAttributeValue(null, "k");
            var Value = Reader.getAttributeValue(null, "v");

            if (Key.equals("highway")) {
                System.out.println("[DEBUG] Highway tag found: " + Value);
                m_IsHighway = true;
                m_LevelOfDetail = Importance.getImportance(Key, Value);
                this.type = Value;

            } else if (Key.equals("name")) {

                // Get Name and save it later on. - Maybe pass it into search tree from here?
                this.name = Value;
                Model.addToSearchTree(Value); // Gets send to our TernarySearchTree


            } else if (Name.equals("nd")) {
                long referenceId = Long.parseLong(Reader.getAttributeValue(null, "ref"));
                nodeIds.add(referenceId); // <-- collect the ID

                OsmNode node = Model.GetNode(referenceId);
                if (node != null) {
                    g_TempNodes.add(node);
                }
            }
        }
    }

    public void End(Model Model) {

        m_Coords = new float[2 * g_TempNodes.size()];

        for(int i = 0; i < g_TempNodes.size(); i++) {
            m_Coords[i * 2] = g_TempNodes.get(i).m_Lon;
            m_Coords[i * 2 + 1] = g_TempNodes.get(i).m_Lat;
        }

        g_TempNodes.clear();

        Model.AppendWay(this);
        if (m_IsHighway) {
            Way graphWay = toWay();
            Model.graphWays.add(graphWay);
        }
    }
    public List<Long> getNodeIds() {
        return nodeIds;
    }

    public Rectangle GetBoundingBox() {

        float MinimumX = m_Coords[0];
        float MaximumX = m_Coords[0];
        float MinimumY = m_Coords[1];
        float MaximumY = m_Coords[1];

        for(int i = 2; i < m_Coords.length; i+=2) {

            float x = m_Coords[i+0];
            float y = m_Coords[i+1];

            if (x < MinimumX) MinimumX = x;
            if (x > MaximumX) MaximumX = x;
            if (y > MinimumY) MinimumY = y;
            if (y < MaximumY) MaximumY = y;
        }

        return new Rectangle(MinimumX, MinimumY, MaximumX, MaximumY);
    }

    public Way toWay() {
        ArrayList<Node> nodes = new ArrayList<>();
        for (OsmNode osmNode : m_Nodes) {
            if (osmNode != null) {
                nodes.add(new Node(osmNode.m_Lat, osmNode.m_Lon, osmNode.GetId()));
            }
        }
        Way way = new Way(nodes);
        way.setType(this.type);

        return way;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }

    private double getStrokeWidthFromTags(double zoomScale) {
        if (type != null) {
            return switch (type) {
                case "motorway" -> Math.max(0.01, 3.0 / zoomScale);
                case "trunk"    -> Math.max(0.009, 2.5 / zoomScale);
                case "primary"  -> Math.max(0.008, 2.0 / zoomScale);
                case "secondary"-> Math.max(0.007, 1.5 / zoomScale);
                case "tertiary" -> Math.max(0.006, 1.2 / zoomScale);
                default         -> Math.max(0.005, 0.5 / zoomScale);
            };
        }

        //if (tags.containsKey("building")) return 0.8;
        return (1.0 / zoomScale) * 0.1;
    }

    private Color getColorForLOD(LOD lod) {
        return switch (lod) {
            case Motorway -> Color.web("E892A2");
            case PrimaryRoad, ServiceRoad -> Color.web("FDD7A1");
            case SecondaryRoad -> Color.web("F6FABB");
            case ResidentialRoad, Unclassified, TertiaryRoad -> Color.web("FFFFFF");
            case Trunk -> Color.web("FBC0AC");
            case Footway, FootPath -> Color.web("F9A196");
            case Cycleway -> Color.web("6A6AF9");
            case Way, Forest -> Color.web("ADD19E");
            case Scrub -> Color.web("C8D7AB");
            case Grass -> Color.web("CDEBB0");
            case Tree -> Color.web("B1CDA3");
            case Water, River, Stream, Ditch -> Color.web("AAD3DF");
            case Farmland -> Color.web("EEF0D5");
            case DetatchedHouse, Building -> Color.web("D9D0C9");
            case IndustrialArea -> Color.web("EBDBE8");
            case RetailArea -> Color.web("FFD6D1");
            case Farmyard -> Color.web("F5DCBA");
            case SportsArea -> Color.web("dffce2");
            case ResidentialArea -> Color.web("e0dfdf");
            default -> Color.web("FEFEE5");
        };
    }

    public void setType(String type) {
        this.type = type;
    }
}
