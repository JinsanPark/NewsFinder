package org.jin.newsfinder.embedding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

class EmbeddingSimilarityTest {

    @Test
    void 같은_백터는_1임(){

        List<Double> vectorA = List.of(1.0, 1.0);
        List<Double> vectorB = List.of(1.0, 1.0);

        Double result1 = EmbeddingSimilarity.cosineSimilarity(vectorA,vectorB);

        assertThat(result1).isCloseTo(1.0,within(0.0001));
    }

    @Test
    void 직각은_0임(){

        List<Double> vectorA = List.of(1.0, 0.0);
        List<Double> vectorB = List.of(0.0, 1.0);

        Double result1 = EmbeddingSimilarity.cosineSimilarity(vectorA,vectorB);

        assertThat(result1).isCloseTo(0.0,within(0.0001));
    }

    @Test
    void 다른_방향_백터는_마이너스1임(){

        List<Double> vectorA = List.of(1.0, 1.0);
        List<Double> vectorB = List.of(-1.0, -1.0);

        Double result1 = EmbeddingSimilarity.cosineSimilarity(vectorA,vectorB);

        assertThat(result1).isCloseTo(-1.0,within(0.0001));
    }

    @Test
    void 길이가_달라도_방향이_같은_백터는_1임(){

        List<Double> vectorA = List.of(1.0, 1.0);
        List<Double> vectorB = List.of(3.0, 3.0);

        Double result1 = EmbeddingSimilarity.cosineSimilarity(vectorA,vectorB);

        assertThat(result1).isCloseTo(1.0,within(0.0001));
    }

}
