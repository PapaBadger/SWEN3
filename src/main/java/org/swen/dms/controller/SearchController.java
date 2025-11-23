package org.swen.dms.controller;

import org.springframework.web.bind.annotation.*;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.service.SearchService;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:4200") // Allows your Angular frontend to access this
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Usage: GET http://localhost:8080/api/search?q=invoice
    @GetMapping
    public List<DocumentSearch> search(@RequestParam("q") String query) {
        return searchService.searchDocuments(query);
    }
}
