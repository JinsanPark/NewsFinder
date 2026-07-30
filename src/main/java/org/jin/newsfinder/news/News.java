package org.jin.newsfinder.news;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class News {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(length = 20000)
    private String content;

    private String summary;
    private String url;
    private String source;
    private LocalDateTime publishedAt;

    @Column(length = 30000)
    private String embedding;

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getEmbedding() {
        return embedding;
    }

    public News(String title, String content, String summary, String url, String source, LocalDateTime publishedAt){
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.url = url;
        this.source = source;
        this.publishedAt = publishedAt;
    }

    protected News(){

    }

}


