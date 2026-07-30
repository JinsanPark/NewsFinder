package org.jin.newsfinder.embedding;

import java.util.List;

public class EmbeddingSimilarity {
    public static double cosineSimilarity(List<Double> vector1, List<Double> vector2){
        double innerProduct = 0;
        double vector1Mag = 0;
        double vector2Mag = 0;

        for(int i = 0; i < vector1.size(); i++){
            innerProduct += vector1.get(i) * vector2.get(i);
            vector1Mag += Math.pow(vector1.get(i),2);
            vector2Mag += Math.pow(vector2.get(i),2);
        }

        vector1Mag = Math.sqrt(vector1Mag);
        vector2Mag = Math.sqrt(vector2Mag);

        return  innerProduct / (vector1Mag * vector2Mag);
    }

}
