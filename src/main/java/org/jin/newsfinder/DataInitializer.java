package org.jin.newsfinder;

import org.jin.newsfinder.embedding.EmbeddingClient;
import org.jin.newsfinder.embedding.EmbeddingConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//@Component
public class DataInitializer implements CommandLineRunner {

    private final NewsRepository newsRepository;
    private final EmbeddingClient embeddingClient;

    public DataInitializer(NewsRepository newsRepository, EmbeddingClient embeddingClient) {
        this.newsRepository = newsRepository;
        this.embeddingClient = embeddingClient;
    }

    @Override
    public void run(String... args) throws Exception {

        News news = new News(
                "코스피 5000 시대",
                "코스피 9000에서 5000으로 하락",
                "누구의 책임인가",
                "https://www.test.test/view/1234?section=market/",
                "테스트 뉴스",
                LocalDateTime.now());


        List<Double> vector = embeddingClient.embedDocument(news.getSummary());
        news.setEmbedding(EmbeddingConverter.toText(vector));
        newsRepository.save(news);
        System.out.println(vector.size());
        System.out.println(vector.subList(0,5));

    }
}
