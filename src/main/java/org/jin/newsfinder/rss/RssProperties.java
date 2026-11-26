package org.jin.newsfinder.rss;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "rss")
public record RssProperties(List<Feed> feeds) {

    public record Feed(String source, String url) {
    }
}
