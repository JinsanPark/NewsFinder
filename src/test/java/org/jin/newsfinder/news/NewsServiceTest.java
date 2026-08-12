package org.jin.newsfinder.news;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import org.jin.newsfinder.embedding.FakeEmbeddingClient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NewsServiceTest {

    @Test
    void 점수_미만_필터링_테스트(){

        CachedNews news1 = new CachedNews("제목1",  "요약", "url1", "출처", LocalDateTime.now(), List.of(1.0,0.0));

        CachedNews news2 = new CachedNews("제목2", "요약", "url2", "출처", LocalDateTime.now(), List.of(1.0,1.0));

        CachedNews news3 = new CachedNews("제목3",  "요약", "url3", "출처", LocalDateTime.now(), List.of(0.0,1.0));

        NewsCache newsCache = mock(NewsCache.class);
        when(newsCache.getCachedNewsList()).thenReturn(List.of(news1, news2,news3));

        NewsService newsService = new NewsService(newsCache, new FakeEmbeddingClient());

        List<NewsSearchResult> results = newsService.search("테스트");

        assertThat(results).extracting(NewsSearchResult::title).containsExactly("제목1", "제목2");
    }

    @Test
    void 게시물_10개_제한_테스트(){

        List<CachedNews> cachedNewsList= new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            CachedNews cachedNews = new CachedNews("제목" + i, "요약", "url" + i, "출처", LocalDateTime.now(), List.of(1.0,0.0));
            cachedNewsList.add(cachedNews);
        }

        NewsCache newsCache = mock(NewsCache.class);
        when(newsCache.getCachedNewsList()).thenReturn(cachedNewsList);
        NewsService newsService = new NewsService(newsCache, new FakeEmbeddingClient());
        List<NewsSearchResult> results = newsService.search("테스트");

        assertThat(results).hasSize(10);

    }


}
