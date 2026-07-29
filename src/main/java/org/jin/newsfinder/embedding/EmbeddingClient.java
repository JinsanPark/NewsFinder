package org.jin.newsfinder.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class EmbeddingClient {
    @Value("${voyage.api-key}")
    private String apiKey;
    private final RestClient restClient = RestClient.create();

    public String maskedKey() {
        return apiKey.substring(0,5);
    }

    private List<Double> embed(String text, String inputType){

        EmbeddingRequest embeddingRequest = new EmbeddingRequest(List.of(text),"voyage-4-lite" ,inputType);

        EmbeddingResponse response = restClient.post()
                .uri("https://api.voyageai.com/v1/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(embeddingRequest)
                .retrieve()
                .body(EmbeddingResponse.class);

        return response.data().get(0).embedding();
    }

    public List<Double> embedDocument(String text) {
        return embed(text,"document");
    }

    public List<Double> embedQuery(String text) {
        return embed(text,"query");
    }
}


