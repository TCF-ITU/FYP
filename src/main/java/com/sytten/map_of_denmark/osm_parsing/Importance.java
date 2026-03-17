package com.sytten.map_of_denmark.osm_parsing;

import java.util.HashMap;
import java.util.Map;

public class Importance {

    private static final Map<String, LOD> HIGHWAY_IMPORTANCE = new HashMap<>();
    private static final Map<String, LOD> RAILWAY_IMPORTANCE = new HashMap<>();
    private static final Map<String, LOD> WATERWAY_IMPORTANCE = new HashMap<>();
    private static final Map<String, LOD> BUILDING_IMPORTANCE = new HashMap<>();
    private static final Map<String, LOD> NATURE_IMPORTANCE = new HashMap<>();
    private static final Map<String, LOD> LANDUSE_IMPORTANCE = new HashMap<>();

    static {
        // Highway types
        HIGHWAY_IMPORTANCE.put("motorway", LOD.Motorway);
        HIGHWAY_IMPORTANCE.put("trunk", LOD.Trunk);
        HIGHWAY_IMPORTANCE.put("primary", LOD.PrimaryRoad);
        HIGHWAY_IMPORTANCE.put("secondary", LOD.SecondaryRoad);
        HIGHWAY_IMPORTANCE.put("tertiary", LOD.TertiaryRoad);
        HIGHWAY_IMPORTANCE.put("residential", LOD.ResidentialRoad);
        HIGHWAY_IMPORTANCE.put("unclassified", LOD.Unclassified);
        HIGHWAY_IMPORTANCE.put("service", LOD.ResidentialRoad);
        HIGHWAY_IMPORTANCE.put("footway", LOD.Footway);
        HIGHWAY_IMPORTANCE.put("cycleway", LOD.Cycleway);
        HIGHWAY_IMPORTANCE.put("path", LOD.Way);
        HIGHWAY_IMPORTANCE.put("track", LOD.Unclassified);
        HIGHWAY_IMPORTANCE.put("raceway", LOD.Way);
        HIGHWAY_IMPORTANCE.put("steps", LOD.Footway);
        HIGHWAY_IMPORTANCE.put("living_street", LOD.ResidentialRoad);
        HIGHWAY_IMPORTANCE.put("pedestrian", LOD.Footway);
        HIGHWAY_IMPORTANCE.put("corridor", LOD.Footway);

        // Railways
        RAILWAY_IMPORTANCE.put("rail", LOD.Railway);
        RAILWAY_IMPORTANCE.put("tram", LOD.Tram);
        RAILWAY_IMPORTANCE.put("subway", LOD.Subway);

        // Waterways
        WATERWAY_IMPORTANCE.put("river", LOD.River);
        WATERWAY_IMPORTANCE.put("canal", LOD.Canal);
        WATERWAY_IMPORTANCE.put("stream", LOD.Stream);
        WATERWAY_IMPORTANCE.put("ferry", LOD.Ferry);
        WATERWAY_IMPORTANCE.put("water", LOD.Water);
        WATERWAY_IMPORTANCE.put("lake", LOD.Lake);

        // Buildings
        BUILDING_IMPORTANCE.put("yes", LOD.Building);
        BUILDING_IMPORTANCE.put("school", LOD.school);
        BUILDING_IMPORTANCE.put("church", LOD.church);
        BUILDING_IMPORTANCE.put("hospital", LOD.hospital);
        BUILDING_IMPORTANCE.put("residential", LOD.Building);

        // Nature
        NATURE_IMPORTANCE.put("forest", LOD.Forest);
        NATURE_IMPORTANCE.put("ocean", LOD.Ocean);
        NATURE_IMPORTANCE.put("water", LOD.Water);
        NATURE_IMPORTANCE.put("scrub", LOD.Scrub);

        LANDUSE_IMPORTANCE.put("farmland", LOD.Farmland);
        LANDUSE_IMPORTANCE.put("forest", LOD.Forest);
        LANDUSE_IMPORTANCE.put("grass", LOD.Grass);
        LANDUSE_IMPORTANCE.put("industrial",LOD.IndustrialArea);
        LANDUSE_IMPORTANCE.put("residential",LOD.ResidentialArea);
    }

    public static LOD getImportance(String key, String value) {
        if (key == null || value == null) return LOD.Level2;

        key = key.toLowerCase();
        value = value.toLowerCase();

        LOD result = null;

        switch (key) {
            case "highway":
                result = HIGHWAY_IMPORTANCE.get(value);
                break;
            case "railway":
                result = RAILWAY_IMPORTANCE.get(value);
                break;
            case "waterway":
                result = WATERWAY_IMPORTANCE.get(value);
                break;
            case "building":
                result = BUILDING_IMPORTANCE.get(value);
                break;
            case "natural":
                result = NATURE_IMPORTANCE.get(value);
                break;
            case "landuse":
                result = LANDUSE_IMPORTANCE.get(value);
                break;

            case "leisure":
                result = LOD.SportsArea;
                break;
        }

        if (result == null) {
            //System.out.println("[IMPORTANCE] Unknown tag: " + key + " = " + value);
            return LOD.Level2; // Fallback LOD
        }

        return result;
    }
}
