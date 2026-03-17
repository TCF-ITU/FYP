package com.sytten.map_of_denmark.osm_parsing;

import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;
import com.sytten.map_of_denmark.osm_parsing.rtree.HilbertRTree;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class RTreeTest {

    class TestRectangle implements SpatialObject {
        public float x1,y1;
        public float x2,y2;

        public TestRectangle(float x1, float y1, float x2, float y2) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
        }

        public Rectangle GetBoundingBox() {
            return new Rectangle(x1, y1, x2, y2);
        }
    };

    @Test
    void testRTreeCreation() {
        LinkedList<TestRectangle> TestSamples = new LinkedList<>();
        TestSamples.add(new TestRectangle(1, 1, 4, 4));
        TestSamples.add(new TestRectangle(4, 4, 8, 8));
        TestSamples.add(new TestRectangle(8, 8, 16, 16));

        HilbertRTree<TestRectangle> RTree = new HilbertRTree<>(TestSamples, 16);

        assertNotNull(RTree);

        assertEquals(RTree.QueryRegion(new Rectangle(0, 0, 16, 16)).size(), 3);
    }

    @Test
    void testRTreeQuery() {
        LinkedList<TestRectangle> TestSamples = new LinkedList<>();
        TestSamples.add(new TestRectangle(1, 1, 4, 4));
        TestSamples.add(new TestRectangle(4, 4, 8, 8));
        TestSamples.add(new TestRectangle(8, 8, 16, 16));

        HilbertRTree<TestRectangle> RTree = new HilbertRTree<>(TestSamples, 16);

        assertNotNull(RTree);

        assertEquals(RTree.QueryRegion(new Rectangle(0, 0, 16, 16)).size(), 3); // All
        assertEquals(RTree.QueryRegion(new Rectangle(1, 1, 2, 2)).size(), 1);   // Intersects
        assertEquals(RTree.QueryRegion(new Rectangle(1, 1, 4, 4)).size(), 2);   // Overlap & Contains
    }
}
