package org.jin.newsfinder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LruCacheTest {


    @Test
    void LRU_동작확인() {

        //given
        LruCache<String, String> cache = new LruCache<>(3);
        cache.put("A", "A");
        cache.put("B", "B");
        cache.put("C", "C");
        cache.get("A");

        //when
        cache.put("D", "D");

        //then
        assertThat(cache.keySet()).containsExactly("C", "A", "D");

    }
}
