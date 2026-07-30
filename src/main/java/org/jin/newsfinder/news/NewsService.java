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
            results.add(new NewsSearchResult(news,score));
        }

        results.sort(Comparator.comparingDouble(NewsSearchResult::score).reversed());

        return results;
    }

}
