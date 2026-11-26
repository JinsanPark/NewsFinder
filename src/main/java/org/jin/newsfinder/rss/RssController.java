package org.jin.newsfinder.rss;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RssController {

    private final RssService rssService;
    private final RssProperties rssProperties;

    public RssController(RssService rssService, RssProperties rssProperties) {
        this.rssService = rssService;
        this.rssProperties = rssProperties;
    }

    // 부를 때마다 Voyage 비용이 나가므로 배포 전에 제거
    @PostMapping("/admin/rss")
    Map<String, Integer> ingest() {
        Map<String, Integer> savedBySource = new LinkedHashMap<>();
        for (RssProperties.Feed feed : rssProperties.feeds()) {
            savedBySource.put(feed.source(), rssService.ingest(feed.source(), feed.url()));
        }
        return savedBySource;
    }
}
