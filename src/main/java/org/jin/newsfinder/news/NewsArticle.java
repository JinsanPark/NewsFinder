package org.jin.newsfinder.news;

import java.time.LocalDateTime;

public record NewsArticle(String title, String content, String summary, String url, String source, LocalDateTime publishedAt) {
}
