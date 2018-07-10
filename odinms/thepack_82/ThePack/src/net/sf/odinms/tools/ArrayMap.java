package net.sf.odinms.tools;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ArrayMap<K, V> extends AbstractMap<K, V> implements Serializable {

    static final long serialVersionUID = 9179541993413738569L;

    static class Entry<K, V> implements Map.Entry<K, V>, Serializable {

        static final long serialVersionUID = 9179541993413738569L;
        protected K key;
        protected V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            return (key == null ? e.getKey() == null : key.equals(e.getKey())) &&
                    (value == null ? e.getValue() == null : value.equals(e.getValue()));
        }

        @Override
        public int hashCode() {
            int keyHash = (key == null ? 0 : key.hashCode());
            int valueHash = (value == null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
    private Set<? extends java.util.Map.Entry<K, V>> entries = null;
    private ArrayList<Entry<K, V>> list;

    public ArrayMap() {
        list = new ArrayList<Entry<K, V>>();
    }

    public ArrayMap(Map<K, V> map) {
        list = new ArrayList<Entry<K, V>>();
        putAll(map);
    }

    public ArrayMap(int initialCapacity) {
        list = new ArrayList<Entry<K, V>>(initialCapacity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        if (entries == null) {
            entries = new AbstractSet<Entry<K, V>>() {

                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return list.iterator();
                }

                @Override
                public int size() {
                    return list.size();
                }
            };
        }
        return (Set<java.util.Map.Entry<K, V>>) entries;
    }

    @Override
    public V put(K key, V value) {
        int size = list.size();
        Entry<K, V> entry = null;
        int i;
        if (key == null) {
            for (i = 0; i < size; i++) {
                entry = (list.get(i));
                if (entry.getKey() == null) {
                    break;
                }
            }
        } else {
            for (i = 0; i < size; i++) {
                entry = (list.get(i));
                if (key.equals(entry.getKey())) {
                    break;
                }
            }
        }
        V oldValue = null;
        if (i < size) {
            oldValue = entry.getValue();
            entry.setValue(value);
        } else {
            list.add(new Entry<K, V>(key, value));
        }
        return oldValue;
    }
}
