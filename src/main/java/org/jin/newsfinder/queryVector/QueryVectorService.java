package org.jin.newsfinder.queryVector;

import org.jin.newsfinder.LruCache;
import org.jin.newsfinder.embedding.EmbeddingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class QueryVectorService {

    private final EmbeddingClient embeddingClient;
    private final QueryVectorCacheRepository queryVectorCacheRepository;
    private final String voyageModel;
    private static final Logger log = LoggerFactory.getLogger(QueryVectorService.class);

    private LruCache<String, float[]> lruCached = new LruCache<>(10);

    public QueryVectorService(EmbeddingClient embeddingClient, QueryVectorCacheRepository queryVectorCacheRepository, @Value("${voyage.model}") String voyageModel) {
        this.embeddingClient = embeddingClient;
        this.queryVectorCacheRepository = queryVectorCacheRepository;
        this.voyageModel = voyageModel;
    }

    private String normalizeQuery(String query) {
        String normalized = Normalizer.normalize(query, Normalizer.Form.NFC)
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();

        return normalized;
    }

    public float[] getVector(String query) {

        long startGetVector = System.nanoTime();
        double ms;
        String norm = normalizeQuery(query);
        float[] normToVector;

        try {

            if (lruCached.containsKey(norm)) {
                normToVector = lruCached.get(norm);
                ms = (System.nanoTime() - startGetVector) / 1_000_000.0;
                log.debug("L1_hit_ms={}", ms);
                return normToVector;
            }

            Optional<QueryVectorCache> cached = queryVectorCacheRepository.findByNormalizedQueryAndModel(norm, voyageModel);

            if (cached.isPresent()) {
                QueryVectorCache cache = cached.get();
                lruCached.put(norm, cache.getEmbedding());
                ms = (System.nanoTime() - startGetVector) / 1_000_000.0;
                log.debug("DB_hit_ms={}", ms);
                return cache.getEmbedding();
            } else {
                long apiStart = System.nanoTime();
                normToVector = embeddingClient.embedQuery(norm);
                double apiTime = (System.nanoTime() - apiStart) / 1_000_000.0;
                log.debug("api_time_ms={}", apiTime);
                QueryVectorCache cache = new QueryVectorCache(norm, voyageModel, normToVector, LocalDateTime.now());
                lruCached.put(norm, cache.getEmbedding());
                queryVectorCacheRepository.save(cache);
                ms = (System.nanoTime() - startGetVector) / 1_000_000.0;
                log.debug("L1_and_DB_miss_ms={}", ms);
            }

        } finally {

            ms = (System.nanoTime() - startGetVector) / 1_000_000.0;
            log.debug("total_ms={}", ms);

        }

        return normToVector;

    }


}
