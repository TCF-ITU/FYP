package com.sytten.map_of_denmark.osm_parsing.parser;

import com.sytten.map_of_denmark.osm_parsing.Model;

import javax.xml.stream.XMLStreamReader;

public interface OsmData {
    void Start(XMLStreamReader Reader, Model Model);
    void Append(XMLStreamReader Reader, Model Model);
    void End(Model Model);
}
