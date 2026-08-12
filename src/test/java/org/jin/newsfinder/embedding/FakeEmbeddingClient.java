package org.jin.newsfinder.embedding;

import java.util.List;

public class FakeEmbeddingClient implements EmbeddingClient {


    @Override
    public float[] embedQuery(String text) {
        float[] test = {1.0f,0.0f};
        return test;
    }

    @Override
    public List<float[]> embedDocuments(List<String> texts) {
        return List.of();
    }
}
