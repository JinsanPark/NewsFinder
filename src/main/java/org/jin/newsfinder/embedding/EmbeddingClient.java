package org.jin.newsfinder.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
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

    private List<List<Double>> embedBatch(List<String> chunk) {

        EmbeddingRequest embeddingRequest = new EmbeddingRequest(chunk, "voyage-4-lite", "document");
        List<List<Double>> result = new ArrayList<>();

        EmbeddingResponse response = restClient.post()
                .uri("https://api.voyageai.com/v1/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(embeddingRequest)
                .retrieve()
                .body(EmbeddingResponse.class);


        for (EmbeddingData data : response.data()){
            result.add(data.embedding());
        }

        return result;

    }

    public List<Double> embedDocument(String text) {
        return embed(text,"document");
    }

    public List<Double> embedQuery(String text) {
        return embed(text,"query");
    }

    public List<List<Double>> embedDocuments(List<String> texts){

        List<List<Double>> batchList = new ArrayList<>();

        //voyage4lite batch 요청 최대 크기 128
        for(int i = 0; i < texts.size(); i += 128){
            int end = Math.min(texts.size(), i + 128);
            List<String> chunk = texts.subList(i,end);
            batchList.addAll(embedBatch(chunk));
        }

        return batchList;

    }
}


