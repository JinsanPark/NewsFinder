package org.jin.newsfinder;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.jin.newsfinder.news.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("init")
public class DataInitializer implements CommandLineRunner {

    private final NewsRepository newsRepository;
    private final EmbeddingClient embeddingClient;
    private final ObjectMapper objectMapper;

    public DataInitializer(NewsRepository newsRepository, EmbeddingClient embeddingClient,ObjectMapper objectMapper) {
        this.newsRepository = newsRepository;
        this.embeddingClient = embeddingClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        if (newsRepository.count() > 0){
            System.out.println("데이터 이미 있음.");
            return;
        }

        NewsArticle[] articles = objectMapper.readValue(new File("data/news_articles_dummy.json"), NewsArticle[].class);
        long start = System.nanoTime();

        List<String> newsString = new ArrayList<>();

        for (NewsArticle article : articles){
                newsString.add(article.summary() + "\n" + article.title());
        }

        List<float[]> vectors = embeddingClient.embedDocuments(newsString);

        List<News> newsList = new ArrayList<>();

        for (int i = 0; i < articles.length; i++) {
            NewsArticle article = articles[i];
            float[] vector = vectors.get(i);

            News news = new News(
                    article.title(),
                    article.content(),
                    article.summary(),
                    article.url(),
                    article.source(),
                    article.publishedAt()
            );

            news.setEmbedding(vector);
            newsList.add(news);

        }

        newsRepository.saveAll(newsList);

        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("%.5f초%n", seconds);

    }
}
