package org.swen.dms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.repository.search.DocumentSearchRepository;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final DocumentSearchRepository repository;

    public SearchServiceImpl(DocumentSearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DocumentSearch> searchDocuments(String query) {
        try {
            return repository.searchByContent(query);

        } catch (Exception e) {
            log.error("Elasticsearch search failed: {}", e.getMessage());
            return List.of();
        }
    }
}