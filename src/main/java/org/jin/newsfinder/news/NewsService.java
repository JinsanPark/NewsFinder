package org.jin.newsfinder.news;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
public class NewsService {
    private static final  int MAX_SEARCH_SIZE = 10;
    //무관한 검색어 검색시 최고 점수 0.3아래. 연관된 검색어 검색시 최고 0.46;
    private static final  double MIN_SEARCH_SCORE = 0.35;
    private final NewsRepository newsRepository;
    private final EmbeddingClient embeddingClient;

    public NewsService(NewsRepository newsRepository, EmbeddingClient embeddingClient) {
        this.newsRepository = newsRepository;
        this.embeddingClient = embeddingClient;
    }

    public List<NewsSearchResult> search(String query) {

        float[] queryToVector = embeddingClient.embedQuery(query);

        List<NewsSearchProjection> newsList = newsRepository.searchByVector(queryToVector, MIN_SEARCH_SCORE, MAX_SEARCH_SIZE);
        List<NewsSearchResult> results = new ArrayList<>();

        for(NewsSearchProjection news : newsList) {
                results.add(new NewsSearchResult(
                        news.getTitle(),
                        news.getSummary(),
                        news.getUrl(),
                        news.getPublishedAt(),
                        news.getSource(),
                        news.getScore()
                ));
        }

        return results;
    }
}
