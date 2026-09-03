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

    private LruCache<String, float[]> lruCached = new LruCache<>(100);

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
        String path = null;
        double apiTime = 0;
        double saveTime = 0;

        try {

            if (lruCached.containsKey(norm)) {
                path = "L1_hit";
                normToVector = lruCached.get(norm);
                return normToVector;
            }

            Optional<QueryVectorCache> cached = queryVectorCacheRepository.findByNormalizedQueryAndModel(norm, voyageModel);

            if (cached.isPresent()) {
                path = "DB_hit";
                QueryVectorCache cache = cached.get();
                lruCached.put(norm, cache.getEmbedding());
                return cache.getEmbedding();
            } else {
                path = "API_miss";
                long apiStart = System.nanoTime();
                normToVector = embeddingClient.embedQuery(norm);
                apiTime = (System.nanoTime() - apiStart) / 1_000_000.0;
                QueryVectorCache cache = new QueryVectorCache(norm, voyageModel, normToVector, LocalDateTime.now());
                lruCached.put(norm, cache.getEmbedding());
                long saveStart = System.nanoTime();
                queryVectorCacheRepository.save(cache);
                saveTime = (System.nanoTime() - saveStart) / 1_000_000.0;
            }

        } finally {
            ms = (System.nanoTime() - startGetVector) / 1_000_000.0;
            log.debug("path={} term={} save_ms={} api_ms={} total_ms={}", path, norm, saveTime , apiTime, ms);
        }

        return normToVector;

    }


}
