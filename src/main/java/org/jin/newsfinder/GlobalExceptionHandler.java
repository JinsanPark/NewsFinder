package org.jin.newsfinder;

import org.jin.newsfinder.embedding.EmbeddingApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmbeddingApiException.class)
    public String handleEmbeddingError(EmbeddingApiException e, Model model){
        log.error("Voyage API 호출 실패", e);
        model.addAttribute("errorMessage", "검색 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        return "search";
    }


}
