package org.jin.newsfinder.embedding;

import java.util.List;

public record EmbeddingRequest(List<String> input, String model, String input_type) {
}
