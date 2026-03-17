package com.sytten.map_of_denmark.osm_parsing.parser;

import com.sytten.map_of_denmark.osm_parsing.Importance;
import com.sytten.map_of_denmark.osm_parsing.LOD;
import com.sytten.map_of_denmark.osm_parsing.Model;

import javax.xml.stream.XMLStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OsmPreRelation implements OsmData, Serializable {

    public class RelationPart {

        private Long m_Id;
        private boolean m_Inner;
        private boolean m_Outer;
        private boolean m_Backwards;

        public Long GetId() {
            return m_Id;
        }

        public boolean IsInner() {
            return m_Inner;
        }

        public boolean IsOuter() {
            return m_Outer;
        }

        public boolean IsBackwards() {
            return m_Backwards;
        }

        public RelationPart(Long Id, boolean Backwards, boolean Inner, boolean Outer) {
            m_Id = Id;
            m_Inner = Inner;
            m_Outer = Outer;
            m_Backwards = Backwards;
        }

    }

    List<RelationPart> m_Parts;
    LOD m_LevelOfDetail = null;

    public OsmPreRelation() {
        m_Parts = new LinkedList<>();
    }

    public LOD GetLOD() {
        return m_LevelOfDetail;
    }

    public List<RelationPart> GetParts() {
        return m_Parts;
    }


    // Must be called after Parsing & Resolving of OsmWays
    public boolean IsValid(Model Model) {

        for (RelationPart Part : m_Parts) {
            if (Model.GetMissingWay(Part.GetId()) == null)
                return false;
        }

        return true;
    }

    public void Start(XMLStreamReader Reader, Model Model) {

    }

    public void Append(XMLStreamReader Reader, Model Model) {

        String Name = Reader.getLocalName();

        if (Name.equals("member")) {

            String Type = Reader.getAttributeValue(null, "type");

            String Role = Reader.getAttributeValue(null, "role");

            Long Ref = Long.parseLong(Reader.getAttributeValue(null, "ref"));

            if (Type.equals("way")) {
                if (Role.equals("inner")) {
                    m_Parts.add(new RelationPart(Ref, false, true, false));
                } else if (Role.equals("backward")) {
                    m_Parts.add(new RelationPart(Ref, true, false, true));
                } else {
                    m_Parts.add(new RelationPart(Ref, false, false, true));
                }
            }
        } else if (Name.equals("tag")) {

            String Key = Reader.getAttributeValue(null, "k");
            String Value = Reader.getAttributeValue(null, "v");

            if (Key.equals("landuse")) {

                m_LevelOfDetail = Importance.getImportance(Key, Value);

            }

        }
    }

    public void End(Model Model) {

        if (m_LevelOfDetail == null) {
            return;
        }

        for (RelationPart Part : m_Parts) {
            Model.SetMissingWay(Part.GetId());
        }

        // Append Relation to drawable.
        Model.AppendPreRelation(this);
    }
}
