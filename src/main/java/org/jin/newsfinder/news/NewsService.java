package org.jin.newsfinder.news;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.jin.newsfinder.embedding.EmbeddingSimilarity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



@Service
public class NewsService {
    private final static int MAX_SEARCH_SIZE = 10;
    //무관한 검색어 검색시 최고 점수 0.3아래. 연관된 검색어 검색시 최고 0.46;
    private final static double MIN_SEARCH_SCORE = 0.35;
    private final NewsCache newsCache;
    private final EmbeddingClient embeddingClient;

    public NewsService(NewsCache newsCache, EmbeddingClient embeddingClient) {
        this.newsCache = newsCache;
        this.embeddingClient = embeddingClient;
    }

    public List<NewsSearchResult> search(String query) {

        List<Double> queryToVector = embeddingClient.embedQuery(query);
        List<CachedNews> newsList = newsCache.getCachedNewsList();
        List<NewsSearchResult> results = new ArrayList<>();

        for(CachedNews news : newsList) {
            double score = EmbeddingSimilarity.cosineSimilarity(queryToVector, news.vector());

            if(score >= MIN_SEARCH_SCORE){
                results.add(new NewsSearchResult(
                        news.title(),
                        news.summary(),
                        news.url(),
                        news.publishedAt(),
                        news.source(),
                        score
                ));
            }
        }

        results.sort(Comparator.comparingDouble(NewsSearchResult::score).reversed());

        return results.subList(0, Math.min(MAX_SEARCH_SIZE , results.size()));
    }

}
