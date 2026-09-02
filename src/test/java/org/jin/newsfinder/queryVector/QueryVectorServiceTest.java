package org.jin.newsfinder.queryVector;


import org.jin.newsfinder.embedding.EmbeddingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

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
    void LRU_캐시히트시_DB_조회_1번 (){
        QueryVectorCache cached = new QueryVectorCache("테스트", MODEL, new float[]{1.0f, 0.0f}, LocalDateTime.now());
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.of(cached));
        queryVectorService.getVector("테스트");

        queryVectorService.getVector("테스트");

        verify(queryVectorCacheRepository, times(1)).findByNormalizedQueryAndModel(any(), any());
        verify(embeddingClient, never()).embedQuery(any());
    }

    @Test
    void 두번_불러도_API는_1번(){
        when(queryVectorCacheRepository.findByNormalizedQueryAndModel(any(), any())).thenReturn(Optional.empty());
        when(embeddingClient.embedQuery(any())).thenReturn(new float[]{0.1f, 0.1f});
        queryVectorService.getVector("테스트");

        float[] result = queryVectorService.getVector("테스트");

        assertThat(result).containsExactly(0.1f, 0.1f);
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
