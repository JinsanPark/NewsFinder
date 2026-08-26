package org.jin.newsfinder.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class VoyageEmbeddingClient implements EmbeddingClient{
    @Value("${voyage.api-key}")
    private String apiKey;

    private final String voyageModel;
    private final RestClient restClient = RestClient.create();


    public VoyageEmbeddingClient(@Value("${voyage.model}") String voyageModel) {
        this.voyageModel = voyageModel;
    }

    private float[] embed(String text, String inputType){

        EmbeddingRequest embeddingRequest = new EmbeddingRequest(List.of(text), voyageModel ,inputType);

        EmbeddingResponse response = restClient.post()
                .uri("https://api.voyageai.com/v1/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(embeddingRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new EmbeddingApiException("Voyage 오류 : " + res.getStatusCode(), null);
                })
                .body(EmbeddingResponse.class);

        if(response == null || response.data().isEmpty()) {
            throw new EmbeddingApiException("Voyage 응답 없음", null);
        }

        return response.data().get(0).embedding();
    }

    private List<float[]> embedBatch(List<String> chunk) {

        EmbeddingRequest embeddingRequest = new EmbeddingRequest(chunk, voyageModel , "document");
        List<float[]> result = new ArrayList<>();

        EmbeddingResponse response = restClient.post()
                .uri("https://api.voyageai.com/v1/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(embeddingRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new EmbeddingApiException("Voyage 오류 : " + res.getStatusCode(), null);
                })
                .body(EmbeddingResponse.class);

        if(response == null || chunk.size() != response.data().size()){
            System.out.println("청크 개수" + chunk.size());
            System.out.println("데이터 개수" + response.data().size());
            throw new EmbeddingApiException("데이터 매칭 실패", null);
        }


        for (EmbeddingData data : response.data()){
            result.add(data.embedding());
        }

        return result;

    }

    public float[] embedDocument(String text) {
        return embed(text,"document");
    }

    public float[] embedQuery(String text) {
        return embed(text,"query");
    }

    public List<float[]> embedDocuments(List<String> texts){

        List<float[]> batchList = new ArrayList<>();

        //voyage4lite batch 요청 최대 크기 1000. 128로 쪼갬
        for(int i = 0; i < texts.size(); i += 128){
            int end = Math.min(texts.size(), i + 128);
            List<String> chunk = texts.subList(i,end);
            batchList.addAll(embedBatch(chunk));
        }

        return batchList;

    }
}


