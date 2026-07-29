package org.jin.newsfinder.embedding;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingConverterTest {

    @Test
    void 벡터를_문자열로_바꿨다가_되돌리면_원본과_같은지() {

        List<Double> list = List.of(0.1,-0.2,0.3);
        String text = EmbeddingConverter.toText(list);
        List<Double> result = EmbeddingConverter.toVector(text);

        assertThat(result).isEqualTo(list);
    }
}
