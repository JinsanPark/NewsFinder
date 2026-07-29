package org.jin.newsfinder;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final NewsRepository newsRepository;
    private final EmbeddingClient embeddingClient;

    public DataInitializer(NewsRepository newsRepository, EmbeddingClient embeddingClient) {
        this.newsRepository = newsRepository;
        this.embeddingClient = embeddingClient;
    }

    @Override
    public void run(String... args) throws Exception {
//        News news = new News("제목", "본문", "요약", "URL", "원본", LocalDateTime.now());
//        news.setEmbedding("0.1, 0.2, 0.3");
//        newsRepository.save(news);
        List<Double> vector = embeddingClient.embedDocument("안녕하세요.");
        System.out.println(vector.size());
        System.out.println(vector.subList(0,5));

    }




}
