package org.jin.newsfinder.embedding;

import java.util.List;

public interface EmbeddingClient {
    List<Double> embedQuery(String text);
    List<List<Double>> embedDocuments(List<String> texts);
}
