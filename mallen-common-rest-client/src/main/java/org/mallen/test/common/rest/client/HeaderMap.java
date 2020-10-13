package org.mallen.test.common.rest.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 封装HashMap的Headers参数集合
 *
 * @author mallen
 * @date 6/3/19
 */
public class HeaderMap<K, V> {

    private Map<K, V> params;

    public static HeaderMap builder() {
        return new HeaderMap();
    }

    public HeaderMap() {
        params = new HashMap<>();
    }

    public HeaderMap<K, V> add(K key, V value) {
        params.put(key, value);
        return this;
    }

    public V get(K key) {
        return params.get(key);
    }

    public Boolean isNotEmpty() {
        return params != null && params.size() > 0;
    }

    public Set<K> keySet() {
        return params.keySet();
    }
}
