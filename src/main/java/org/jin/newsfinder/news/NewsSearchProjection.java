package org.jin.newsfinder.news;

import java.time.LocalDateTime;

public interface NewsSearchProjection {
    String getTitle();
    double getScore();
    String getSummary();
    String getUrl();
    LocalDateTime getPublishedAt();
    String getSource();
}
