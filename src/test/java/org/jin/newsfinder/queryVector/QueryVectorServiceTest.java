package org.jin.newsfinder.queryVector;


import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.jin.newsfinder.LruCache;
import org.jin.newsfinder.embedding.EmbeddingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QueryVectorServiceTest {

    private static final String MODEL = "테스트 모델";
    private EmbeddingClient embeddingClient;
    private QueryVectorCacheRepository queryVectorCacheRepository;
    private QueryVectorService queryVectorService;


    @BeforeEach
    void setUp() {
        embeddingClient = mock(EmbeddingClient.class);
        queryVectorCacheRepository = mock(QueryVectorCacheRepository.class);
        queryVectorService = new QueryVectorService(embeddingClient, queryVectorCacheRepository, MODEL);
    }

    @Test
    void LRU_캐시히트시_DB_조회_1번() {
        QueryVectorCache cached = new QueryVectorCache("테스트", MODEL, new float[]{1.0f, 0.0f}, LocalDateTime.now());
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.of(cached));
        queryVectorService.getVector("테스트");

        queryVectorService.getVector("테스트");

        verify(queryVectorCacheRepository, times(1)).findByNormalizedQueryAndModel(any(), any());
        verify(embeddingClient, never()).embedQuery(any());
    }

    @Test
    void 두번_불러도_API는_1번() {
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.empty());
        when(embeddingClient.embedQuery(any())).thenReturn(new float[]{0.1f, 0.1f});
        queryVectorService.getVector("테스트");

        float[] result = queryVectorService.getVector("테스트");

        assertThat(result).containsExactly(0.1f, 0.1f);
        verify(embeddingClient, times(1)).embedQuery(any());
    }

    @Test
    void 동시에_불러도_API는_1번() throws InterruptedException {
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.empty());
        when(embeddingClient.embedQuery(any())).thenAnswer(invocation -> {
            Thread.sleep(100);
            return new float[]{1.0f, 1.0f};
        });

        int threadCount = 2;
        CountDownLatch start = new CountDownLatch(1);
        Thread[] threads = new Thread[threadCount];

        for (int t = 0; t < threadCount; t++) {
            threads[t] = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                queryVectorService.getVector("테스트");
            });
            threads[t].start();
        }

        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        verify(embeddingClient, times(1)).embedQuery(any());
    }

    @Test
    void 캐시히트시_API_호출_안함() {
        QueryVectorCache cached = new QueryVectorCache("테스트", MODEL, new float[]{1.0f, 0.0f}, LocalDateTime.now());
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.of(cached));

        float[] result = queryVectorService.getVector("테스트");

        assertThat(result).containsExactly(1.0f, 0.0f);
        verify(embeddingClient, never()).embedQuery(any());
    }

    @Test
    void 캐시미스시_API_호출_및_저장() {
        ArgumentCaptor<QueryVectorCache> argumentCaptor = ArgumentCaptor.forClass(QueryVectorCache.class);
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.empty());
        when(embeddingClient.embedQuery(any())).thenReturn(new float[]{0.1f, 0.1f});

        float[] result = queryVectorService.getVector(" QVC  API  테스트 ");

        assertThat(result).containsExactly(0.1f, 0.1f);
        verify(queryVectorCacheRepository).save(argumentCaptor.capture());

        QueryVectorCache saved = argumentCaptor.getValue();
        assertThat(saved.getNormalizedQuery()).isEqualTo("qvc api 테스트");
        assertThat(saved.getModel()).isEqualTo(MODEL);
    }


}
