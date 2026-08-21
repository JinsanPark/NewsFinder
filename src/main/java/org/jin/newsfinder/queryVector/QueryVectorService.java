package org.jin.newsfinder.queryVector;

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

    @Value("${voyage.model}")
    private String voyageModel;

    public QueryVectorService(EmbeddingClient embeddingClient, QueryVectorCacheRepository queryVectorCacheRepository) {
        this.embeddingClient = embeddingClient;
        this.queryVectorCacheRepository = queryVectorCacheRepository;
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
        Optional<QueryVectorCache> cached = queryVectorCacheRepository.findByNormalizedQueryAndModel(norm, voyageModel);
        float[] normTovector;

        if (cached.isPresent()){
            QueryVectorCache cache = cached.get();
            return cache.getEmbedding();
        } else {
            normTovector = embeddingClient.embedQuery(norm);
            QueryVectorCache cache = new QueryVectorCache(norm, voyageModel, normTovector, LocalDateTime.now());
            queryVectorCacheRepository.save(cache);
        }

        return normTovector;

    }







}
