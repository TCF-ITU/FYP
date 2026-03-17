package com.sytten.map_of_denmark.osm_parsing.simpletable;

import java.util.Objects;

public class SimpleTable<K,V> {

    private class Entry {
        private K Key;
        private V Value;

        public Entry(K Key, V Value) {
            this.Key = Key;
            this.Value = Value;
        }
    }

    final static float LOAD_CAPACITY = 0.65f;

    private int m_Capacity;
    private int m_Size;
    private Entry[] m_Entries;

    public SimpleTable() {
        m_Capacity = 4;
        m_Size = 0;
        m_Entries = (Entry[]) new SimpleTable.Entry[m_Capacity];
    }

    public SimpleTable(int Capacity) {
        m_Capacity = Capacity;
        m_Size = 0;
        m_Entries = (Entry[]) new SimpleTable.Entry[m_Capacity];
    }

    public int Size() {
        return m_Size;
    }

    public void Resize(int NewCapacity) {
        m_Capacity = NewCapacity;

        Entry[] OldTable = m_Entries;

        m_Entries = (Entry[]) new SimpleTable.Entry[m_Capacity];

        for(int i = 0; i < OldTable.length; i++) {

            if (OldTable[i] != null) {
                Put(OldTable[i].Key, OldTable[i].Value);
            }
        }
    }

    public void Put(K Key, V Value) {
        int Index = FindFreeIndex(Key);

        if ((float)m_Size / m_Entries.length > LOAD_CAPACITY) {
            Resize(m_Capacity * 2);
        }

        if (Index == -1) {
            Resize(m_Capacity*2);
            System.out.println("Index null, resize and go again.");
            Put(Key, Value);
            return;
        }

        m_Entries[Index] = new Entry(Key, Value);
        m_Size++;
    }

    public V get(K Key) {
        return Get(Key);
    }

    public void put(K Key, V Value) {
        Put(Key, Value);
    }

    public boolean containsKey(K Key) {
        return ContainsKey(Key);
    }

    public void clear() {
        Clear();
    }

    public V Get(K Key) {
        int Index = FindElement(Key);

        if (Index == -1) {
            return null;
        }

        return m_Entries[Index].Value;
    }

    public boolean ContainsKey(K Key) {
        int Index = FindElement(Key);

        return Index != -1;
    }

    public void Clear() {
        for (int i = 0; i < m_Entries.length; i++) {
            if (m_Entries[i] != null) {
                m_Entries[i].Value = null;
                m_Entries[i].Key = null;
                m_Entries[i] = null;
            }
        }

        m_Entries = null;
    }

    private int FindElement(K Key) {
        int Hash = Hash(Key);
        int Index = Hash & (m_Capacity - 1);

        while (Index < m_Entries.length) {
            if (m_Entries[Index] == null) {
                System.out.println("No such element");
                break;
            }

            if (m_Entries[Index] != null) {

                if (Objects.equals(m_Entries[Index].Key, Key)) {
                    return Index;
                }
            }

            Index++;
        }

        return -1;
    }

    private int FindFreeIndex(K Key) {
        int Hash = Hash(Key);
        int Index = Hash & (m_Capacity - 1);

        while (Index < m_Entries.length) {
            if (m_Entries[Index] == null) {
                return Index;
            } else if (Objects.equals(m_Entries[Index].Key, Key)) {
                return Index;
            } else if (m_Entries[Index].Value == null) {
                return Index;
            }

            Index++;
        }

        return -1;
    }

    private int Hash(K Key) {
        return Key.hashCode();
    }
}
