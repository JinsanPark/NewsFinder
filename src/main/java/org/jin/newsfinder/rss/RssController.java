package org.jin.newsfinder.rss;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RssController {

    private static final String YONHAP_FEED_URL = "https://www.yna.co.kr/rss/news.xml";

    private final RssService rssService;

    public RssController(RssService rssService) {
        this.rssService = rssService;
    }

    // 부를 때마다 Voyage 비용이 나가므로 배포 전에 제거
    @PostMapping("/admin/rss")
    String ingest() {
        int saved = rssService.ingest("연합뉴스", YONHAP_FEED_URL);
        return saved + "건 저장";
    }
}
