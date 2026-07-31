package org.jin.newsfinder;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.jin.newsfinder.embedding.EmbeddingConverter;
import org.jin.newsfinder.news.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final NewsRepository newsRepository;
    private final EmbeddingClient embeddingClient;
    private final NewsService newsService;
    private final ObjectMapper objectMapper;

    public DataInitializer(NewsRepository newsRepository, EmbeddingClient embeddingClient, NewsService newsService, ObjectMapper objectMapper) {
        this.newsRepository = newsRepository;
        this.embeddingClient = embeddingClient;
        this.newsService = newsService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        NewsArticle[] articles = objectMapper.readValue(new File("data/news_articles_dummy.json"), NewsArticle[].class);
        int count = 0;
        long start = System.nanoTime();
        for (NewsArticle article : articles){

            News news = new News(
                    article.title(),
                    article.content(),
                    article.summary(),
                    article.url(),
                    article.source(),
                    article.publishedAt()
            );

            List<Double> vector = embeddingClient.embedDocument(news.getSummary() + "\n" + news.getTitle());
            news.setEmbedding(EmbeddingConverter.toText(vector));
            newsRepository.save(news);
            count++;

            if (count % 50 == 0){
                System.out.println(count + "번째 입니다.");
            }

        }

        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("%.5f초%n", seconds);

    }
}
