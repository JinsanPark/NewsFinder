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

        News news1 = new News("제목1", "본문", "요약", "url1", "출처", LocalDateTime.now());
        news1.setEmbedding("1.0,0.0");

        News news2 = new News("제목2", "본문", "요약", "url2", "출처", LocalDateTime.now());
        news2.setEmbedding("1.0,1.0");

        News news3 = new News("제목3", "본문", "요약", "url3", "출처", LocalDateTime.now());
        news3.setEmbedding("0.0,1.0");


        NewsRepository newsRepository = mock(NewsRepository.class);
        when(newsRepository.findAll()).thenReturn(List.of(news1, news2,news3));

        NewsService newsService = new NewsService(newsRepository, new FakeEmbeddingClient());

        List<NewsSearchResult> results = newsService.search("테스트");

        assertThat(results).extracting(NewsSearchResult::title).containsExactly("제목1", "제목2");
    }

    @Test
    void 게시물_10개_제한_테스트(){

        List<News> newsList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            News news = new News("제목" + i, "본문", "요약", "url" + i, "출처", LocalDateTime.now());
            news.setEmbedding("1.0,0.0");
            newsList.add(news);
        }

        NewsRepository newsRepository = mock(NewsRepository.class);
        when(newsRepository.findAll()).thenReturn(newsList);
        NewsService newsService = new NewsService(newsRepository, new FakeEmbeddingClient());
        List<NewsSearchResult> results = newsService.search("테스트");

        assertThat(results).hasSize(10);

    }


}
