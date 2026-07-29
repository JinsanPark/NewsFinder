package org.jin.newsfinder.embedding;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingConverter {
    public static String toText(List<Double> vector){

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < vector.size(); i++){
            if(i < vector.size() - 1){
                sb.append(vector.get(i)).append(",");
            } else {
                sb.append(vector.get(i));
            }
        }
        return sb.toString();
    }

    public static List<Double> toVector(String text){

        List<Double> vector = new ArrayList<>();
        String[] parts = text.split(",");

        for (int i = 0; i < parts.length; i++) {
            vector.add(Double.parseDouble(parts[i]));
        }

        return vector;
    }

}
