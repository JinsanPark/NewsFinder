package org.jin.newsfinder.news;

import org.jin.newsfinder.queryVector.QueryVectorService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NewsServiceTest {

    @Test
    void 프로젝션_TO_검색결과() {

        NewsRepository newsRepository = mock(NewsRepository.class);
        NewsSearchProjection projection = mock(NewsSearchProjection.class);
        QueryVectorService queryVectorService = mock(QueryVectorService.class);

        when(projection.getTitle()).thenReturn("제목1");
        when(projection.getScore()).thenReturn(0.5);
        when(projection.getPublishedAt()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
        when(projection.getSummary()).thenReturn("요약1");
        when(projection.getUrl()).thenReturn("원본링크1");
        when(projection.getSource()).thenReturn("뉴스회사1");

        when(newsRepository.searchByVector(any(), anyDouble(), anyInt())).thenReturn(List.of(projection));
        when(queryVectorService.getVector(any())).thenReturn(new float[]{1.0f, 0.0f});

        List<NewsSearchResult> results = new NewsService(newsRepository, queryVectorService).search("테스트");

        assertThat(results).hasSize(1);
        NewsSearchResult result = results.get(0);
        assertThat(result).isEqualTo(new NewsSearchResult(
                "제목1", "요약1", "원본링크1", LocalDateTime.of(2026, 1, 1, 0, 0), "뉴스회사1", 0.5));

    }
}
