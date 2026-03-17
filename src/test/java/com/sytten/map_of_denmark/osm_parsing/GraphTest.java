package com.sytten.map_of_denmark.osm_parsing;

import com.sytten.map_of_denmark.osm_parsing.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {

    private Graph graph;

    @BeforeEach
    void basicGraph() {
        graph = new Graph(4);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);
    }


    @Test
    void testGetEdgeWeight() throws Exception {
        int weight = graph.getEdgeWeight(graph, 0, 1);
        assertEquals(4, weight);
    }

    @Test
    void testDijkstra() {
        PathResult result = graph.dijkstra(0);
        assertNotNull(result);
        assertEquals(0, result.distances[0]);
        assertEquals(1, result.distances[2]);
    }

    @Test
    void testGetPath() {
        int[] prev = new int[] {-1, 2, 0, 1};
        ArrayList<Integer> path = graph.getPath(0, 3, prev);
        List<Integer> expected = Arrays.asList(0, 2, 1, 3);
        assertEquals(expected, path);
    }
}
