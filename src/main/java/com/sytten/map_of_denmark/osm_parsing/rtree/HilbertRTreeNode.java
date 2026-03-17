package com.sytten.map_of_denmark.osm_parsing.rtree;

import com.sytten.map_of_denmark.osm_parsing.geometry.Rectangle;
import com.sytten.map_of_denmark.osm_parsing.geometry.SpatialObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HilbertRTreeNode<T extends SpatialObject> implements Serializable {
    boolean m_IsLeaf;

    List<T> m_Objects;
    List<HilbertRTreeNode<T>> m_Children;

    Rectangle m_Bounds;

    public HilbertRTreeNode(boolean IsLeaf) {
        m_IsLeaf = IsLeaf;
        m_Objects = new ArrayList<>();
        m_Children = new ArrayList<>();
        m_Bounds = null;
    }

    public void InsertChild(HilbertRTreeNode<T> Node) {
        m_Children.add(Node);
        RecalculateBounds();
    }

    public void Insert(T Object) {
        m_Objects.add(Object);
        ExpandBoundingBox(Object);
    }

    private void ExpandBoundingBox(SpatialObject Object) {
        if (m_Bounds == null) {
            m_Bounds = new Rectangle(Object.GetBoundingBox());
        } else {
            m_Bounds.Expand(Object.GetBoundingBox());
        }
    }

    /* Assumes the Child bounds is already correct!!! */
    public void RecalculateBounds() {

        Rectangle Bounds = null;

        if (m_IsLeaf) {
            for (SpatialObject Object : m_Objects) {
                if (Bounds == null) {
                    Bounds = new Rectangle(Object.GetBoundingBox());
                } else {
                    Bounds.Expand(Object.GetBoundingBox());
                }
            }
        } else {
            for (HilbertRTreeNode<T> Node : m_Children) {
                if (Bounds == null) {
                    Bounds = new Rectangle(Node.m_Bounds);
                } else {
                    Bounds.Expand(Node.m_Bounds);
                }
            }
        }

        m_Bounds = Bounds;
    }

    public void QueryRegion(List<T> Objects, Rectangle Region) {
        if (m_IsLeaf) {
            for (T Object : m_Objects) {
                if (Object.GetBoundingBox().Intersects(Region)) {
                    Objects.add(Object);
                }
            }
        } else {
            for (HilbertRTreeNode<T> Child : m_Children) {
                if (Child.m_Bounds.Intersects(Region)) {
                    Child.QueryRegion(Objects, Region);
                }
            }
        }
    }

    public void DebugDraw(GraphicsContext Context, int Level) {
        javafx.scene.paint.Color[] colors = { javafx.scene.paint.Color.RED, javafx.scene.paint.Color.GREEN, javafx.scene.paint.Color.BLUE, javafx.scene.paint.Color.ORANGE, javafx.scene.paint.Color.PURPLE };


        Context.setStroke(colors[Level % colors.length]);
        Context.strokeRect(m_Bounds.getMinX(), m_Bounds.getMinY(), m_Bounds.GetWidth(), m_Bounds.GetHeight());

        if (m_IsLeaf) {

            Context.setFill(Color.GRAY);

            for (SpatialObject Object : m_Objects) {
                Rectangle r = Object.GetBoundingBox();

                Context.setStroke(javafx.scene.paint.Color.GRAY);
                Context.strokeRect(r.getMinX(), r.getMinY(), r.GetWidth(), r.GetHeight());
            }

        } else {
            for (HilbertRTreeNode<T> Child : m_Children) {
                Child.DebugDraw(Context, Level + 1);
            }
        }
    }
}
