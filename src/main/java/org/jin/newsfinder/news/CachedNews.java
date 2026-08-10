package org.jin.newsfinder.news;

import java.time.LocalDateTime;
import java.util.List;

public record CachedNews(String title, String summary, String url, String source, LocalDateTime publishedAt, List<Double> vector) {
}
