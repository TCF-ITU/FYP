package com.sytten.map_of_denmark.osm_parsing.parser;

import com.sytten.map_of_denmark.osm_parsing.LOD;
import com.sytten.map_of_denmark.osm_parsing.Model;
import com.sytten.map_of_denmark.osm_parsing.Projection;
import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
import javax.xml.stream.XMLStreamReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.io.Serializable;

public class OsmRelation implements SpatialObject, Serializable {

    private List<Float> m_OuterCoords = new LinkedList<>();
    private List<Float> m_InnerCoords = new LinkedList<>();

    private static final float INNER_COORDINATE_END_KEY = 1234.1234f;

    LOD m_LevelOfDetail;

    public OsmRelation(Model Model, OsmPreRelation Data) {

        m_LevelOfDetail = Data.GetLOD();

        List<OsmPreRelation.RelationPart> Parts = Data.GetParts();

        for (OsmPreRelation.RelationPart Part : Parts) {

            if (Part.IsInner()) {

                long WayId = Part.GetId();

                OsmWay Way = Model.GetMissingWay(WayId);

                if (Way == null)
                    continue;

                float[] Coordinates = Way.GetCoords();

                for(int i = 0; i < Coordinates.length; i++){
                    m_InnerCoords.add(Coordinates[i]);
                }

                /* Buffer between cutouts! */
                m_InnerCoords.add(INNER_COORDINATE_END_KEY);
                m_InnerCoords.add(INNER_COORDINATE_END_KEY);

            } else if (Part.IsOuter()) {

                long WayId = Part.GetId();

                OsmWay Way = Model.GetMissingWay(WayId);

                if (Way == null)
                    continue;

                float[] Coordinates = Way.GetCoords();

                for(int i = 0; i < Coordinates.length; i++){
                    m_OuterCoords.add(Coordinates[i]);
                }
            } else if (Part.IsBackwards()) {
                long WayId = Part.GetId();
                OsmWay Way = Model.GetMissingWay(WayId);

                if (Way == null)
                    continue;
                float[] Coordinates = Way.GetCoords();

                for (int i = Coordinates.length -1; i > 0; i--){
                    m_OuterCoords.add(Coordinates[i]);
                }
            }
        }
    }

    public LOD GetLOD() {
        return m_LevelOfDetail;
    }

    public LOD GetLODLevel() {
        return m_LevelOfDetail.GetLevel();
    }

    public void Draw(GraphicsContext Context) {

        if (m_OuterCoords.size() < 4)
            return;

        var OldRule = Context.getFillRule();

        Context.setFill(getColorForLOD(m_LevelOfDetail));

        Context.beginPath();

        Context.moveTo(
                Projection.ProjectLon(m_OuterCoords.get(0)),
                Projection.ProjectLat(m_OuterCoords.get(1))
        );

        for (int i = 2; i < m_OuterCoords.size(); i+=2) {
            Context.lineTo(
                    Projection.ProjectLon(m_OuterCoords.get(i)),
                    Projection.ProjectLat(m_OuterCoords.get(i + 1))
            );
        }

        Context.closePath();

        //int Index = 0;
//
        //Iterator<Float> Iter = m_InnerCoords.iterator();
//
        //while(Index < m_InnerCoords.size()) {
//
        //    float Lon = Iter.next();
        //    float Lat = Iter.next();
//
        //    if (Lon == INNER_COORDINATE_END_KEY || Lat == INNER_COORDINATE_END_KEY) {
        //        Context.closePath();
        //        Index += 2;
        //        continue;
        //    } else {
        //        // do stuff here now.
        //    }
//
        //    Index += 2;
        //}

        //Context.stroke();
        Context.fill();

        // Do stuff from here.

        //Context.setFillRule(FillRule.EVEN_ODD);



        Context.setFillRule(OldRule);
    }

    public Rectangle GetBoundingBox() {

        float MinimumX = m_OuterCoords.get(0);
        float MaximumX = m_OuterCoords.get(0);
        float MinimumY = m_OuterCoords.get(1);
        float MaximumY = m_OuterCoords.get(1);

        for(int i = 2; i < m_OuterCoords.size(); i+=2) {

            float x = m_OuterCoords.get(i+0);
            float y = m_OuterCoords.get(i+1);

            if (x < MinimumX) MinimumX = x;
            if (x > MaximumX) MaximumX = x;
            if (y > MinimumY) MinimumY = y;
            if (y < MaximumY) MaximumY = y;
        }

        return new Rectangle(MinimumX, MinimumY, MaximumX, MaximumY);
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
}



