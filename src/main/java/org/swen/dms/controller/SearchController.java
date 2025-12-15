package org.swen.dms.controller;

import org.springframework.web.bind.annotation.*;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.service.SearchService;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:4200")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public List<DocumentSearch> search(@RequestParam("q") String query) {
        return searchService.searchDocuments(query);
    }
}
