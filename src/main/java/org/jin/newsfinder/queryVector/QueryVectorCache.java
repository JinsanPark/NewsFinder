package org.jin.newsfinder.queryVector;

import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"normalized_query", "model"} , name = "uk_query_vector_cache_query_model"))
public class QueryVectorCache {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String normalizedQuery;
    private String model;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024)
    private float[] embedding;

    private LocalDateTime createdAt;


    public Long getId() {
        return id;
    }

    public String getNormalizedQuery() {
        return normalizedQuery;
    }

    public String getModel() {
        return model;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public QueryVectorCache(String normalizedQuery, String model, float[] embedding, LocalDateTime createdAt) {
        this.normalizedQuery = normalizedQuery;
        this.model = model;
        this.embedding = embedding;
        this.createdAt = createdAt;
    }

    protected QueryVectorCache(){

    }

}
