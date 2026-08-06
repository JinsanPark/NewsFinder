package org.jin.newsfinder.news;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.jin.newsfinder.embedding.EmbeddingConverter;
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
    private final NewsRepository newsRepository;
    private final EmbeddingClient embeddingClient;

    public NewsService(NewsRepository newsRepository, EmbeddingClient embeddingClient) {
        this.newsRepository = newsRepository;
        this.embeddingClient = embeddingClient;
    }

    public List<NewsSearchResult> search(String query) {

        List<Double> queryToVector = embeddingClient.embedQuery(query);
        List<News> newsList = newsRepository.findAll();
        List<NewsSearchResult> results = new ArrayList<>();

        for(News news : newsList) {
            List<Double> newsVector = EmbeddingConverter.toVector(news.getEmbedding());
            double score = EmbeddingSimilarity.cosineSimilarity(queryToVector, newsVector);

            if(score >= MIN_SEARCH_SCORE){
                results.add(new NewsSearchResult(
                        news.getTitle(),
                        news.getSummary(),
                        news.getUrl(),
                        news.getPublishedAt(),
                        news.getSource(),
                        score
                ));
            }
        }

        results.sort(Comparator.comparingDouble(NewsSearchResult::score).reversed());

        return results.subList(0, Math.min(MAX_SEARCH_SIZE , results.size()));
    }

}
