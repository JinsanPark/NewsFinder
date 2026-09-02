package org.jin.newsfinder.queryVector;

import org.jin.newsfinder.LruCache;
import org.jin.newsfinder.embedding.EmbeddingClient;
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

    private LruCache<String, float[]> lruCached = new LruCache<>(10);

    public QueryVectorService(EmbeddingClient embeddingClient, QueryVectorCacheRepository queryVectorCacheRepository, @Value("${voyage.model}") String voyageModel) {
        this.embeddingClient = embeddingClient;
        this.queryVectorCacheRepository = queryVectorCacheRepository;
        this.voyageModel = voyageModel;
    }

    private String normalizeQuery(String query) {
        String normalized = Normalizer.normalize(query, Normalizer.Form.NFC)
                .trim()
                .replaceAll("\\s+" , " ")
                .toLowerCase();

        return normalized;
    }

    public float[] getVector(String query) {

        String norm = normalizeQuery(query);
        float[] normToVector;

       if (lruCached.containsKey(norm)) {
           normToVector = lruCached.get(norm);
           return normToVector;
       }

        Optional<QueryVectorCache> cached = queryVectorCacheRepository.findByNormalizedQueryAndModel(norm, voyageModel);

        if (cached.isPresent()){
            QueryVectorCache cache = cached.get();
            lruCached.put(norm,cache.getEmbedding());
            return cache.getEmbedding();
        } else {
            normToVector = embeddingClient.embedQuery(norm);
            QueryVectorCache cache = new QueryVectorCache(norm, voyageModel, normToVector, LocalDateTime.now());
            lruCached.put(norm,cache.getEmbedding());
            queryVectorCacheRepository.save(cache);
        }

        return normToVector;

    }







}
