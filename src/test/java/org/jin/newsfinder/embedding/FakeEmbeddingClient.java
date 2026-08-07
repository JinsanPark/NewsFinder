package org.jin.newsfinder.embedding;

import java.util.List;

public class FakeEmbeddingClient implements EmbeddingClient {


    @Override
    public List<Double> embedQuery(String text) {
        return List.of(1.0, 0.0);
    }

    @Override
    public List<List<Double>> embedDocuments(List<String> texts) {
        return List.of();
    }
}
