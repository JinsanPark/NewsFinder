package org.jin.newsfinder.embedding;

import java.util.List;

public interface EmbeddingClient {
    float[] embedQuery(String text);
    List<float[]> embedDocuments(List<String> texts);

}
