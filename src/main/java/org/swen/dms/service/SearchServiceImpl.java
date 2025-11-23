package org.swen.dms.service;

import org.springframework.stereotype.Service;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.repository.search.DocumentSearchRepository;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    private final DocumentSearchRepository repository;

    public SearchServiceImpl(DocumentSearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DocumentSearch> searchDocuments(String query) {
        // "findByContentContaining" creates a wildcard query (*query*)
        return repository.findByContentContaining(query);
    }
}