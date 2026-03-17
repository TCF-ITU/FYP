package com.sytten.map_of_denmark.osm_parsing;
import com.sytten.map_of_denmark.osm_parsing.TernarySearchTree;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.zip.ZipInputStream;

import javax.management.relation.Relation;
import javax.xml.stream.*;

import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;
import com.sytten.map_of_denmark.osm_parsing.parser.*;
import com.sytten.map_of_denmark.osm_parsing.rtree.HilbertRTree;
import com.sytten.map_of_denmark.osm_parsing.simpletable.SimpleTable;
import javafx.geometry.Point2D;
public class Model implements Serializable{
    public static final Set<String> carHighWayTypes = Set.of(
            "motorway", "trunk", "primary", "secondary", "tertiary",
            "residential", "unclassified", "road" // ← add more if needed
    );
    List<Line> list = new ArrayList<Line>();
    List<Way> ways = new ArrayList<Way>();
    Map<LOD, List<Way>> waysByLayer = new EnumMap<>(LOD.class);
    public List<Way> graphWays = new ArrayList<>();
    public List<Way> buildingWays = new ArrayList<>();
    public Graph road;
    /* Temporary Setup shit */
    Map<Long, OsmNode> m_IdToNode = new HashMap<>();
    HashMap<Long, Integer> idToGraphIndex = new HashMap<>();
    public transient Map<Integer, Node> graphIndexToNode = new HashMap<>();
    transient int[] componentRoot;
    private TernarySearchTree searchTree = new TernarySearchTree();
    LinkedList<OsmWay> m_Ways = new LinkedList<>();

    HashMap<LOD, LinkedList<OsmWay>> m_WaysLodMapTemp = new HashMap<>();
    transient HashMap<LOD, HilbertRTree<OsmWay>> m_WaysLodMap = new HashMap<>();

    transient HilbertRTree<OsmWay> m_WaysSpatial;

    HashMap<LOD, LinkedList<OsmRelation>> m_RelationsLodMapTemp = new HashMap<>();
    HashMap<LOD, HilbertRTree<OsmRelation>> m_RelationsLodMap = new HashMap<>();

    /* PreProcessing */
    List<OsmPreWay> m_PreWays = new LinkedList<>();
    List<OsmPreRelation> m_PreRelations = new LinkedList<>();

    HashMap<Long, OsmWay> m_WaysIds = new HashMap<>();
    HashMap<Long, OsmNode> m_NodeIds = new HashMap<>();

    float m_MinimumLatitude;
    float m_MinimumLongitude;
    float m_MaximumLatitude;
    float m_MaximumLongitude;

    double minlat, maxlat, minlon, maxlon;

    Graph carRoad;
    Graph bikeRoad;
    public List<OsmWay> allResolvedWays = new ArrayList<>();

    public void SetMissingWay(Long WaysId) {
        m_WaysIds.put(WaysId, null);
    }

    public void SetMissingWay(Long WaysId, OsmWay Way) {
        m_WaysIds.put(WaysId, Way);
    }

    public OsmWay GetMissingWay(Long WayId) {
        return m_WaysIds.get(WayId);
    }

    public void SetMissingNode(long NodeId) {
        m_NodeIds.put(NodeId, null);
    }

    public void SetMissingNode(long NodeId, OsmNode Node) {
        if (m_NodeIds.containsKey(NodeId)) {
            m_NodeIds.put(NodeId, Node);
        }
    }

    public OsmNode GetMissingNode(Long NodeId) {
        return m_NodeIds.get(NodeId);
    }

    static Model load(String filename) throws FileNotFoundException, IOException, ClassNotFoundException, XMLStreamException, FactoryConfigurationError {
        if (filename.endsWith(".obj")) {
            try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                Model model = (Model) in.readObject();
                model.buildRoad(RouteType.CAR);
                model.buildRoad(RouteType.BIKE);
                return model;
            }
        }
        return new Model(filename);
    }


    public Model(String filename) throws XMLStreamException, FactoryConfigurationError, IOException {
        if (filename.endsWith(".osm.zip")) {
            parseZIP(filename);
        } else if (filename.endsWith(".osm")) {
            ParseOSM(filename);
        } else {
            parseTXT(filename);
        }
        save(filename + ".obj");
    }

    void buildRoad(RouteType routeType) {
        if (routeType == RouteType.CAR && carRoad != null) return;
        if (routeType == RouteType.BIKE && bikeRoad != null) return;

        HashMap<Long, Node> everyNode = new HashMap<>();
        for (Way way : graphWays) {

            for (Node node : way.getNodes()) {
                everyNode.put(node.id, node);
            }
        }

        System.out.println("Building road graph with " + everyNode.size() + " nodes");

        Graph graph = new Graph(everyNode.size());
        idToGraphIndex.clear();
        graphIndexToNode = new HashMap<>();

        List<Long> sortedNodeIds = new ArrayList<>(everyNode.keySet());
        Collections.sort(sortedNodeIds);

        int index = 0;
        for (Long nodeId : sortedNodeIds) {
            Node node = everyNode.get(nodeId);
            idToGraphIndex.put(nodeId, index);
            graphIndexToNode.put(index, node);
            graph.addVertexData(index, String.valueOf(nodeId));
            index++;
        }
        int[] parent = new int[everyNode.size()];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }

        for (Way way : graphWays) {
            List<Node> nodes = way.getNodes();
            for (int i = 0; i < nodes.size() - 1; i++) {
                Node node1 = nodes.get(i);
                Node node2 = nodes.get(i + 1);

                Integer id1 = idToGraphIndex.get(node1.id);
                Integer id2 = idToGraphIndex.get(node2.id);

                if (id1 != null && id2 != null) {
                    float distance = calculateDistance(node1, node2);
                    int speed = (int) getEfficiancy(way.getType());
                    int intDistance = 0;

                    if(routeType == RouteType.CAR){
                        intDistance = Math.max(1, (int)((distance / 1000.0f) / (speed / 3600.0f)));
                    } else if (routeType == RouteType.BIKE) {
                        String type = way.getType();
                        if (type != null && List.of("motorway", "trunk", "primary").contains(type)) {
                            continue;
                        }
                        intDistance = Math.max(1, (int) distance);
                    }

                    graph.addEdge(id1, id2, intDistance);
                    int rootI = find(parent, id1);
                    int rootX = find(parent, id2);
                    if (rootI != rootX) {
                        parent[rootI] = rootX;
                    }
                }
            }


        }
        if(routeType == RouteType.CAR){
            carRoad = graph;
        } else if(routeType == RouteType.BIKE) {
            bikeRoad = graph;
        }

        componentRoot = new int[parent.length];
        for (int i = 0; i < parent.length; i++) {
            componentRoot[i] = find(parent, i);
        }

        Set<Integer> components = new HashSet<>();
        for (int i = 0; i < parent.length; i++) {
            components.add(find(parent, i));
        }
        if (components.size() > 1) {
            System.out.println("Connecting " + components.size() + " disconnected components...");
            connectComponents(parent, routeType);
        }
    }

    private void connectComponents(int[] parent, RouteType routeType) {
        Map<Integer, List<Integer>> compNodes = new HashMap<>();
        for (int i = 0; i < parent.length; i++) {
            int root = find(parent, i);
            compNodes.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }

        List<Integer> roots = new ArrayList<>(compNodes.keySet());
        for (int i = 0; i < roots.size(); i++) {
            for (int j = i + 1; j < roots.size(); j++) {
                int u = roots.get(i);
                int v = roots.get(j);

                float minDist = Float.MAX_VALUE;
                int bestA = -1, bestB = -1;
                for (int a : compNodes.get(u)) {
                    Node na = graphIndexToNode.get(a);
                    for (int b : compNodes.get(v)) {
                        Node nb = graphIndexToNode.get(b);
                        float d = calculateDistance(na, nb);
                        if (d < minDist) {
                            minDist = d;
                            bestA = a;
                            bestB = b;
                        }
                    }
                }

                if (bestA != -1 && bestB != -1) {
                    int carPenalty = 100000;
                    int bikePenalty = 500000;
                    int bias;

                    if(routeType == RouteType.CAR){
                        bias = Math.max(1, (int) minDist + carPenalty);
                    } else{
                        bias = Math.max(1, (int) minDist + bikePenalty);
                    }

                    if(routeType == RouteType.CAR){
                        carRoad.addEdge(bestA, bestB, bias);
                    }else{
                        bikeRoad.addEdge(bestA, bestB, bias);
                    }

                    parent[find(parent, bestA)] = find(parent, bestB);
                }
            }
        }

        for (int i = 0; i < parent.length; i++) {
            componentRoot[i] = find(parent, i);
        }
    }
    private int find(int[] parent, int a) {
        if (parent[a] != a) {
            parent[a] = find(parent, parent[a]);
        }
        return parent[a];
    }

    public List<Node> findShortestPath(int startIndex, int endIndex, RouteType routeType) {
        buildRoad(routeType);

        Graph graph;
        if (routeType == RouteType.CAR) {
            graph = carRoad;
        } else {
            graph = bikeRoad;
        }

        if (componentRoot == null || componentRoot[startIndex] != componentRoot[endIndex]) {
            System.out.println("No path: nodes are in different disconnected components.");
            List<Node> path = new ArrayList<>();
            Node start = graphIndexToNode.get(startIndex);
            if (start != null) {
                path.add(start);
            }
            return path;
        }

        PathResult result = graph.dijkstra(startIndex);
        List<Integer> pathIndices = graph.getPath(startIndex, endIndex, result.previous);

        List<Node> path = new ArrayList<>();
        for (int idx : pathIndices) {
            Node node = graphIndexToNode.get(idx);
            if (node != null) path.add(node);
        }

        System.out.println("Path indices: " + pathIndices);
        return path;
    }
    private void ParseOSMNodes(InputStream Input) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {

        XMLStreamReader Reader = XMLInputFactory.newInstance().createXMLStreamReader(Input);

        OsmData CurrentData = null;
        int DataLevel = 0;
        int CurrentLevel = 0;

        while (Reader.hasNext()) {

            var Tag = Reader.next();

            if (Tag == XMLStreamConstants.START_ELEMENT) {

                var Name = Reader.getLocalName();

                if (Name.equals("bounds")) {
                    m_MinimumLatitude = Float.parseFloat(Reader.getAttributeValue(null, "minlat"));
                    m_MinimumLongitude = Float.parseFloat(Reader.getAttributeValue(null, "minlon"));
                    m_MaximumLatitude = Float.parseFloat(Reader.getAttributeValue(null, "maxlat"));
                    m_MaximumLongitude = Float.parseFloat(Reader.getAttributeValue(null, "maxlon"));
                }

                if (Name.equals("node")) {

                    long NodeId = Long.parseLong(Reader.getAttributeValue(null, "id"));

                    if (m_NodeIds.containsKey(NodeId)) {
                        CurrentData = new OsmNode();
                        CurrentData.Start(Reader, this);
                        DataLevel = CurrentLevel;
                        CurrentLevel++;
                        continue;
                    }
                }

                CurrentLevel++;

            } else if (Tag == XMLStreamConstants.END_ELEMENT) {

                CurrentLevel--;

                if (CurrentData != null && CurrentLevel == DataLevel) {
                    CurrentData.End(this);
                    CurrentData = null;
                    DataLevel = 0;
                }
            }
        }
    }

    private float calculateDistance(Node node1, Node node2) {
        double a = 0.56 * 111320 * (node1.lon - node2.lon);
        double b = -(node1.lat - node2.lat) * 110540;
        return (float) Math.sqrt(a * a + b * b);
    }

    void save(String filename) throws FileNotFoundException, IOException {
        try (var out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    private void parseZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var input = new ZipInputStream(new FileInputStream(filename));
        input.getNextEntry();
        //ParseOSM(input);
        System.out.println("[PARSER] Zip not supported pls fix");
    }

    public void AppendNode(OsmNode Node) {
        m_IdToNode.put(Node.GetId(), Node);
    }

    public void AppendPreWay(OsmPreWay Way) {

        if (m_WaysIds.containsKey(Way.GetId())) {
            m_PreWays.add(Way);
        } else if (Way.GetLOD() != null) {
            m_PreWays.add(Way);
        }
    }


    public void AppendPreRelation(OsmPreRelation Relation) {
        m_PreRelations.add(Relation);
    }

    public void AppendWay(OsmWay Way) {

        LOD Level;

        if (Way.GetLOD() == null) {
            //System.out.println("[PARSER] Way has no LOD");
            Level = LOD.Level6;
            return;
        } else {
            Level = Way.GetLODLevel();
        }

        if (m_WaysLodMapTemp.containsKey(Level)) {
            m_WaysLodMapTemp.get(Level).add(Way);
        } else {
            m_WaysLodMapTemp.put(Level, new LinkedList<>());
            m_WaysLodMapTemp.get(Level).add(Way);
        }
    }

    public OsmNode GetNode(long Id) {
        return m_IdToNode.get(Id);
    }
    public void AppendRelation(OsmRelation Relation) {

        if (Relation.GetLOD() == null) {
            return;
        }

        LOD Level = Relation.GetLODLevel();

        if (m_RelationsLodMapTemp.containsKey(Level)) {
            m_RelationsLodMapTemp.get(Level).add(Relation);
        } else {
            m_RelationsLodMapTemp.put(Level, new LinkedList<>());
            m_RelationsLodMapTemp.get(Level).add(Relation);
        }
    }
    private void ParseOSMRelations(InputStream Input) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {

        XMLStreamReader Reader = XMLInputFactory.newInstance().createXMLStreamReader(Input);

        OsmData CurrentData = null;
        int DataLevel = 0;
        int CurrentLevel = 0;

        while (Reader.hasNext()) {

            int Tag = Reader.next();

            if (Tag == XMLStreamConstants.START_ELEMENT) {

                // TODO: String Intern??
                var Name = Reader.getLocalName();

                if (CurrentData != null) {
                    CurrentData.Append(Reader, this);
                }

                if (Name.equals("relation")) {
                    CurrentData = new OsmPreRelation();
                    CurrentData.Start(Reader, this);
                    DataLevel = CurrentLevel;
                    CurrentLevel++;
                } else {
                    CurrentLevel++;
                    continue;
                }


            } else if (Tag == XMLStreamConstants.END_ELEMENT) {

                CurrentLevel--;

                if (CurrentData != null && CurrentLevel == DataLevel) {
                    CurrentData.End(this);
                    CurrentData = null;
                    DataLevel = 0;
                }
            }

        }
    }

    private void ParseOSMWays(InputStream Input) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {

        XMLStreamReader Reader = XMLInputFactory.newInstance().createXMLStreamReader(Input);

        OsmData CurrentData = null;
        int DataLevel = 0;
        int CurrentLevel = 0;

        while (Reader.hasNext()) {

            var Tag = Reader.next();

            if (Tag == XMLStreamConstants.START_ELEMENT) {
                if (CurrentData != null) {
                    CurrentData.Append(Reader, this);
                }

                var Name = Reader.getLocalName();

                if (Name.equals("way")) {
                    CurrentData = new OsmPreWay();
                    CurrentData.Start(Reader, this);
                    DataLevel = CurrentLevel;
                    CurrentLevel++;
                    continue;
                } else {
                    CurrentLevel++;
                    continue;
                }

            } else if (Tag == XMLStreamConstants.END_ELEMENT) {

                CurrentLevel--;

                if (CurrentData != null && CurrentLevel == DataLevel) {
                    CurrentData.End(this);
                    CurrentData = null;
                    DataLevel = 0;
                }
            }
        }
    }
    private void ResolveOSMWays() {

        while (!m_PreWays.isEmpty()) {
            OsmPreWay Way = m_PreWays.getFirst();

            OsmWay CurrentWay = null;

            if (m_WaysIds.containsKey(Way.GetId())) {
                CurrentWay = new OsmWay(this, Way);
                SetMissingWay(Way.GetId(), CurrentWay);
            }

            if (Way.IsHouse()) {

            } else {
                if (CurrentWay != null) {
                    AppendWay(CurrentWay);
                    allResolvedWays.add(CurrentWay);
                } else {
                    OsmWay fallback = new OsmWay(this, Way);
                    AppendWay(new OsmWay(this, Way));
                    allResolvedWays.add(fallback);

                }
            }

            m_PreWays.removeFirst();
        }

        System.gc();


        final LOD[] Levels = {LOD.Level1, LOD.Level2, LOD.Level3, LOD.Level4, LOD.Level5, LOD.Level6};

        for (LOD Level : Levels) {
            m_WaysLodMap.put(Level, new HilbertRTree<>(m_WaysLodMapTemp.get(Level), 128));
        }

        m_WaysLodMapTemp.clear();

        System.gc();
    }

    private void ResolveOSMRelations() {

        while (!m_PreRelations.isEmpty()) {
            OsmPreRelation PreRelation = m_PreRelations.getFirst();

            if (!PreRelation.IsValid(this)) {
                m_PreRelations.removeFirst();
                continue;
            }

            OsmRelation Relation = new OsmRelation(this, PreRelation);

            AppendRelation(Relation);

            m_PreRelations.removeFirst();
        }

        final LOD[] Levels = {LOD.Level1, LOD.Level2, LOD.Level3, LOD.Level4, LOD.Level5, LOD.Level6};

        for (LOD Level : Levels) {
            m_RelationsLodMap.put(Level, new HilbertRTree<>(m_RelationsLodMapTemp.get(Level), 64));
        }
    }


    private void ParseOSM(String Filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {

        ParseOSMRelations(new BufferedInputStream(new FileInputStream(Filename)));
        System.out.println("[PARSER] Parsed PreRelations");

        ParseOSMWays(new BufferedInputStream(new FileInputStream(Filename)));
        System.out.println("[PARSER] Parsed PreWays");

        ParseOSMNodes(new BufferedInputStream(new FileInputStream(Filename)));
        System.out.println("[PARSER] Parsed Nodes");

        System.gc();

        // Resolving!
        ResolveOSMWays();

        m_NodeIds.clear();
        m_NodeIds = null;

        System.gc();

        ResolveOSMRelations();

        m_WaysIds.clear();
        m_WaysIds = null;

        System.gc();
        buildGraphWaysFromOsmWays();
    }



    private void parseTXT(String filename) throws FileNotFoundException {
        File f = new File(filename);
        try (Scanner s = new Scanner(f)) {
            while (s.hasNext()) {
                list.add(new Line(s.nextLine()));
            }
        }
    }

    public void add(Point2D p1, Point2D p2) {
        list.add(new Line(p1, p2));
    }

        public List<Way> getWaysForZoomLevel(int zoomLevel) {
        List<Way> result = new ArrayList<>();
        for (Map.Entry<LOD, List<Way>> entry : waysByLayer.entrySet())
            if (entry.getKey().compare(zoomLevel)) {
                result.addAll(entry.getValue());
            }
        return result;
    }

    public void addToSearchTree(String name) {
        searchTree.put(name);
    }

    public List<String> autocomplete(String prefix) {
        return searchTree.autocomplete(prefix);
    }

    public List<Way> getWaysForLayer(LOD lod) {
        return waysByLayer.getOrDefault(lod, Collections.emptyList());
    }
    private float getEfficiancy(String type) {
        if (type == null) return 30;
        switch (type) {
            case "motorway": return 110;
            case "trunk": return 90;
            case "primary": return 70;
            case "secondary": return 60;
            case "tertiary": return 50;
            case "residential": return 40;
            case "unclassified": return 30;
            case "service": return 20;
            default: return 30;
        }
    }
    public void buildGraphWaysFromOsmWays() {
        graphWays.clear();

        for (OsmWay osmWay : allResolvedWays) {
            if (!osmWay.isHighway()) continue;

            Way graphWay = osmWay.toWay();
            if (graphWay.getNodes().size() >= 2) {
                graphWays.add(graphWay);
            }
        }

    }
    public Model (){

    }

    public Node findNodeByName(String name) {
        for (Way way : ways) {
            for (Node node : way.getNodes()) {
                if (node.name != null && node.name.toLowerCase().contains(name.toLowerCase())) {
                    return node;
                }
            }
        }
        return null;
    }
}
