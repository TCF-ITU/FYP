package com.sytten.map_of_denmark.osm_parsing;

public enum LOD { // LOD = Level Of Detail


    /*
    Eventuelt relevante keys
     */

    Level1(1,0),
    Level2(2,0),
    Level3(3,0),
    Level4(4,0),
    Level5(5,0),
    Level6(6,0),

    // Highways - https://wiki.openstreetmap.org/wiki/Key:highway#Highway
    Motorway(Level6,1),
    Trunk(Level6,2),
    PrimaryRoad(Level5,3),
    SecondaryRoad(Level4,4),
    TertiaryRoad(Level3,5),
    ResidentialRoad(Level2, 6),
    ServiceRoad(Level2, 7),
    Unclassified(Level2, 8),
    Footway(Level1,9),
    FootPath(Level1,10),
    Cycleway(Level1, 11),
    Way(Level1, 12),

    // Railway -
    Railway(Level5, 101),
    Tram(Level3, 102),
    Subway(Level3,103),

    // Waterway -
    Lake(Level5, 201),
    River(Level5, 202),
    Canal(Level4,203),
    Stream(Level4,204),
    Ditch(Level3,205),
    Water(Level5,206),
    Ferry(Level4,207),

    // Building -
    church(Level3, 301),
    hospital(Level3,302),
    school(Level3,303),
    Building(Level3,304),
    DetatchedHouse(Level3,305),
    university(Level3, 306),
    kindergarten(Level3, 307),
    apartments(Level3, 308),
    residential(Level3, 309),
    house(Level3, 310),
    commercial(Level3, 311),

    // Areas -
    IndustrialArea(Level5, 501),
    ResidentialArea(Level5, 502),
    RetailArea(Level3, 503),
    Farmyard(Level5,504),
    SportsArea(Level3,505),

    // Nature -
    Coastline(Level6, 400),
    Forest(Level5,401),
    Ocean(Level5,402),
    Farmland(Level5, 403),
    Tree(Level1,404),
    Scrub(Level3,405),
    Grass(Level3,406),
    CountryBorder(Level6, 407);


    public final int value;

    LOD (LOD Level, int Data) {
        this.value = Level.value | Data;
    }

    LOD(int Level, int Data) {
        this.value = (1 << (Level - 1)) << 27 | Data;

        /*
        Søger i de 6 levels.
        (Advanced boolean)
        "<<" betyder bit shifting (rykker 1-tallet til venstre x gange).
        32 bits (in total) - 6 bits - 1 bit = 27
         */
    }

    public LOD GetLevel() {
        if ((this.value & Level1.value) != 0) { return Level1; }
        else if ((this.value & Level2.value) != 0) { return Level2; }
        else if ((this.value & Level3.value) != 0) { return Level3; }
        else if ((this.value & Level4.value) != 0) { return Level4; }
        else if ((this.value & Level5.value) != 0) { return Level5; }
        else if ((this.value & Level6.value) != 0) { return Level6; }

        return null;
    }

    boolean compare(int Level) {
        int LevelMask = 0xF8000000;
        int LODLevel = (this.value & LevelMask) >> 27;
        return LODLevel == Level;
    }

    // Vi må ikke compare - øv bøv
    boolean Equals(LOD That) {
        return (this.GetLevel().value == That.GetLevel().value);
    }

    boolean NotEquals(LOD That) {
        return (this.GetLevel().value != That.GetLevel().value);
    }

    boolean Greater(LOD That) {
        return (this.GetLevel().value < That.GetLevel().value);
    }

    boolean Less(LOD That) {
        return (this.GetLevel().value > That.GetLevel().value);
    }

    boolean GreaterOrEqual(LOD That) {
        return (this.GetLevel().value >= That.GetLevel().value);
    }

    boolean LessOrEqual(LOD That) {

        LOD ThisLevel = this.GetLevel();
        LOD ThatLevel = That.GetLevel();

        if (ThisLevel == null)
            return false;

        if (ThatLevel == null)
            return false;

        return (Math.abs(ThisLevel.value) <= Math.abs(ThatLevel.value));
    }
}