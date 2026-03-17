package com.sytten.map_of_denmark.osm_parsing;

import java.lang.reflect.Field;
import java.util.*;

public class Graph {
    private Map<Integer, Map<Integer, Integer>> adjList;
    private int size;
    private String[] vertexData;

    //Dijkstras algoritme
    public PathResult dijkstra(int startVertex){
        //Vi laver et array som indeholder alle vertices forgængere
        //Vi initialiserer alle indexpositioner i arrayet til at indeholde -1
        int[] prev = new int[size];
        Arrays.fill(prev, -1);

        //Vi laver et nyt array som indeholder alle de korsteste distancer fra startvertexen
        //Vi initialiserer alle indexpositioner til at være uendelige, med undtagelse af startvertexen
        int[] dist = new int[size];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[startVertex] = 0;

        //Vi opretter en PQ som prioriterer vertices med en kortere distance
        PriorityQueue<int[]> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        priorityQueue.add(new int[]{startVertex, 0});

        //Vi laver en boolean til at holde styr på om vi har fundet den korteste distance
        boolean[] settled = new boolean[size];

        //Vi kører et loop så længe der fortsat er vertices i vores PQ
        while (!priorityQueue.isEmpty()) {
            int[] node = priorityQueue.poll();
            int u = node[0];

            //Vi sikrer os at vi ikke allerede har fundet den korteste distance
            if(settled[u]) continue;
            settled[u] = true;

            //Vi gennemgår den pågældende vertex naboer og finder distancen fra vores startvertex
            for(Map.Entry<Integer, Integer> entry : adjList.get(u).entrySet()) {
                int v = entry.getKey();
                int w = entry.getValue();

                //Vi beregner den nye distance og hvis den er bedre end den originale opdaterer vi dist og prev
                //Herefter sætter vi nabovertexen ind i vore PQ
                if(!settled[v]) {
                    int newDist = dist[u] + w;
                    if(newDist < dist[v]) {
                        dist[v] = newDist;
                        prev[v] = u;
                        priorityQueue.add(new int[]{v, newDist});
                    }
                }
            }
        }
        //Herefter returnerer vi et objekt af de korteste distancer, såvel som forskellige vertices forgængere
        return new PathResult(dist, prev);
    }


    public Graph(int size) {
        this.size = size;
        adjList = new HashMap<>();
        vertexData = new String[size];
        for (int i = 0; i < size; i++) {
            adjList.put(i, new HashMap<>());
            vertexData[i] = "";
        }
    }
    public ArrayList<Integer> getPath(int startVertex, int endVertex, int[]prev){
        ArrayList<Integer> path = new ArrayList<>();
        for(int i = endVertex; i != -1; i = prev[i]){
            path.add(i);
        }
        Collections.reverse(path);
        return path;
    }
    public void addEdge(int u, int v, int weight) {
        if (u >= 0 && u < size && v >= 0 && v < size) {
            adjList.get(u).put(v, weight);
            adjList.get(v).put(u, weight);
        }
    }

    //Forbinder en string med en specifik vertex, kan f.eks. bruges til at sammensætte med adresser
    public void addVertexData(int vertex, String data) {
        if (vertex >= 0 && vertex < size) {
            vertexData[vertex] = data;
        }
    }

    public int getEdgeWeight(Graph graph, int from, int to) throws Exception {
        Field adjListField = Graph.class.getDeclaredField("adjList");
        adjListField.setAccessible(true);
        Map<Integer, Map<Integer, Integer>> adjList = (Map<Integer, Map<Integer, Integer>>) adjListField.get(graph);
        return adjList.getOrDefault(from, Map.of()).getOrDefault(to, -1);
    }


}
