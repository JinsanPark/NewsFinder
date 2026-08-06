package org.jin.newsfinder.news;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }


    @GetMapping("/search")
    String search(@RequestParam(required = false) String query, Model model){

        if(query != null && !query.isBlank()){
            model.addAttribute("results", newsService.search(query));
        }

        return "search";
    }


}
