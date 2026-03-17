package com.sytten.map_of_denmark.osm_parsing.parser;

import com.sytten.map_of_denmark.osm_parsing.Model;
import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;

import javax.xml.stream.XMLStreamReader;
import java.io.Serializable;

public class OsmNode implements OsmData, SpatialObject, Serializable {

    public float m_Lon;
    public float m_Lat;
    long m_Id;

    public long GetId() {
        return m_Id;
    }

    public float GetLon() {
        return m_Lon;
    }

    public float GetLat() {
        return m_Lat;
    }

    public void Start(XMLStreamReader Reader, Model Model) {
        m_Lon = Float.parseFloat(Reader.getAttributeValue(null, "lon"));
        m_Lat = Float.parseFloat(Reader.getAttributeValue(null, "lat"));
        m_Id = Long.parseLong(Reader.getAttributeValue(null, "id"));
    }

    public void Append(XMLStreamReader Reader, Model Model) {

    }

    public void End(Model Model) {
        Model.SetMissingNode(m_Id, this);
        Model.AppendNode(this);
    }
    public void setId (long id){
        m_Id = id;
    }

    public Rectangle GetBoundingBox() {
        return new Rectangle(m_Lon, m_Lat, m_Lon, m_Lat);
    }
}
