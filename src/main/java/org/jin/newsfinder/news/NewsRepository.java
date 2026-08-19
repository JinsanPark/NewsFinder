package org.jin.newsfinder.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    @Query(value = """
        SELECT title,
               summary,
               url,
               published_at AS "publishedAt",
               source,
               1 - (embedding <=> CAST(:queryVector AS vector)) AS "score"
        FROM news
        WHERE 1 - (embedding <=> CAST(:queryVector AS vector)) >= :minScore
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<NewsSearchProjection> searchByVector(@Param("queryVector") float[] queryVector,
                                              @Param("minScore") double minScore,
                                              @Param("limit") int limit);

}
