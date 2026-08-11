package org.jin.newsfinder.news;

import jakarta.annotation.PostConstruct;
import org.jin.newsfinder.embedding.EmbeddingConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NewsCache {

    private final NewsRepository newsRepository;
    private List<CachedNews> cachedNewsList;

    public NewsCache(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @PostConstruct
    public void loadCache() {

        List<News> newsList = newsRepository.findAll();
        this.cachedNewsList = new ArrayList<>();


        for (News news : newsList) {
            cachedNewsList.add(new CachedNews(news.getTitle(), news.getSummary(), news.getUrl(), news.getSource(),news.getPublishedAt(), EmbeddingConverter.toVector(news.getEmbedding())));
        }
    }

    public List<CachedNews> getCachedNewsList() {
        return cachedNewsList;
    }

}
