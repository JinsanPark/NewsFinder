package org.jin.newsfinder.rss;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jin.newsfinder.embedding.EmbeddingClient;
import org.jin.newsfinder.news.News;
import org.jin.newsfinder.news.NewsRepository;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RssService {

    private final EmbeddingClient embeddingClient;
    private final NewsRepository newsRepository;
    private final RestClient restClient;

    public RssService(EmbeddingClient embeddingClient, NewsRepository newsRepository) {
        this.embeddingClient = embeddingClient;
        this.newsRepository = newsRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public int ingest(String source, String feedUrl) {
        List<SyndEntry> entries = fetch(feedUrl).getEntries();

        List<String> feedUrls = entries.stream().map(SyndEntry::getLink).toList();
        // 임베딩 전에 걸러야 중복 기사에 Voyage 비용이 안 나간다
        Set<String> seen = new HashSet<>(newsRepository.findExistingUrls(feedUrls));

        List<News> newsList = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        for (SyndEntry entry : entries) {
            if (!seen.add(entry.getLink())) {
                continue;
            }

            String title = unescape(entry.getTitle());
            String summary = meaningfulOrNull(entry.getDescription());

            newsList.add(new News(
                    title,
                    null,
                    summary,
                    entry.getLink(),
                    source,
                    toLocalDateTime(entry.getPublishedDate())
            ));

            texts.add(summary == null ? title : summary + "\n" + title);
        }

        if (newsList.isEmpty()) {
            return 0;
        }

        List<float[]> vectors = embeddingClient.embedDocuments(texts);

        for (int i = 0; i < newsList.size(); i++) {
            newsList.get(i).setEmbedding(vectors.get(i));
        }

        newsRepository.saveAll(newsList);
        return newsList.size();
    }

    private SyndFeed fetch(String feedUrl) {
        byte[] body = restClient.get().uri(feedUrl).retrieve().body(byte[].class);

        if (body == null) {
            throw new IllegalStateException("RSS 응답이 비어 있음 : " + feedUrl);
        }

        try {
            return new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(body)));
        } catch (FeedException | IOException e) {
            throw new IllegalStateException("RSS 파싱 실패 : " + feedUrl, e);
        }
    }

    // 짧은 속보는 description이 "(" 처럼 머리말 첫 글자만 남고 잘려 온다. 글자·숫자가 없으면 요약으로 보지 않는다
    private String meaningfulOrNull(SyndContent description) {
        if (description == null || description.getValue() == null) {
            return null;
        }

        String value = unescape(description.getValue()).strip();
        return value.chars().anyMatch(Character::isLetterOrDigit) ? value : null;
    }

    // HtmlUtils는 HTML 4.01 기준이라 &apos;를 모른다
    private String unescape(String raw) {
        if (raw == null) {
            return null;
        }
        return HtmlUtils.htmlUnescape(raw).replace("&apos;", "'");
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Asia/Seoul"));
    }
}
