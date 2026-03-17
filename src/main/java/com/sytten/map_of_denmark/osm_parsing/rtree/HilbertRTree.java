package com.sytten.map_of_denmark.osm_parsing.rtree;


import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;
import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.*;

public class HilbertRTree<T extends SpatialObject> implements Serializable {

    final static int MAX_CAPACITY = 100;
    final static int MAX_CHILD_CAPACITY = 4;

    HilbertRTreeNode<T> m_Root;
    HilbertCurve m_Curve;

    Rectangle m_GlobalBounds;

    public HilbertRTree(LinkedList<T> Objects, int HilbertOrder) {

        if (Objects == null)
            return;

        m_GlobalBounds = CalculateGlobalBounds(Objects);

        m_Curve = new HilbertCurve(HilbertOrder, m_GlobalBounds.getMinX(), m_GlobalBounds.getMinY(), m_GlobalBounds.getMaxX(), m_GlobalBounds.getMaxY());

        Objects.sort(new Comparator<SpatialObject>() {
            @Override
            public int compare(SpatialObject This, SpatialObject That) {
                return Long.compare(m_Curve.GetHilbertValue(This.GetBoundingBox()), m_Curve.GetHilbertValue(That.GetBoundingBox()));
            }
        });

        LinkedList<HilbertRTreeNode<T>> LeafNodes = BuildLeafNodes(Objects);

        System.gc();

        m_Root = BuildHilbertRTreeBottomUp(LeafNodes);
    }

    private Rectangle CalculateGlobalBounds(LinkedList<? extends SpatialObject> Objects) {
        Rectangle Bounds = null;

        for (SpatialObject Object : Objects) {
            if (Bounds == null) {
                Bounds = new Rectangle(Object.GetBoundingBox());
            } else {
                Bounds.Expand(Object.GetBoundingBox());
            }
        }

        return Bounds;
    }

    private HilbertRTreeNode<T> BuildHilbertRTreeBottomUp(LinkedList<HilbertRTreeNode<T>> Nodes) {

        if (Nodes.size() <= 1) {
            return Nodes.isEmpty() ? null : Nodes.getFirst();
        }

        LinkedList<HilbertRTreeNode<T>> ParentNodes = new LinkedList<>();

        for(int i = 0; i < Nodes.size(); i += MAX_CHILD_CAPACITY) {

            HilbertRTreeNode<T> Parent = new HilbertRTreeNode<T>(false);

            for (int j = i; j < Math.min(i + MAX_CHILD_CAPACITY, Nodes.size()); j++) {
                Parent.InsertChild(Nodes.get(j));
            }

            ParentNodes.add(Parent);
        }

        return BuildHilbertRTreeBottomUp(ParentNodes);
    }

    /* This will clear Objects in the LinkedList slowly, to prevent high memory usage. */
    private LinkedList<HilbertRTreeNode<T>> BuildLeafNodes(LinkedList<T> Objects) {

        LinkedList<HilbertRTreeNode<T>> Nodes = new LinkedList<>();

        HilbertRTreeNode<T> Leaf = new HilbertRTreeNode<>(true);
        Nodes.add(Leaf);

        int Index = 0;

        while (!Objects.isEmpty()) {
            T Object = Objects.getFirst();

            Leaf.Insert(Object);

            Objects.removeFirst();

            if (Index == MAX_CAPACITY) {
                Leaf = new HilbertRTreeNode<>(true);
                Nodes.add(Leaf);
                Index = 0;
            } else {
                Index++;
            }
        }
/* REMOVED THIS TO SWITCH TO LINKEDLIST CLEARING IMPLEMENTATION
        for(int i = 0; i < Objects.size(); i += MAX_CAPACITY) {

            HilbertRTreeNode<T> Leaf = new HilbertRTreeNode<T>(true);

            for (int j = i; j < Math.min(i + MAX_CAPACITY, Objects.size()); j++) {
                Leaf.Insert(Objects.get(j));
            }

            Nodes.add(Leaf);
        }
*/
        return Nodes;
    }

    public void DebugDraw(GraphicsContext Context) {
        m_Root.DebugDraw(Context, 0);
    }

    public List<T> QueryRegion(Rectangle Region) {

        if (m_Root == null) {
            return new ArrayList<>();
        }

        List<T> Objects = new ArrayList<>();

        m_Root.QueryRegion(Objects, Region);

        return Objects;
    }

    public void QueryRegionAppend(List<T> List, Rectangle Region) {
        if (m_Root == null) {
            return;
        }

        m_Root.QueryRegion(List, Region);
    }
}
