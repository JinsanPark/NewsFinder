package org.jin.newsfinder.news;


import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024)
    private float[] embedding;

    public void setEmbedding(float[] embedding) {
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

    public float[] getEmbedding() {
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


