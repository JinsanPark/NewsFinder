package org.jin.newsfinder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LruCacheTest {


    @Test
    void LRU_get한_키가_늦게_나감() {

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

    @Test
    void 동시에_put하면_개수_맞음() throws InterruptedException {
        int threadCount = 8;
        int perThread = 1000;

        LruCache<String, Integer> cache = new LruCache<>(8000);

        CountDownLatch start = new CountDownLatch(1);
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {

            final int threadNum = t;
            threads[t] = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for (int i = 0; i < perThread; i++) {
                    cache.put(String.valueOf(threadNum * perThread + i), i);
                }
            });
            threads[t].start();
        }

        start.countDown();

        for (Thread thread : threads){
            thread.join();
        }
        assertThat(cache).hasSize(8000);
    }

    @Test
    @Timeout(10)
    void 동시_get해도_순회개수_캐시크기_일치() throws InterruptedException{
        LruCache<String, Integer> cache = new LruCache<>(1000);
        int threadCount = 8;

        for (int i = 0; i < 100; i++) {
            cache.put(String.valueOf(i), i);
        }

        CountDownLatch start = new CountDownLatch(1);
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {

            threads[t] = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for (int i = 0; i < 100; i++) {
                    cache.get(String.valueOf(i));
                }

            });
            threads[t].start();
        }

        start.countDown();

        for (Thread thread : threads){
            thread.join();
        }

        int count = 0;
        for (String key : cache.keySet()){
            count++;
        }

        assertThat(cache.size()).isEqualTo(100);
        assertThat(count).isEqualTo(100);

    }

}
