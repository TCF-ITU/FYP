package com.sytten.map_of_denmark.osm_parsing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TstTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model();

        model.addToSearchTree("Bækkelunden");
        model.addToSearchTree("Bækkegrunden");
        model.addToSearchTree("Bækkesunden");
        model.addToSearchTree("Blanklund");
        model.addToSearchTree("Blæklund");
    }

    @Test
    void testNothingFound() {
        List<String> results = model.autocomplete("Be");
        assertTrue(results.isEmpty(), "We dont have an address with this prefix");
    }

    @Test
    void testTstautocomplete1() {
        List<String> results = model.autocomplete("Bæ");
        assertTrue(results.contains("Bækkelunden"));
        assertTrue(results.contains("Bækkegrunden"));
        assertTrue(results.contains("Bækkesunden"));
    }

    @Test
    void testTstautocomplete2() {
        List<String> results = model.autocomplete("Bl");
        assertTrue(results.contains("Blanklund"));
        assertTrue(results.contains("Blæklund"));
    }
}
