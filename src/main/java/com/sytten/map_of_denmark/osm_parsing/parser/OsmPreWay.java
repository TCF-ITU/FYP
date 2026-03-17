package com.sytten.map_of_denmark.osm_parsing.parser;

import com.sytten.map_of_denmark.osm_parsing.Importance;
import com.sytten.map_of_denmark.osm_parsing.LOD;
import com.sytten.map_of_denmark.osm_parsing.Model;

import javax.xml.stream.XMLStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OsmPreWay implements OsmData, Serializable {
    private boolean m_IsHighway = false;
    public ArrayList<Long> m_NodeIds;

    boolean m_IsHouse = false;

    LOD m_LevelOfDetail;

    private int m_WayId;

    public String type;

    public List<Long> GetNodeIds() {
        return m_NodeIds;
    }

    public boolean IsHouse() {
        return m_IsHouse;
    }

    public LOD GetLOD() {
        return m_LevelOfDetail;
    }

    public long GetId() {
        return m_WayId;
    }

    public OsmPreWay() {
        m_NodeIds = new ArrayList<>();
    }

    public void Start(XMLStreamReader Reader, Model Model) {

        m_WayId = Integer.parseInt(Reader.getAttributeValue(null, "id"));

    }

    public void Append(XMLStreamReader Reader, Model Model) {

        String Name = Reader.getLocalName().intern();

        if (Name.equals("nd")) { /* change this to int instead of long */
            long ReferenceId = Long.parseLong(Reader.getAttributeValue(null, "ref"));

            m_NodeIds.add(ReferenceId);

            Model.SetMissingNode(ReferenceId); /* to:do only append this if im needed in m_WaysIds OR if i have an LOD - AT THE END */
        }
        else if (Name.equals("tag")) {

            String Key = Reader.getAttributeValue(null, "k").intern();
            String Value = Reader.getAttributeValue(null, "v").intern();


            if (Key.equals("highway")) {
                if (Model.carHighWayTypes.contains(Value)) {
                    m_LevelOfDetail = Importance.getImportance(Key, Value);
                    m_IsHighway = true;
                    type = Value;
                } else {
                    m_IsHighway = false; // explicitly ignore "service", "track", etc.
                }
            } else if (Key.equals("name")) {

                // Get Name and save it later on. - Maybe pass it into search tree from here?
                Model.addToSearchTree(Value);
            } else if (Key.equals("natural") || Key.equals("railway") || Key.equals("waterway") || Key.equals("building") || Key.equals("landuse")) {
                m_LevelOfDetail = Importance.getImportance(Key, Value);
            }


        }


    }

        // Get other stuff here.


    public boolean isHighway() {
        return m_IsHighway;
    }
    public void setHighway(boolean highway) {
        this.m_IsHighway = highway;
    }

    public void End(Model Model) {

        if (m_IsHouse) {

        } else {
            Model.AppendPreWay(this);   // Actually set this to something else?
        }
    }
}
