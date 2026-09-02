package org.jin.newsfinder;

import java.util.Map;
import java.util.LinkedHashMap;

public class LruCache<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    public LruCache(int capacity) {
        super(10, 0.75f, true); // LRU 사이즈는 축출 확인위해 10으로 결정.
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
