package org.jin.newsfinder.news;

import java.time.LocalDateTime;

public record NewsSearchResult(String title, String summary, String url, LocalDateTime publishedAt, String source, double score) {}